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
    private String localDomain;

    public SMTPTransmitter(String domain) {
        System.out.println("new SMTPHandler for domain: " + domain);
        this.localDomain = domain;
    }

    public void forward(Message message, String rcpt) {
        if (!rcpt.contains("@")){
            System.err.println("Can't forward following adress: " + rcpt);
            return;
        }

        String rcptDomain;
        String[] parts = rcpt.split("@");
        if (parts.length == 2)
            rcptDomain = parts[1];

        else{
            System.err.println("Can't forward following adress: " + rcpt);
            return;
        }

        System.out.println("Trying to resolve adress: " + rcpt);
        String host = DomainResolver.resolveSmtpServer(rcptDomain);
        if (host == null){
            System.err.println("Failed to resolve adress: " + rcpt);
            return;
        }

        try {
            Socket socket = new Socket(host, 25);
            ConnectionIO connectionIO = new ConnectionIO(socket);

            // Lire le greeting
            connectionIO.readLine();

            connectionIO.sendMessage("HELO " + localDomain);
            connectionIO.readLine();

            connectionIO.sendMessage("MAIL FROM:<" + message.getFrom() + ">");
            connectionIO.readLine();

            connectionIO.sendMessage("RCPT TO:<" + rcpt + ">");
            connectionIO.readLine();

            connectionIO.sendMessage("DATA");
            connectionIO.readLine();

            for (String dataLine : message.getDataLines())
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
