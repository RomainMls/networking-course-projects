
/*
 * recoit un socket TCP
 * lit les commandes qui arrivent dessus
 * détecte le protocole en fct du port
 * => délègue au handler correspondant
 * ecris les reponses des handlers sur le canal
 * gère la fermeture du socket proprement (fermeture du read et write aussi)
 */
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Protocol protocol;

    public ClientHandler(Socket socket, Protocol protocol) {
        this.socket = socket;
        this.protocol = protocol;

    }

    @Override
    public void run() {

    }
}
