import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class OnlineBattleServer {

    private static final int DEFAULT_PORT = 9999;

    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        MatchRoom matchRoom = new MatchRoom();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Online battle server started on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, matchRoom);
                handler.start();
            }
        }
    }

    private static final class MatchRoom {
        private final ClientHandler[] players = new ClientHandler[2];
        private final int[] scores = new int[2];
        private final boolean[] alive = new boolean[]{true, true};

        synchronized int register(ClientHandler handler) {
            for (int index = 0; index < players.length; index++) {
                if (players[index] == null) {
                    players[index] = handler;
                    scores[index] = 0;
                    alive[index] = true;
                    return index;
                }
            }
            return -1;
        }

        synchronized void unregister(int playerIndex) {
            if (playerIndex < 0 || playerIndex >= players.length) {
                return;
            }
            players[playerIndex] = null;
            alive[playerIndex] = false;
            ClientHandler peer = peerOf(playerIndex);
            if (peer != null) {
                peer.sendLine("PEER_LEFT");
                broadcastState();
            }
        }

        synchronized void updateScore(int playerIndex, int score) {
            if (!isValidPlayer(playerIndex)) {
                return;
            }
            scores[playerIndex] = score;
            broadcastState();
        }

        synchronized void markGameOver(int playerIndex, int score) {
            if (!isValidPlayer(playerIndex)) {
                return;
            }
            scores[playerIndex] = score;
            alive[playerIndex] = false;
            if (players[0] != null && players[1] != null && !alive[0] && !alive[1]) {
                broadcastResult();
            } else {
                broadcastState();
            }
        }

        synchronized void broadcastState() {
            String phase = currentPhase();
            String line = "STATE|" + scores[0] + "|" + alive[0] + "|" + scores[1] + "|" + alive[1] + "|" + phase;
            for (ClientHandler player : players) {
                if (player != null) {
                    player.sendLine(line);
                }
            }
        }

        synchronized void broadcastResult() {
            String resultLine = "RESULT|" + scores[0] + "|" + scores[1];
            for (ClientHandler player : players) {
                if (player != null) {
                    player.sendLine(resultLine);
                }
            }
        }

        private boolean isValidPlayer(int playerIndex) {
            return playerIndex >= 0 && playerIndex < players.length;
        }

        private ClientHandler peerOf(int playerIndex) {
            if (playerIndex == 0) {
                return players[1];
            }
            if (playerIndex == 1) {
                return players[0];
            }
            return null;
        }

        private String currentPhase() {
            if (players[0] != null && players[1] != null && !alive[0] && !alive[1]) {
                return "RESULT";
            }
            if (players[0] != null && players[1] != null) {
                return "ACTIVE";
            }
            return "WAITING";
        }
    }

    private static final class ClientHandler extends Thread {
        private final Socket socket;
        private final MatchRoom room;
        private BufferedReader reader;
        private PrintWriter writer;
        private int playerIndex = -1;

        private ClientHandler(Socket socket, MatchRoom room) {
            super("online-battle-client");
            this.socket = socket;
            this.room = room;
        }

        @Override
        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                        socket.getOutputStream(), StandardCharsets.UTF_8)), true);

                playerIndex = room.register(this);
                if (playerIndex < 0) {
                    sendLine("MATCH_FULL");
                    socket.close();
                    return;
                }

                sendLine("ASSIGN|" + (playerIndex + 1));
                room.broadcastState();

                String line;
                while ((line = reader.readLine()) != null) {
                    handleLine(line);
                }
            } catch (IOException exception) {
                System.out.println("client disconnected: " + exception.getMessage());
            } finally {
                room.unregister(playerIndex);
                closeQuietly();
            }
        }

        private void handleLine(String line) {
            if (line == null || line.trim().isEmpty() || "JOIN".equals(line)) {
                return;
            }
            String[] parts = line.split("\\|");
            if (parts.length == 0) {
                sendLine("ERROR|invalid message");
                return;
            }
            try {
                switch (parts[0]) {
                    case "SCORE":
                        room.updateScore(playerIndex, Integer.parseInt(parts[1]));
                        break;
                    case "GAME_OVER":
                        room.markGameOver(playerIndex, Integer.parseInt(parts[1]));
                        break;
                    case "QUIT":
                        socket.close();
                        break;
                    default:
                        sendLine("ERROR|unsupported message");
                        break;
                }
            } catch (Exception exception) {
                sendLine("ERROR|bad payload");
            }
        }

        private void sendLine(String line) {
            if (writer != null) {
                writer.println(line);
            }
        }

        private void closeQuietly() {
            try {
                socket.close();
            } catch (IOException ignored) {
                // no-op
            }
        }
    }
}
