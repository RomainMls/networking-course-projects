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
            System.out.println("Error while getting input stream: " + e.getMessage());
        }
    }

    public String readLine() {

        String line = null;
        try {
            line = in.readLine();
            if (line == null) // Connection is closed
                return null;

        } catch (SocketTimeoutException e) {
            System.out.println("Client silent too long time: " + e.getMessage());
            return null;
        } catch (IOException e) {
            System.out.println("Error while reading input stream: " + e.getMessage());
            return null;
        }
        return line;
    }

    public void writeMessage(String message) {
        try {
            message += "\r\n";
            out.write(message.getBytes());
            out.flush();
        } catch (IOException e) {
            System.out.println("Error while sending bytes through the socket: " + e.getMessage());
        }
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
            System.out.println("Error while closing connection" + e.getMessage());
        }
    }

}
