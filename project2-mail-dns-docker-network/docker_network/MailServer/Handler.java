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

    /*
     * Provides a session loop that handles a client's input according to a protocol.
     */
    public Handler(ConnectionIO connectionIO) {
        this.connectionIO = connectionIO;
        this.connectionActive = true;
    }

    /*
     * Sends the initial protocol greeting required to initiate the session.
     */
    public abstract void sendGreeting();

    /*
     * Processes a single protol command, responds to client and updates
     * handler state accordingly.
     */
    public abstract void handleCommand(String command);

    /*
     * Drives the entire lifecycle of the client's session
     * - Sends the initial greeting
     * - Continuously reads messages from the client and forwards the message to handleCommand
     * - Terminates the loop if the client closes the connection or if the connection is inactive
     */
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

