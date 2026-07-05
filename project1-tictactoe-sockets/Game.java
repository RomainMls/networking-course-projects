/*
 * Class to represent a game in singleplayer or multiplayer
 */
public class Game {
    private char[][] grid;
    private char currentPlayer;
    private boolean finished;
    private ClientHandler player1 = null;
    private ClientHandler player2 = null;
    private final int SIZE = 3;

    /*
     * Initializes an empty 3x3 game
     */
    public Game() {
        grid = new char[SIZE][SIZE];

        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                grid[i][j] = ' ';

        currentPlayer = 'X';
        finished = false;
    }

    /*
     * Initializes a game with two connected players (multiplayer game)
     */
    public Game(ClientHandler player1, ClientHandler player2) {
        this();
        this.player1 = player1;
        this.player2 = player2;
    }

    /*
     * Executes a move and returns its result
     */
    public synchronized MoveResult playMove(int row, int col, char symbol) {
        if (finished)
            return MoveResult.GAME_FINISHED;
        if (row >= SIZE || col >= SIZE || row < 0 || col < 0)
            return MoveResult.INVALID_RANGE;
        if (symbol != currentPlayer)
            return MoveResult.NOT_YOUR_TURN;
        if (grid[row][col] != ' ')
            return MoveResult.CELL_OCCUPIED;

        if (currentPlayer == 'X') {
            grid[row][col] = 'X';
            currentPlayer = 'O';
            return MoveResult.VALID;
        } else {
            grid[row][col] = 'O';
            currentPlayer = 'X';
            return MoveResult.VALID;
        }
    }

    /*
     * Checks the current grid to determine if the game has a winner, a draw, or is
     * ongoing, or if opponent has left
     */
    public synchronized MoveResult checkWin() {

        // Check rows
        for (int i = 0; i < 3; i++) {
            if (grid[i][0] != ' ' && grid[i][0] == grid[i][1] && grid[i][1] == grid[i][2]) {
                finished = true;
                return (grid[i][0] == 'X') ? MoveResult.X_WON : MoveResult.O_WON;
            }
        }

        // Check columns
        for (int j = 0; j < 3; j++) {
            if (grid[0][j] != ' ' && grid[0][j] == grid[1][j] && grid[1][j] == grid[2][j]) {
                finished = true;
                return (grid[0][j] == 'X') ? MoveResult.X_WON : MoveResult.O_WON;
            }
        }

        // Check diagonals
        if (grid[0][0] != ' ' && grid[0][0] == grid[1][1] && grid[1][1] == grid[2][2]) {
            finished = true;
            return (grid[0][0] == 'X') ? MoveResult.X_WON : MoveResult.O_WON;
        }

        if (grid[0][2] != ' ' && grid[0][2] == grid[1][1] && grid[1][1] == grid[2][0]) {
            finished = true;
            return (grid[0][2] == 'X') ? MoveResult.X_WON : MoveResult.O_WON;
        }

        // Check if there are empty cells left
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (grid[i][j] == ' ') {
                    if (!opponentQuit())
                        return MoveResult.ONGOING;
                    else
                        return MoveResult.OPPONENT_QUIT;
                }

        finished = true;
        return MoveResult.DRAW;
    }

    /*
     * Converts the grid into a string to send it to the client
     */
    public synchronized String gridToString() {
        StringBuilder gameString = new StringBuilder();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++)
                gameString.append(grid[i][j]);
            if (i != SIZE - 1)
                gameString.append("\r\n");
        }
        return gameString.toString();
    }

    // Returns true if the game is finished
    public boolean isFinished() {
        return finished;
    }

    // Sets the game as finished
    public void setFinished() {
        finished = true;
    }

    // Removes the player who quits from the game
    public void removePlayer(ClientHandler quitPlayer) {
        if (this.player1 == quitPlayer)
            this.player1 = null;
        else if (this.player2 == quitPlayer)
            this.player2 = null;
    }

    /*
     * Checks if the opponent has quit the game
     * We dont check if the two players are null because it can be a single player
     * game or a multiplayer game where 2 players
     * have left but in this case we have nobody to notify
     */
    private boolean opponentQuit() {
        return (player1 == null && player2 != null) || (player1 != null && player2 == null);
    }
}

// Defines all possible results
enum MoveResult {
    VALID,
    CELL_OCCUPIED,
    INVALID_RANGE,
    NOT_YOUR_TURN,
    GAME_FINISHED,
    OPPONENT_QUIT,
    ONGOING,
    X_WON,
    O_WON,
    DRAW;

    // Converts all move result into a string to be able to print it easily when
    // received
    @Override
    public String toString() {
        switch (this) {
            case CELL_OCCUPIED:
                return "CELL OCCUPIED";
            case INVALID_RANGE:
                return "INVALID RANGE";
            case NOT_YOUR_TURN:
                return "NOT YOUR TURN";
            case GAME_FINISHED:
                return "GAME FINISHED";
            case OPPONENT_QUIT:
                return "OPPONENT QUIT";
            case X_WON:
                return "X WON";
            case O_WON:
                return "O WON";
            case DRAW:
                return "DRAW";
            default:
                return "";
        }
    }
}