/*
 * détecte une commande, exécuter ce qu'elle doit faire, envoyer réponse
 * HELO, MAIL FROM, RCPT TO, DATA
 */
import java.util.ArrayList;
import java.io.*;

public class SMTPHandler extends Handler {
    private boolean ready;
    private String clientDomain;
    private String from;
    private ArrayList<String> rcpts;
    private boolean reading;
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
        if(command == null){
            connectionActive = false;
            return;
        }

        String[] split = command.split("\\s+");
        if(split.length < 1){
            connectionIO.writeMessage("BAD");
            return;
        }
        if(reading){
            if(command.equals(".")){
                sendMessage(currentMsg);
                reading = false;
                from = null;
                rcpts = null;
                currentMsg = null;
                connectionIO.writeMessage("250 OK Message accepted for delivery");
                return;
            }
            currentMsg.addDataLine(command);
            return;
        }

        switch(split[0]){
            case "HELO":
                if (split.length < 2){
                    connectionIO.writeMessage("BAD");
                    return;
                }
                ready = true;
                clientDomain = split[1];
                connectionIO.writeMessage("250 " + MailServer.getDomain() + " greets " + clientDomain);
                return;

            case "DATA":
                if(from == null || rcpts == null || rcpts.isEmpty()){
                    connectionIO.writeMessage("BAD");
                    return;
                }
                connectionIO.writeMessage("354 End data with <CRLF>.<CRLF>");
                currentMsg = new Message();
                currentMsg.setFrom(from);
                for(String rcpt : rcpts){
                    currentMsg.addRcpt(rcpt);
                }
                reading = true;
                return;

            case "QUIT":
                connectionIO.writeMessage("221 Bye");
                connectionActive = false;
                return;
        }

        if(command.startsWith("MAIL FROM:")){
            if(!ready){
                connectionIO.writeMessage("BAD");
                return;
            }
            from = command.substring(command.indexOf(":")+1);
            if (from == null){
                connectionIO.writeMessage("BAD");
                return;
            }
            connectionIO.writeMessage("250 OK");
            rcpts = new ArrayList<>();
            return;
        }
        if(command.startsWith("RCPT TO:")){
            if(from == null){
                connectionIO.writeMessage("BAD");
                return;
            }
            String rcpt = command.substring(command.indexOf(":")+1);
            if (rcpt == null){
                connectionIO.writeMessage("BAD");
                return;
            }
            rcpts.add(rcpt);
            connectionIO.writeMessage("250 OK");
            return;
        }
        connectionIO.writeMessage("BAD");
        return;
    }

    private void sendMessage(Message msg){
        for(String rcpt : rcpts){
            User user = new User(rcpt);
            String domain = user.getUserDomain();
            if(domain == null || !user.userExists()){
                System.out.println("Skipping user: " + user);
                continue;
            }

            if(MailServer.getDomain().equals(domain)){
                System.out.println("Saving message localy for user: " + user);
                Mailbox userMailbox = MailStore.loadMailbox(user, "INBOX");
                userMailbox.addMessage(msg);
                MailStore.saveMailbox(user, "INBOX", userMailbox);
            }
            else{
                System.out.println("Transmitting message to: " + user);
                System.out.println("Transmittion disabled");
                if(true)
                    return;

                SMTPTransmitter ts = new SMTPTransmitter(domain);
                ts.forward(msg, rcpt);
            }
        }
    }
}
