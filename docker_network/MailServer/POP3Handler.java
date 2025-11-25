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
        connectionIO.writeMessage("+OK POP3 server ready");
    }

    @Override
    public void handleCommand(String command) {
        if(command == null){
            connectionActive = false;
            return;
        }
        String[] split = command.split("\\s+");
        switch (split[0]){
            case "USER":
                if(split.length < 2 || seq != 0){
                    connectionIO.writeMessage("BAD");
                    return;
                }
                String username = split[1];
                user = new User(username);
                if(user.userExists())
                    connectionIO.writeMessage("+OK");
                else
                    connectionIO.writeMessage("BAD");

                seq = 1;
                return;

            case "PASS":
                if(seq != 1){
                    connectionIO.writeMessage("BAD");
                    return;
                }
                if(user.checkPassword(split[1]))
                    connectionIO.writeMessage("+OK Mailbox locked and ready");

                else
                    connectionIO.writeMessage("BAD");

                seq = 2;
                mailbox = MailStore.loadMailbox(user, "INBOX");
                return;

            case "STAT":
                if(seq != 2){
                    connectionIO.writeMessage("BAD");
                    return;
                }
                totalBytes = 0;
                for(Message message : messages = mailbox.getAllMessages()){
                    totalBytes += message.size();
                }
                connectionIO.writeMessage("+OK " + messages.size() + " " + totalBytes);
                seq = 3;
                return;

            case "LIST":
                if(seq != 3){
                    connectionIO.writeMessage("BAD");
                    return;
                }
                connectionIO.writeMessage("+OK " + messages.size() + " messages (" + totalBytes + " octects)");
                for(int i = 0; i < messages.size(); i++){
                    connectionIO.writeMessage((i+1) + " " + messages.get(i).size());
                }
                connectionIO.writeMessage(".");
                seq = 4;
                return;

            case "RETR":
                if(seq != 4 || split.length < 2){
                    connectionIO.writeMessage("BAD");
                    return;
                }
                int ref = Integer.parseInt(split[1]);
                Message msg = messages.get(ref-1);
                if(msg == null){
                    connectionIO.writeMessage("BAD");
                    return;
                }
                for(String line : msg.getDataLines())
                    connectionIO.writeMessage(line);

                connectionIO.writeMessage(".");
                return;

            case "DELE":
                if(seq != 4 || split.length < 2){
                    connectionIO.writeMessage("BAD");
                    return;
                }
                int ref2 = Integer.parseInt(split[1]);
                Message msg2 = messages.get(ref2-1);
                if(msg2 == null){
                    connectionIO.writeMessage("BAD");
                    return;
                }
                msg2.addFlag("\\Deleted");
                connectionIO.writeMessage("+OK Message marked for deletion");
                return;

            case "QUIT":
                mailbox.expunge();
                MailStore.expungeMailbox(user, "INBOX", mailbox);
                connectionIO.writeMessage("+OK Goodbye");
                connectionActive = false;
                return;
        }
    }
}
