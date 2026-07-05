import java.io.*;
import java.net.*;

//Class to handle communication with a client 
public class ClientHandler extends Thread {
    private Socket socket;
    private MessageReader reader;
    private MessageWriter writer;
    private char symbol;
    private Game game;
    private BotPlayer bot = null;
    private Matchmaking matchmaking = null;

    /*
     * Initializes a new client handler for the given socket and with the
     * matchmaking system (if he wishes to play multiplayer later)
     */
    public ClientHandler(Socket socket, Matchmaking matchmaking) {
        this.socket = socket;
        this.matchmaking = matchmaking;
        reader = new MessageReader(socket);
        writer = new MessageWriter(socket);
        try {
            socket.setSoTimeout(180000);
        } catch (SocketException e) {
            System.out.println("Client socket timeout Exception : " + e.getMessage());
        }
    }

    /*
     * Runs the main loop that listens, handles client requests and
     * sends it a response
     */
    @Override
    public void run() {
        try {
            System.out.println("New client connected");

            while (true) {
                String request = reader.readMessage();
                if (request == null) {
                    break;
                }

                String response = handleRequest(request);
                if (response == null)
                    break;
                else
                    writer.writeMessage(response);
            }
        } catch (Exception e) {
            System.out.println("Error in clientHandler: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    // Sets the player's symbol (X or O)
    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }

    // Links a game instance to the client
    public void setGame(Game game) {
        this.game = game;
    }

    // Returns the player's symbol
    public char getSymbol() {
        return symbol;
    }

    // Returns the game instance associated with the client
    public Game getGame() {
        return game;
    }

    // Takes a client request as input and gives the appropriated response
    private String handleRequest(String request) {
        String response = null;
        String[] reqStrings = request.trim().split(" ");

        if (request.startsWith("START BOT")) {
            response = handleStartBot(reqStrings);

        } else if (request.equals("START PLAYER")) {
            response = handleStartPlayer();

        } else if (request.startsWith("PUT")) {
            response = handlePut(reqStrings);

        } else if (request.equals("UPDATE")) {
            response = handleUpdate();

        } else if (request.equals("QUIT")) {
            response = handleQuit();

        } else {
            response = "WRONG";
        }

        return response;
    }

    /*
     * Starts a singleplayer game against a bot with the symbol player given in the
     * request and returns the response
     */
    private String handleStartBot(String[] reqStrings) {
        if (reqStrings.length != 3 || reqStrings[2].length() != 1
                || (reqStrings[2].charAt(0) != 'X' && reqStrings[2].charAt(0) != 'O'))
            return "WRONG";
        else {
            this.matchmaking = null;
            this.game = new Game();
            if (reqStrings[2].charAt(0) == 'O') {
                this.symbol = 'O';
                bot = new BotPlayer(game, 'X');
                bot.makeMove();
            } else {
                this.symbol = 'X';
                bot = new BotPlayer(game, 'O');
            }

            return game.gridToString();
        }
    }

    /*
     * Requests a multiplayer game by sending a request to the matchmaking
     * And returns an appropriated response
     */
    private String handleStartPlayer() {
        return matchmaking.requestGame(this);
    }

    /*
     * Handles the PUT command and returns an appropriated response
     */
    private String handlePut(String[] reqStrings) {
        if (game == null) {
            return "NO GAME MODE";
        } else if (reqStrings.length != 3 || !isNumeric(reqStrings[1]) || !isNumeric(reqStrings[2]))
            return "WRONG";

        else {
            MoveResult result = game.playMove(Integer.parseInt(reqStrings[1]), Integer.parseInt(reqStrings[2]),
                    symbol);
            if (result == MoveResult.CELL_OCCUPIED || result == MoveResult.INVALID_RANGE
                    || result == MoveResult.NOT_YOUR_TURN || result == MoveResult.GAME_FINISHED)
                return result.toString();
            else {
                String status = game.checkWin().toString();
                if (!game.isFinished()) {
                    if (bot != null)
                        bot.makeMove();
                    status = game.checkWin().toString();
                }

                if (!status.isEmpty())
                    return game.gridToString() + "\r\n" + status;
                else
                    return game.gridToString();
            }
        }
    }

    /*
     * Handles the update command and returns the current game grid (with its status
     * if there is)
     */
    private String handleUpdate() {
        if (game == null) {
            return "NO GAME MODE";
        } else {
            String status = game.checkWin().toString();
            if (!status.isEmpty())
                return game.gridToString() + "\r\n" + status;
            else
                return game.gridToString();
        }
    }

    /*
     * Handles the quit command
     * Returns null to mean that in the main loop we have to leave it and close the
     * connection
     */
    private String handleQuit() {
        if (matchmaking != null)
            matchmaking.removeWaitingPlayer(this);
        if (game != null && matchmaking != null) {
            game.removePlayer(this);
            game.setFinished();
        }
        
        return null;
    }

    /*
     * Closes the socket connection with the client
     */
    private void closeConnection() {
        if (socket != null)
            try {
                socket.close();
                System.out.println("Client disconnected");
            } catch (IOException e) {
                System.out.println("Error while closing a socket client connection : " + e.getMessage());
            }
    }

    /*
     * Checks if given string represents a numeric value
     */
    private boolean isNumeric(String string) {
        return string != null && string.matches("\\d+");
    }
}