/*
 * Class to represent the queue of the matchmaking like a waiting room
 */
public class Matchmaking {
    private ClientHandler waitingPlayer;

    /*
     * Takes a player as input and adds it to the queue or resets the queue and
     * start the game if 2 players
     * Returns a string PLAYER X if the player is alone in the queue or PLAYER O if
     * the player is the second
     */
    public synchronized String requestGame(ClientHandler player) {
        if (waitingPlayer == null) {
            waitingPlayer = player;
            return "PLAYER X";

        } else {
            waitingPlayer.setSymbol('X');
            player.setSymbol('O');

            Game game = new Game(waitingPlayer, player);
            waitingPlayer.setGame(game);
            player.setGame(game);

            waitingPlayer = null;
            return "PLAYER O";
        }

    }

    // Removes the player from the queue
    public synchronized void removeWaitingPlayer(ClientHandler player) {
        if (player == waitingPlayer)
            waitingPlayer = null;
    }
}