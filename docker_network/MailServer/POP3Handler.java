
/*
 * détecte une commande, exécuter ce qu'elle doit faire, envoyer réponse
 * USER, PASS (appelle User pour vérifier)
 * STAT, LIST, RETR, DELE, QUIT
 * 
 */
import java.util.List;

public class POP3Handler extends Handler {
    private int seq = 0;
    private User user;
    private Mailbox mailbox;
    private List<Message> messages;
    private int totalBytes;

    public POP3Handler(ConnectionIO connecionIO) {
        super(connecionIO);
    }

    @Override
    public void sendGreeting() {
        connectionIO.sendMessage("+OK POP3 server ready");
    }

    @Override
    public void handleCommand(String command) {
        if (command == null) {
            connectionActive = false;
            return;
        }
        String[] split = command.split("\\s+");
        switch (split[0]) {
            case "USER":
                if (split.length < 2 || seq != 0) {
                    connectionIO.sendMessage("BAD");
                    return;
                }
                String username = split[1];
                user = new User(username);
                if (user.userExists())
                    connectionIO.sendMessage("+OK");
                else {
                    connectionIO.sendMessage("BAD");
                    return;
                }
                seq = 1;
                return;

            case "PASS":
                if (seq != 1) {
                    connectionIO.sendMessage("BAD");
                    return;
                }
                if (user.checkPassword(split[1]))
                    connectionIO.sendMessage("+OK Mailbox locked and ready");

                else {
                    connectionIO.sendMessage("BAD");
                    return;
                }

                seq = 2;
                mailbox = MailStore.loadMailbox(user, "INBOX");
                return;

            case "STAT":
                if (seq != 2) {
                    connectionIO.sendMessage("BAD");
                    return;
                }
                totalBytes = 0;
                for (Message message : messages = mailbox.getAllMessages()) {
                    totalBytes += message.size();
                }
                connectionIO.sendMessage("+OK " + messages.size() + " " + totalBytes);
                seq = 3;
                return;

            case "LIST":
                if (seq != 3) {
                    connectionIO.sendMessage("BAD");
                    return;
                }
                connectionIO.sendMessage("+OK " + messages.size() + " messages (" + totalBytes + " octets)");
                for (int i = 0; i < messages.size(); i++) {
                    connectionIO.sendMessage((i + 1) + " " + messages.get(i).size());
                }
                connectionIO.sendMessage(".");
                seq = 4;
                return;

            case "RETR":
                if (seq != 4 || split.length < 2) {
                    connectionIO.sendMessage("BAD");
                    return;
                }
                int ref = Integer.parseInt(split[1]) - 1;
                if (ref < 0 || ref >= messages.size()) {
                    connectionIO.sendMessage("BAD");
                    return;
                }
                Message msg = messages.get(ref);
                for (String line : msg.getDataLines())
                    connectionIO.sendMessage(line);

                connectionIO.sendMessage(".");
                return;

            case "DELE":
                if (seq != 4 || split.length < 2) {
                    connectionIO.sendMessage("BAD");
                    return;
                }
                int ref2 = Integer.parseInt(split[1]) - 1;
                if (ref2 < 0 || ref2 >= messages.size()) {
                    connectionIO.sendMessage("BAD");
                    return;
                }
                Message msg2 = messages.get(ref2);
                msg2.addFlag("\\Deleted");
                connectionIO.sendMessage("+OK Message marked for deletion");
                return;

            case "QUIT":
                connectionIO.sendMessage("+OK Goodbye");
                if (mailbox != null) {
                    mailbox.expunge();
                    MailStore.expungeMailbox(user, mailbox);
                }
                connectionActive = false;
                return;

            case "CAPA":
                connectionIO.sendMessage("+OK CAPA\r\nUSER\r\nPASS\r\nSTAT\r\nLIST\r\nRETR\r\nDELE\r\nQUIT\r\n.");
                return;

            case "UIDL":
                if (seq != 4) {
                    connectionIO.sendMessage("BAD");
                    return;
                }
                connectionIO.sendMessage("+OKIDs\r\n.");
                return;
        }
        connectionIO.sendMessage("BAD");
    }
}
