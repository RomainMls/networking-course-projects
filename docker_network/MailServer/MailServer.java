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
    private static String DOMAIN;
    private static int MAXTHREADS;

    public static void main(String[] args) {
        DOMAIN = args[0];
        MAXTHREADS = Integer.valueOf(args[1]);

        System.out.println("Starting mailserver: " + DOMAIN);
        ExecutorService threadPool = Executors.newFixedThreadPool(MAXTHREADS);

        // Threads listening on each dedicated ports
        Thread SMTPThread = createThread(Protocol.SMTP, threadPool);
        Thread POP3Thread = createThread(Protocol.POP3, threadPool);
        Thread IMAPThread = createThread(Protocol.IMAP, threadPool);

        SMTPThread.start();
        POP3Thread.start();
        IMAPThread.start();
    }

    /*
     * Create a runnable thread that accepts and handles client connection 
     * for the given protocol. The threads created for each client connection
     * are executed by the threadpool.
     */
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
                    System.err.println("Could not start " + protocol.toString() + " server: " + e.getMessage());
                }
            }
        };
        return thread;
    }

    // return the domain of the running server
    public static String getDomain() {
        return DOMAIN;
    }
}

