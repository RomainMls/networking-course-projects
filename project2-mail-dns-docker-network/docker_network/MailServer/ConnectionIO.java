import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ConnectionIO {
    private BufferedReader in;
    private OutputStream out;
    private Socket socket;

    /*
     * Represents a IO handler for a single connection socket.
     * Handles the dedicated input reader and output stream.
     * Not thread safe.
     */
    public ConnectionIO(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = socket.getOutputStream();
        } catch (IOException e) {
            System.err.println("Error while getting input stream: " + e.getMessage());
        }
    }

    /*
     * Reads a single line from the connection, blocking until available or connection closed.
     * In the latter case, the value of returned is null.
     */
    public String readLine() {

        String line = null;
        try {
            line = in.readLine();
            if (line == null) // Connection is closed
                return null;

        } catch (SocketTimeoutException e) {
            System.err.println("Client silent too long time: " + e.getMessage());
            return null;
        } catch (IOException e) {
            System.err.println("Error while reading input stream: " + e.getMessage());
            return null;
        }
        System.out.println("In:\t" + socket + ": " + line);
        return line;
    }

    /*
     * Sends a message on the connection socket and additionally adds a CRLF.
     * One side effect is that it flushed the socket's output stream.
     */
    public void sendMessage(String message) {
        try {
            message += "\r\n";
            out.write(message.getBytes());
            out.flush();
        } catch (IOException e) {
            System.err.println("Error while sending bytes through the socket: " + e.getMessage());
            return;
        }
        System.out.println("Out:\t" + socket + ": " + message);
    }

    /*
     * Properly closes the connection socket and associated resources
     */
    public void close() {
        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            System.err.println("Error while closing connection" + e.getMessage());
        }
    }

}

