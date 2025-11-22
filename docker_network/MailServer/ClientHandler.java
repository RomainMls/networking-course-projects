
/*
 * recoit un socket TCP et un protocol
 * créé ConnectionIO pour les reader writer
 * => délègue au handler correspondant
 * gère la fermeture du socket proprement (fermeture du read et write aussi)
 */
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Protocol protocol;

    public ClientHandler(Socket socket, Protocol protocol) {
        this.socket = socket;
        this.protocol = protocol;
        try {
            socket.setSoTimeout(180000);
        } catch (SocketException e) {
            System.out.println("Client socket timeout Exception : " + e.getMessage());
        }
    }

    @Override
    public void run() {
        System.out.println("New " + protocol.toString() + " connection");

        ConnectionIO connectionIO = new ConnectionIO(socket);
        Handler handler = null;

        switch (protocol) {
            case SMTP:
                handler = new SMTPHandler(connectionIO);
                break;
            case POP3:
                handler = new POP3Handler(connectionIO);
                break;
            case IMAP:
                handler = new IMAPHandler(connectionIO);
                break;
            default:
                break;
        }
        if (handler != null)
            handler.handleSession();

        connectionIO.close();

    }
}
