//Class which represents the bot that plays random moves in singleplayer mode
public class BotPlayer {
    private Game game;
    private char symbol;

    // Creates a bot linked to a game with a given symbol
    public BotPlayer(Game game, char symbol) {
        this.game = game;
        this.symbol = symbol;
    }

    // Plays a random move (not occupied) on the grid and returns the result
    public MoveResult makeMove() {
        int randomRow = (int) (Math.random() * 3);
        int randomCol = (int) (Math.random() * 3);

        MoveResult result = null;
        while (result != MoveResult.VALID && result != MoveResult.GAME_FINISHED && result != MoveResult.NOT_YOUR_TURN) {
            randomRow = (int) (Math.random() * 3);
            randomCol = (int) (Math.random() * 3);
            result = game.playMove(randomRow, randomCol, symbol);
        }
        return result;
    }
}