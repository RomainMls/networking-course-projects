import java.io.IOException;
import java.net.*;

/*
 * Main server class that accepts client connections (new scoket) and instanciates a new handler for each one
 */
public class TictactoeServer {

    // Main method of the server program
    public static void main(String[] args) {
        final int PORT = 2791;

        ServerSocket serveurSocket = null;
        try {
            serveurSocket = new ServerSocket(PORT);

            Matchmaking matchmaking = new Matchmaking();

            // Infinite loop to accept new client connections
            while (true) {
                Socket clientSocket = serveurSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, matchmaking);
                clientHandler.start(); // Creates a new thread for each client
            }

        } catch (IOException e) {
            System.out.println("Could not start server: " + e.getMessage());
        } finally {
            // Closes the server socket
            if (serveurSocket != null) {
                try {
                    serveurSocket.close();
                } catch (IOException e) {
                    System.out.println("Error while closing server socket: " + e.getMessage());
                }
            }
        }
    }
}