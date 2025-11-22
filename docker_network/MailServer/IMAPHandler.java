/*
 * détecte une commande, exécuter ce qu'elle doit faire, envoyer réponse
 * LOGIN, LIST, SELECT
 * UID FETCH
 * FLAGS
 * EXPUNGE
 * 
 */

public class IMAPHandler extends Handler {

    public IMAPHandler(ConnectionIO connecionIO) {
        super(connecionIO);
    }

    @Override
    public void sendGreeting() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendGreeting'");
    }

    @Override
    public String handleCommand(String command) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleCommand'");
    }

    @Override
    public void sendResponse(String command) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendResponse'");
    }
}