public class SMTPHandler extends Handler {
    private boolean ready;
    private String clientDomain;
    private String from;
    private Message currentMsg;

    /*
     * Provides a session loop that handles a client's input according to the SMTP protocol.
     */
    public SMTPHandler(ConnectionIO connecionIO) {
        super(connecionIO);
    }

    /*
     * Sends the initial SMTP greeting required to initiate the session.
     */
    @Override
    public void sendGreeting() {
        connectionIO.sendMessage("220 mail." + MailServer.getDomain() + " Service ready");
    }

    /*
     * Processes a single SMTP command, responds to client and updates
     * handler state accordingly.
     */
    @Override
    public void handleCommand(String command) {
        if (command == null) {
            connectionActive = false;
            return;
        }

        // split the command by spaces
        String[] split = command.split("\\s+");
        if (split.length < 1) {
            connectionIO.sendMessage("BAD");
            return;
        }

        switch (split[0]) {
            case "HELO":
            case "EHLO":
                if (split.length < 2) {
                    connectionIO.sendMessage("BAD");
                    return;
                }
                ready = true;
                clientDomain = split[1];
                connectionIO.sendMessage("250 " + MailServer.getDomain() + " greets " + clientDomain);
                return;

            case "DATA":
                if (from == null || currentMsg.getRcpts().isEmpty()) {
                    System.out.println("line 49");
                    connectionIO.sendMessage("BAD");
                    return;
                }

                connectionIO.sendMessage("354 End data with <CRLF>.<CRLF>");
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
                connectionIO.sendMessage("250 OK Message accepted for delivery");
                return;

            case "QUIT":
                connectionIO.sendMessage("221 Bye");
                connectionActive = false;
                return;
        }

        if (command.startsWith("MAIL FROM:<") && command.endsWith(">")) {
            if (!ready) {
                connectionIO.sendMessage("BAD");
                return;
            }
            from = command.substring(command.indexOf("<") + 1, command.indexOf(">"));
            if (from == null) {
                connectionIO.sendMessage("BAD");
                return;
            }
            connectionIO.sendMessage("250 OK");
            currentMsg = new Message();
            currentMsg.setFrom(from);
            return;
        }
        if (command.startsWith("RCPT TO:<") && command.endsWith(">")) {
            if (from == null) {
                System.out.println("line 92");
                connectionIO.sendMessage("BAD");
                return;
            }
            String rcpt = command.substring(command.indexOf("<") + 1, command.indexOf(">"));
            if (rcpt == null) {
                System.out.println("line 98");
                connectionIO.sendMessage("BAD");
                return;
            }
            currentMsg.addRcpt(rcpt);
            connectionIO.sendMessage("250 OK");
            return;
        }
        // command not identified
        connectionIO.sendMessage("BAD");
        return;
    }

    /*
     * Sends the provided message to all of its recipients.
     * If a recipient is local, the message is stored to that person's mailbox.
     * If a recipient is from a remote domain, the message is forwarded to the correct server.
     */
    private void sendMessage(Message msg) {
        for (String rcpt : currentMsg.getRcpts()) {
            User user = new User(rcpt);
            String domain = user.getUserDomain();
            if(domain == null)
                continue;

            if (MailServer.getDomain().equals(domain)) {
                if (!user.userExists()) {
                    System.out.println("User: " + user + " does not exist for this domain");
                    continue;
                }

                System.out.println("Saving message localy for user: " + user);
                Mailbox userMailbox = MailStore.loadMailbox(user, "INBOX");
                userMailbox.addMessage(msg);
                MailStore.saveMailbox(user, userMailbox);
            } else {
                // we need to forward
                String destinationIP;
                switch(domain){
                    case "uliege.be":
                        destinationIP = "10.0.1.7";
                        break;

                    case "gembloux.uliege.be":
                        destinationIP = "10.0.2.7";
                        break;

                    case "info.uliege.be":
                        destinationIP = "10.0.3.7";
                        break;

                    default:    // when the domain is outside of the virtual network
                        if(MailServer.getDomain() == "uliege.be"){
                            destinationIP = DomainResolver.resolveDomain(domain);
                            if(destinationIP == null){
                                System.err.println("Couldn't resolve IP for domain: " + domain);
                                continue;
                            }
                        }
                        else{
                            destinationIP = "10.0.1.7";
                        }

                }
                // send message with 'destinationIP'
                SMTPTransmitter.forward(msg, rcpt, destinationIP);
            }
        }
    }
}

