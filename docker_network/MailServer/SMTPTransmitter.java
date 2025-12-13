/*
 * Agit comme un client au sein de notre serveur pour envoyer du SMTP vers un
 * autre serveur
 * Ex: mail arrive chez mail.gembloux.uliege mais destinaire mail.info.uliege,
 * mail.gembloux.uliege doit envoyer vers mail.uliege avant (qui agit comme
 * serveur centrale)
 * et mail.uliege renverra vers mail.info.uliege
 * Ca doit d'abord passer par mail.uliege donc faut que les serveurs agissent
 * comme client SMTP
 * 
 * Ouvre un port, envoit les cmds SMTP, ferme la co
 * De ce que j'ai compris, si on a Client -> Serveur A -> Serveur B -> Serveur C
 * Client parle à A, et A répond, A parle à B et B répond,... (rejoue la
 * conversation)
 */

import java.io.IOException;
import java.net.Socket;

public class SMTPTransmitter {

    /*
     * Transfers a message to a remote host (given by IP) via SMTP.
     * The message recipient is given by rcpt.
     */
    public static void forward(Message msg, String rcpt, String IP){
        try {
            Socket socket = new Socket(IP, 25);
            ConnectionIO connectionIO = new ConnectionIO(socket);

            // Lire le greeting
            connectionIO.readLine();

            connectionIO.sendMessage("HELO " + MailServer.getDomain());
            connectionIO.readLine();

            connectionIO.sendMessage("MAIL FROM:<" + msg.getFrom() + ">");
            connectionIO.readLine();

            connectionIO.sendMessage("RCPT TO:<" + rcpt + ">");
            connectionIO.readLine();

            connectionIO.sendMessage("DATA");
            connectionIO.readLine();

            for (String dataLine : msg.getDataLines())
                connectionIO.sendMessage(dataLine);

            connectionIO.sendMessage(".");
            connectionIO.readLine();

            connectionIO.sendMessage("QUIT");
            connectionIO.readLine();

            connectionIO.close();

        } catch (IOException e) {
            System.err.println("Error while SMTP forwarding : " + e.getMessage());
        }
    }
}

