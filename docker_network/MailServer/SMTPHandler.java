/*
 * détecte une commande, exécuter ce qu'elle doit faire, envoyer réponse
 * HELO, MAIL FROM, RCPT TO, DATA
 */

public class SMTPHandler extends Handler {
    private ConnectionIO connectionIO;

    public SMTPHandler(ConnectionIO connecionIO) {
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