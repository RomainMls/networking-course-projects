import java.io.*;
import java.net.*;
import java.util.Scanner;

// Client program to play Tic-tac-toe in singleplayer or multiplayer mode
public class TictactoeClient {

    // Main method of the client program
    public static void main(String[] args) {
        final int PORT = 2791;

        Socket socket = null;
        try {
            socket = new Socket("localhost", PORT);
        } catch (IOException e) {
            System.out.println("Error while creating clientSocket: " + e.getMessage());
            return;
        }

        System.out.println("Welcome to Tictactoe");

        Scanner playerInputReader = new Scanner(System.in);
        MessageWriter messageWriter = new MessageWriter(socket);
        MessageReader messageReader = new MessageReader(socket);

        String mode = selectMode(playerInputReader);
        if (mode.equals("1")) {
            String symbol = chooseSymbol(playerInputReader);
            messageWriter.writeMessage("START BOT " + symbol);
        } else {
            messageWriter.writeMessage("START PLAYER");
        }

        String response = messageReader.readMessage();
        if (response != null && !response.isEmpty())
            System.out.println(response);

        playGame(playerInputReader, messageReader, messageWriter, mode);

        playerInputReader.close();
        if (socket != null)
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error while closing socket on client side: " + e.getMessage());
            }
    }

    /*
     * Asks the user to choose between bot mode or multiplayer mode
     * Returns "1" for bot or "2" for player
     */
    private static String selectMode(Scanner playerInputReader) {
        System.out.println("Select the game mode\n1) Bot\n2) Player");
        String mode = "";
        while (!mode.equals("1") && !mode.equals("2")) {
            mode = playerInputReader.nextLine().trim();

            if (!mode.equals("1") && !mode.equals("2"))
                System.out.println("Invalid choice. Please type 1 or 2");
        }
        return mode;
    }

    /*
     * Asks the user to choose a symbol (X or O)
     * Returns the selected symbol in uppercase
     */
    private static String chooseSymbol(Scanner playerInputReader) {
        String symbol = "";
        while (!symbol.equals("X") && !symbol.equals("O")) {
            System.out.println("Choose your symbol X or O:");
            symbol = playerInputReader.nextLine().trim().toUpperCase();

            if (!symbol.equals("X") && !symbol.equals("O"))
                System.out.println("Invalid choice. Please type X or O");
        }
        return symbol;
    }

    /*
     * Main game loop that manages user inputs and server responses
     * Ends when the player quits or is disconnected
     */
    private static void playGame(Scanner playerInputReader, MessageReader messageReader, MessageWriter messageWriter,
            String mode) {
        boolean finished = false;
        while (true) {
            if (!finished)
                if (mode.equals("1"))
                    System.out.println("\r\nPlace your symbol on the grid: 'row' 'col'\nQuit: q");
                else
                    System.out.println("\r\nPlace your symbol on the grid: 'row' 'col'\nQuit: q\nUpdate: u");
            String request = playerInputReader.nextLine().trim();

            int action = handleUserInput(request, mode, messageWriter);
            if (action == 1)
                break;
            if (action == -1)
                continue;

            String response = messageReader.readMessage();
            if (response == null)
                break;
            System.out.println(response);

            if (isGameOver(response)) {
                System.out.println("Game over: type 'q' to quit");
                finished = true;
            }
        }
    }

    /*
     * Handles the player's input and sends the corresponding command to the
     * server
     * Returns 1 if the player quits, -1 for invalid input, or 0 otherwise
     */
    private static int handleUserInput(String request, String mode, MessageWriter messageWriter) {
        if (request.equals("q")) {
            messageWriter.writeMessage("QUIT");
            System.out.println("You left the game");
            return 1;
        }

        if (mode.equals("2") && request.equals("u")) {
            messageWriter.writeMessage("UPDATE");
            return 0;

        } else {
            String[] rowColStrings = request.split(" ");
            if (rowColStrings.length == 2 && isNumeric(rowColStrings[0]) && isNumeric(rowColStrings[1])) {
                messageWriter.writeMessage("PUT " + rowColStrings[0] + " " + rowColStrings[1]);
                return 0;
            } else {
                System.out.println("Invalid input: please type 'row' 'col' or 'q' or 'u'(in multiplayer mode)");
                return -1;
            }
        }
    }

    /*
     * Checks if response indicates that the game is over
     * Returns true if the response contains a final game status keyword
     */
    private static boolean isGameOver(String response) {
        if (response.contains("WON") || response.contains("DRAW") || response.contains("OPPONENT QUIT")
                || response.contains("GAME FINISHED"))
            return true;
        return false;
    }

    /*
     * Checks if given string represents a numeric value
     */
    private static boolean isNumeric(String string) {
        return string != null && string.matches("\\d+");
    }
}