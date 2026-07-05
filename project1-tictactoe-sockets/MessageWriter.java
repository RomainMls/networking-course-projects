import java.io.*;
import java.net.*;

// Class to send messages through a socket output stream
public class MessageWriter {
    private OutputStream out;

    // Initializes MessageWriter by creating an OutputStream from the given socket
    public MessageWriter(Socket socket) {
        try {
            out = socket.getOutputStream();
        } catch (IOException e) {
            System.out.println("Error while getting output stream from the socket: " + e.getMessage());
        }
    }

    /*
     * Sends a complete message by respecting the Tic tac toe protocol
     * Adds empty line at the end and flushes the stream to ensure delivery
     */
    public void writeMessage(String message) {
        try {
            message += "\r\n\r\n";
            out.write(message.getBytes());
            out.flush();
        } catch (IOException e) {
            System.out.println("Error while sending bytes through the socket: " + e.getMessage());
        }
    }
}
