/*
 * Ecouter les 3 ports 25 (SMTP), 110 (POP3), 143 (IMAP)
 * Creer un thread pool
 * ecoute en boucle les co entrantes
 * pour chaque co, crée un ClientHandler dans un nouveau thread
 */

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MailServer {
    public static void main(String[] args) {
        final String DOMAIN = args[0];
        final int MAXTHREADS = Integer.valueOf(args[1]);

        ExecutorService threadPool = Executors.newFixedThreadPool(MAXTHREADS);

        // Threads listening on each dedicated ports
        Thread SMTPThread = createThread(Protocol.SMTP, threadPool);
        Thread POP3Thread = createThread(Protocol.POP3, threadPool);
        Thread IMAPThread = createThread(Protocol.IMAP, threadPool);

        SMTPThread.start();
        POP3Thread.start();
        IMAPThread.start();
    }

    private static Thread createThread(Protocol protocol, ExecutorService threadPool) {
        Thread thread = new Thread() {
            public void run() {
                ServerSocket serverSocket = null;
                try {
                    serverSocket = new ServerSocket(protocol.getPort());
                    while (true) {

                        Socket clientSocket = serverSocket.accept();

                        ClientHandler clientHandler = new ClientHandler(clientSocket, protocol);
                        threadPool.execute(clientHandler);

                    }
                } catch (IOException e) {
                    System.out.println("Could not start" + protocol.toString() + "server: " + e.getMessage());
                }
            }
        };
        return thread;
    }
}