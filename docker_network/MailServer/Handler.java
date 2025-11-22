/*
 * Super classe des handlers spécifiques
 * Définit le comportement Lire cmd => Gérer la commande => Lire cmd => Gérer la commande ...
 * Si command DATA par exemple qui nécessite plusieurs readLine, on lit DATA et puis le handler spécifique passera en mode lecture du contenu
 * On n'envoit pas la réponse ici car des fois la commande nécéssite plusieurs réponses (ex du RETR dans POP)
 * handleCommand gère les envois de réponses
 */
public abstract class Handler {
    protected ConnectionIO connectionIO;
    protected boolean connectionActive;

    public Handler(ConnectionIO connectionIO) {
        this.connectionIO = connectionIO;
        this.connectionActive = true;
    }

    public abstract void sendGreeting();

    public abstract void handleCommand(String command);

    public void handleSession() {
        sendGreeting();
        while (connectionActive) {
            String message = connectionIO.readLine();
            if (message == null)
                break;
            handleCommand(message);
        }
    }
}
