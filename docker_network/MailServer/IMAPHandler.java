/*
 * détecte une commande, exécuter ce qu'elle doit faire, envoyer réponse
 * LOGIN, LIST, SELECT
 * UID FETCH
 * FLAGS
 * EXPUNGE
 * 
 */

import java.util.ArrayList;
import java.util.List;

public class IMAPHandler extends Handler {
    private User user;
    private int state = NOT_CONNECTED;
    private Mailbox selectedMailbox;

    private final static int NOT_CONNECTED = 0;
    private final static int CONNECTED = 1;
    private final static int SELECTED = 2;

    public IMAPHandler(ConnectionIO connecionIO) {
        super(connecionIO);
    }

    @Override
    public void sendGreeting() {
        connectionIO.sendMessage("* OK IMAP server ready");

    }

    @Override
    public void handleCommand(String command) {
        if (command == null) {
            connectionActive = false;
            return;
        }

        // Chaque command est de la forme tag - command - args (au max 3 parties, tt le
        // reste est mis dans args)
        String[] parts = command.split("\\s+", 3);
        String tag = parts[0];
        if (parts.length < 2) {
            if (!tag.isEmpty())
                connectionIO.sendMessage(tag + " BAD");
            else
                connectionIO.sendMessage("* BAD Missing tag");

            return;
        }

        String cmd = parts[1];
        cmd = cmd.toUpperCase();
        String args = "";
        if (parts.length >= 3)
            args = parts[2].trim();

        switch (cmd) {
            case "CAPABILITY":
                handleCapability(tag);
                break;
            case "NOOP":
                handleNoop(tag);
                break;
            case "LOGIN":
                handleLogin(tag, args);
                break;
            case "LOGOUT":
                handleLogout(tag);
                break;
            case "LIST":
                handleList(tag, args);
                break;
            case "SELECT":
                handleSelect(tag, args);
                break;
            case "UID":
                handleUid(tag, args);
                break;
            case "EXPUNGE":
                handleExpunge(tag);
                break;
            case "CLOSE":
                handleClose(tag);
                break;
            default:
                connectionIO.sendMessage(tag + " BAD Invalid command");
        }
    }

    private void handleCapability(String tag) {
        if (!isValidState(tag, "CAPABILITY"))
            return;
        connectionIO.sendMessage("* CAPABILITY IMAP4rev1 UID");
        connectionIO.sendMessage(tag + " OK CAPABILITY completed");
    }

    private void handleNoop(String tag) {
        if (!isValidState(tag, "NOOP"))
            return;
        connectionIO.sendMessage(tag + " OK NOOP completed");
    }

    private void handleLogin(String tag, String args) {
        if (!isValidState(tag, "LOGIN"))
            return;
        String parts[] = args.split("\\s+");
        if (parts.length != 2) {
            connectionIO.sendMessage(tag + " BAD Invalid arguments");
            return;
        }
        String userName = parts[0];
        String password = parts[1];
        user = new User(userName);

        if (!user.checkDomain()) {
            connectionIO.sendMessage(tag + " NO Domain not valid");
            return;
        }

        if (!user.checkPassword(password)) {
            connectionIO.sendMessage(tag + " NO Incorrect password");
            return;
        }

        state = CONNECTED;
        connectionIO.sendMessage(tag + " OK LOGIN completed");

        MailStore.createMailboxIfNotExists(user, "INBOX");
    }

    private void handleLogout(String tag) {
        if (!isValidState(tag, "LOGOUT"))
            return;
        connectionIO.sendMessage("* BYE IMAP server logging out");

        if(selectedMailbox != null)
            MailStore.saveMailbox(user, selectedMailbox);
        connectionActive = false;
        connectionIO.sendMessage(tag + " OK LOGOUT completed");
    }

    private void handleList(String tag, String args) {
        if (!isValidState(tag, "LIST"))
            return;

        String[] parts = args.split("\\s+");
        if (parts.length != 2) {
            connectionIO.sendMessage(tag + " BAD Invalid arguments");
            return;
        }

        String[] listMailboxes = MailStore.getListMailboxes(user);
        if (listMailboxes.length == 0) {
            connectionIO.sendMessage(tag + " BAD No mailbox existing");
            return;
        }

        for (String mailboxName : listMailboxes) {
            connectionIO.sendMessage("* LIST (\\HasNoChildren) + \"/\" " + mailboxName);
        }
        connectionIO.sendMessage(tag + " OK LIST completed");
    }

    private void handleSelect(String tag, String args) {
        if (!isValidState(tag, "SELECT"))
            return;

        String[] parts = args.split("\\s+");
        if (parts.length != 1) {
            connectionIO.sendMessage(tag + " BAD Invalid arguments");
            return;
        }

        String mailboxName = parts[0];
        Mailbox mailbox = MailStore.loadMailbox(user, mailboxName);
        int nbMessages = mailbox.getExistsCount();

        connectionIO.sendMessage("* " + nbMessages + " EXISTS");
        connectionIO.sendMessage(tag + " OK [UIDVALIDITY " + mailbox.getUidValidity() + "] SELECT completed");
        selectedMailbox = mailbox;
        state = SELECTED;
    }

    private void handleUid(String tag, String args) {
        String[] parts = args.split("\\s+", 2);
        if (parts.length < 2) {
            connectionIO.sendMessage(tag + " BAD Invalid arguments");
            return;
        }
        if (parts[0].equals("FETCH"))
            handleUidFetch(tag, args);
        else if (parts[0].equals("STORE"))
            handleUidStore(tag, args);
        else {
            connectionIO.sendMessage(tag + " BAD UID does not match UID FETCH or UID STORE");
            return;
        }
    }

    private void handleUidFetch(String tag, String args) {
        if (!isValidState(tag, "UID"))
            return;

        // FETCH-RANGE-dataItems
        String[] parts = args.split("\\s+", 3);
        if (parts.length != 3) {
            connectionIO.sendMessage(tag + " BAD Invalid arguments");
            return;
        }

        String range = parts[1];
        String dataItems = parts[2];
        if (!dataItems.startsWith("(") || !dataItems.endsWith(")")) {
            connectionIO.sendMessage(tag + " BAD Data items arguments");
            return;
        }

        List<Integer> rangeUid = getUidRange(range);
        if (rangeUid == null) {
            connectionIO.sendMessage(tag + " BAD Invalid arguments");
            return;
        }

        for (Integer uid : rangeUid) {
            Message message = selectedMailbox.getMessageByUid(uid);
            connectionIO.sendMessage("* " + message.getSequenceNumber(selectedMailbox) + " FETCH (UID " + uid
                    + " FLAGS (" + message.setFlagsToString()
                    + " BODY[] {" + message.size() + "}");
            for (String dataLine : message.getDataLines()) {
                connectionIO.sendMessage(dataLine);
            }
            connectionIO.sendMessage(")");
        }
        connectionIO.sendMessage(tag + " OK FETCH completed");
    }

    // UID STORE range +FLAGS (\Deleted \Seen)
    // -FLAGS (\Deleted)
    // FLAGS
    private void handleUidStore(String tag, String args) {
        if (!isValidState(tag, "UID"))
            return;

        String[] parts = args.split("\\s+", 4);
        if (parts.length < 4) {
            connectionIO.sendMessage(tag + " BAD Invalid arguments");
            return;
        }
        String range = parts[1];
        String flagLabel = parts[2];
        String flags = parts[3];

        List<Integer> rangeUid = getUidRange(range);

        if (!flags.startsWith("(") || !flags.endsWith(")")) {
            connectionIO.sendMessage(tag + " BAD Missing values");
            return;
        }

        String[] flagsParts = flags.substring(1, flags.length() - 1).split("\\s+");

        switch (flagLabel) {
            case "+FLAGS":
                for (Integer uid : rangeUid) {
                    Message message = selectedMailbox.getMessageByUid(uid);
                    if(message == null)
                        System.err.println("null message at line 265");
        
                    for (String f : flagsParts){
                        if(f == null)
                            System.err.println("null string at line 265");
                        message.addFlag(f);
                }
                    connectionIO.sendMessage("* " + uid + " FETCH (FLAGS(" + message.setFlagsToString() + "))");
                }
                break;
            case "-FLAGS":
                for (Integer uid : rangeUid) {
                    Message message = selectedMailbox.getMessageByUid(uid);
                    for (String f : flagsParts)
                        message.removeFlag(f);
                    connectionIO.sendMessage("* " + uid + " FETCH (FLAGS(" + message.setFlagsToString() + "))");
                }
                break;
            case "FLAGS":
                for (Integer uid : rangeUid) {
                    Message message = selectedMailbox.getMessageByUid(uid);
                    for (String f : flagsParts) {
                        message.resetFlags();
                        message.addFlag(f);
                    }
                    connectionIO.sendMessage("* " + uid + " FETCH (FLAGS(" + message.setFlagsToString() + "))");
                }
                break;
            default:
                connectionIO.sendMessage(tag + " BAD Invalid arguments");
                return;
        }

        connectionIO.sendMessage(tag + " OK UID STORE completed");
    }

    private void handleExpunge(String tag) {
        if (!isValidState(tag, "EXPUNGE"))
            return;

        List<Integer> sequenceIndex = selectedMailbox.expunge();
        MailStore.expungeMailbox(user, selectedMailbox);
        for (Integer i : sequenceIndex)
            connectionIO.sendMessage("* " + i + " EXPUNGE");

        connectionIO.sendMessage(tag + " OK EXPUNGE completed");
    }

    private void handleClose(String tag) {
        if (!isValidState(tag, "CLOSE"))
            return;

        selectedMailbox.expunge();
        MailStore.expungeMailbox(user, selectedMailbox);
        MailStore.saveMailbox(user, selectedMailbox);
        selectedMailbox = null;
        state = CONNECTED;

        connectionIO.sendMessage(tag + " OK CLOSE completed");
    }

    private boolean isValidState(String tag, String command) {
        switch (command) {
            case "CAPABILITY":
                if (state == NOT_CONNECTED || state == CONNECTED || state == SELECTED)
                    return true;
                break;
            case "NOOP":
                if (state == NOT_CONNECTED || state == CONNECTED || state == SELECTED)
                    return true;
                break;
            case "LOGIN":
                if (state == NOT_CONNECTED)
                    return true;
                break;
            case "LOGOUT":
                if (state == NOT_CONNECTED || state == CONNECTED || state == SELECTED)
                    return true;
                break;
            case "LIST":
                if (state == CONNECTED)
                    return true;
                break;
            case "SELECT":
                if (state == CONNECTED)
                    return true;
                break;
            case "UID":
                if (state == SELECTED)
                    return true;
                break;
            case "EXPUNGE":
                if (state == SELECTED)
                    return true;
                break;
            case "CLOSE":
                if (state == SELECTED)
                    return true;
                break;
            default:
                connectionIO.sendMessage(tag + " BAD Command is not a valid command");
                return false;
        }

        connectionIO.sendMessage(tag + " BAD Command cannot be executed in this state");
        return false;
    }

    private List<Integer> getUidRange(String range) {
        /*
         * Possible cases :
         * number (only one UID)
         * number:number
         * number:*
         */
        List<Integer> uidList = new ArrayList<Integer>();

        if (!range.contains(":")) {
            if (isNumeric(range))
                // Nb
                uidList.add(Integer.parseInt(range));
            else
                return null;
        } else {
            String[] parts = range.split(":");
            if (isNumeric(parts[0]) && isNumeric(parts[1])) {
                // Nb - Nb
                int lower = Integer.parseInt(parts[0]);
                int higher = Integer.parseInt(parts[1]);
                for (Message message : selectedMailbox.getAllMessages()) {
                    if (message.getUid() >= lower && message.getUid() <= higher) {
                        uidList.add(message.getUid());
                    }
                }

            } else if (isNumeric(parts[0]) && parts[1].equals("*")) {
                // Nb - *
                for (Message message : selectedMailbox.getAllMessages()) {
                    if (message.getUid() >= Integer.parseInt(parts[0])) {
                        uidList.add(message.getUid());
                    }
                }
            } else {
                return null;
            }

        }

        return uidList;

    }

    private boolean isNumeric(String string) {
        return string != null && string.matches("\\d+");
    }
}
