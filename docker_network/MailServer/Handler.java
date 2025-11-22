/*
 * Super classe des handlers spécifiques
 * Définit le comportement Lire cmd => Gérer la commande => Répondre => Lire cmd => Gérer la commande => Répondre ...
 * Si command DATA par exemple qui nécessite plusieurs readLine, on lit DATA et puis le handler spécifique passera en mode lecture du contenu
 */
public abstract class Handler {
    protected ConnectionIO connectionIO;
    protected boolean connectionActive;

    public Handler(ConnectionIO connectionIO) {
        this.connectionIO = connectionIO;
        this.connectionActive = true;
    }

    public abstract void sendGreeting();

    public abstract String handleCommand(String command);

    public abstract void sendResponse(String message);

    public void handleSession() {
        sendGreeting();
        while (connectionActive) {
            String message = connectionIO.readLine();
            if (message == null)
                break;
            String response = handleCommand(message);
            if (response != null)
                sendResponse(response);
        }
    }
}
