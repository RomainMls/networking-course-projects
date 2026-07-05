import java.io.*;
import java.net.*;

//Class to read messages sent through a socket input stream
public class MessageReader {
    private BufferedReader in;
    private StringBuilder buffer;

    // Initializes MessageReader by creating a BufferedReader from the given socket
    public MessageReader(Socket socket) {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Error while getting input stream: " + e.getMessage());
        }
        buffer = new StringBuilder();
    }

    /*
     * Reads a complete message until an empty line is received
     * Returns the message as a string without extra CRLF characters
     */
    public String readMessage() {
        buffer.setLength(0); // Clears the previous message content before reading a new one

        String line = null;
        do {
            try {
                line = in.readLine();
                if (line == null) // Connection is closed
                    return null;
                else {
                    buffer.append(line).append("\r\n");
                }
            } catch (SocketTimeoutException e) {
                System.out.println("Client silent too long time: " + e.getMessage());
                return null;
            } catch (IOException e) {
                System.out.println("Error while reading input stream: " + e.getMessage());
                return null;
            }
        } while (line != null && !line.isEmpty());

        // Delete \r and \n at the end of the messsage
        return buffer.toString().replaceAll("[\\r\\n]+$", "");
    }
}
