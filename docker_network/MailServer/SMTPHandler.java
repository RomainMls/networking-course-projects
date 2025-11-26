
/*
 * détecte une commande, exécuter ce qu'elle doit faire, envoyer réponse
 * HELO, MAIL FROM, RCPT TO, DATA
 */

public class SMTPHandler extends Handler {
    private boolean ready;
    private String clientDomain;
    private String from;
    private Message currentMsg;

    public SMTPHandler(ConnectionIO connecionIO) {
        super(connecionIO);
    }

    @Override
    public void sendGreeting() {
        connectionIO.writeMessage("220 mail." + MailServer.getDomain() + " Service ready");
    }

    @Override
    public void handleCommand(String command) {
        if (command == null) {
            connectionActive = false;
            return;
        }

        String[] split = command.split("\\s+");
        if (split.length < 1) {
            connectionIO.writeMessage("BAD");
            return;
        }

        switch (split[0]) {
            case "HELO":
                if (split.length < 2) {
                    connectionIO.writeMessage("BAD");
                    return;
                }
                ready = true;
                clientDomain = split[1];
                connectionIO.writeMessage("250 " + MailServer.getDomain() + " greets " + clientDomain);
                return;

            case "DATA":
                if (from == null || currentMsg.getRcpts().isEmpty()) {
                    System.out.println("line 49");
                    connectionIO.writeMessage("BAD");
                    return;
                }

                connectionIO.writeMessage("354 End data with <CRLF>.<CRLF>");
                boolean reading = true;
                while (reading) {
                    String line = connectionIO.readLine();
                    if (line == null) {
                        connectionActive = false;
                        return;
                    }
                    if (line.equals("."))
                        reading = false;

                    else
                        currentMsg.addDataLine(line);
                }

                currentMsg.extractSubject();
                sendMessage(currentMsg);
                from = null;
                currentMsg = null;
                connectionIO.writeMessage("250 OK Message accepted for delivery");
                return;

            case "QUIT":
                connectionIO.writeMessage("221 Bye");
                connectionActive = false;
                return;
        }

        if (command.startsWith("MAIL FROM:")) {
            if (!ready) {
                connectionIO.writeMessage("BAD");
                return;
            }
            from = command.substring(command.indexOf(":") + 1);
            if (from == null) {
                connectionIO.writeMessage("BAD");
                return;
            }
            connectionIO.writeMessage("250 OK");
            currentMsg = new Message();
            currentMsg.setFrom(from);
            return;
        }
        if (command.startsWith("RCPT TO:")) {
            if (from == null) {
                System.out.println("line 92");
                connectionIO.writeMessage("BAD");
                return;
            }
            String rcpt = command.substring(command.indexOf(":") + 1);
            if (rcpt == null) {
                System.out.println("line 98");
                connectionIO.writeMessage("BAD");
                return;
            }
            currentMsg.addRcpt(rcpt);
            connectionIO.writeMessage("250 OK");
            return;
        }
        connectionIO.writeMessage("BAD");
        return;
    }

    private void sendMessage(Message msg) {
        for (String rcpt : currentMsg.getRcpts()) {
            User user = new User(rcpt);
            String domain = user.getUserDomain();
            if (domain == null || !user.userExists()) {
                System.out.println("Skipping user: " + user);
                continue;
            }

            if (MailServer.getDomain().equals(domain)) {
                System.out.println("Saving message localy for user: " + user);
                Mailbox userMailbox = MailStore.loadMailbox(user, "INBOX");
                userMailbox.addMessage(msg);
                MailStore.saveMailbox(user, "INBOX", userMailbox);
            } else {
                System.out.println("Transmitting message to: " + user);
                System.out.println("Transmittion disabled");
                if (true)
                    return;

                SMTPTransmitter ts = new SMTPTransmitter(domain);
                ts.forward(msg, rcpt);
            }
        }
    }
}
