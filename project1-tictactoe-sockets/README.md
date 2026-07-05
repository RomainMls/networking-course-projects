# Project 1 - Tic-tac-toe Sockets

This project is a Java client/server Tic-tac-toe game based on TCP sockets. The server accepts several clients, manages games in separate threads, and allows players to play either against a bot or against another connected player.

## Main Features

- TCP socket communication between client and server.
- One thread per connected client.
- Matchmaking system for multiplayer games.
- Bot mode for single-player games.
- Simple text-based protocol between client and server.
- Game state handling with win, draw, invalid move and quit cases.

## Main Files

- `TictactoeServer.java`: starts the server and accepts client connections.
- `TictactoeClient.java`: command-line client used to play the game.
- `ClientHandler.java`: handles one connected client.
- `Matchmaking.java`: pairs players for multiplayer games.
- `Game.java`: stores and updates the Tic-tac-toe board.
- `BotPlayer.java`: bot logic for single-player mode.
- `MessageReader.java` and `MessageWriter.java`: socket communication helpers.
- `enonce.pdf`: project statement.
- `report.pdf`: final report.

## Build and Run

Compile the project:

```bash
javac *.java
```

Start the server:

```bash
java TictactoeServer
```

In another terminal, start a client:

```bash
java TictactoeClient
```

For multiplayer mode, start at least two clients while the server is running.

## Note

This is a student networking project. The focus is on sockets, concurrency, message handling, and basic client/server architecture.
