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

    public ConnectionIO(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = socket.getOutputStream();
        } catch (IOException e) {
            System.err.println("Error while getting input stream: " + e.getMessage());
        }
    }

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
        System.out.println("Int:\t" + socket + ": " + line);
        return line;
    }

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
