/*
 * détecte une commande, exécuter ce qu'elle doit faire, envoyer réponse
 * USER, PASS (appelle User pour vérifier)
 * STAT, LIST, RETR, DELE, QUIT
 * 
 */

public class POP3Handler extends Handler {

    public POP3Handler(ConnectionIO connecionIO) {
        super(connecionIO);
    }

    @Override
    public void sendGreeting() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendGreeting'");
    }

    @Override
    public void handleCommand(String command) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleCommand'");
    }
}