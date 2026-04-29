package edu.hitsz.online;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class OnlineMatchClient implements Closeable {

    public static final String PHASE_ACTIVE = "ACTIVE";
    public static final String PHASE_RESULT = "RESULT";

    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final String CLOSE_MARKER = "__ONLINE_MATCH_CLIENT_CLOSE__";
    private static final String QUIT_MESSAGE = "QUIT";

    private final String host;
    private final int port;
    private final String difficulty;
    private final Listener listener;
    private final Object lifecycleLock = new Object();
    private final BlockingDeque<String> sendQueue = new LinkedBlockingDeque<>();

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Thread readThread;
    private Thread sendThread;
    private volatile boolean closed;

    public OnlineMatchClient(String host, int port, String difficulty, Listener listener) {
        this.host = host;
        this.port = port;
        this.difficulty = difficulty;
        this.listener = listener;
    }

    public void connect() {
        readThread = new Thread(this::runClient, "online-match-client");
        readThread.start();
    }

    public void sendScore(int score) {
        sendLine("SCORE|" + score);
    }

    public void sendGameOver(int score) {
        sendLine("GAME_OVER|" + score);
    }

    private void runClient() {
        try {
            Socket connectedSocket = new Socket();
            socket = connectedSocket;
            connectedSocket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            reader = new BufferedReader(new InputStreamReader(connectedSocket.getInputStream(), StandardCharsets.UTF_8));
            writer = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(connectedSocket.getOutputStream(), StandardCharsets.UTF_8)), true);
            if (closed) {
                return;
            }
            sendQueue.offerFirst("JOIN|" + difficulty);
            startSender();
            listener.onConnected();
            String line;
            while (!closed && (line = reader.readLine()) != null) {
                ServerMessage message = parseServerMessage(line);
                dispatchServerMessage(message);
            }
            if (!closed) {
                listener.onDisconnected("server closed connection");
            }
        } catch (IOException exception) {
            if (!closed) {
                listener.onDisconnected(exception.getMessage());
            }
        } finally {
            closeFromRemote();
        }
    }

    private void dispatchServerMessage(ServerMessage message) {
        switch (message.type) {
            case WAITING:
                listener.onWaiting(message.difficulty);
                break;
            case MATCHED:
                listener.onMatched(message.difficulty, message.roomId);
                break;
            case ASSIGN:
                listener.onAssignedPlayer(message.playerIndex);
                break;
            case STATE:
                listener.onStateUpdate(message.state);
                break;
            case RESULT:
                listener.onResult(message.state.playerOneScore, message.state.playerTwoScore);
                break;
            case PEER_LEFT:
                listener.onPeerLeft();
                break;
            case ERROR:
                listener.onError(message.errorMessage);
                break;
            case CONNECTED:
            case PONG:
                break;
            case UNKNOWN:
            default:
                listener.onError("unknown server message: " + message.rawLine);
                break;
        }
    }

    private void sendLine(String line) {
        if (closed) {
            return;
        }
        sendQueue.offer(line);
    }

    private void startSender() {
        sendThread = new Thread(this::runSender, "online-match-sender");
        sendThread.start();
    }

    private void runSender() {
        try {
            while (true) {
                String line = sendQueue.take();
                if (CLOSE_MARKER.equals(line)) {
                    return;
                }
                writeLineDirect(line);
                if (QUIT_MESSAGE.equals(line)) {
                    return;
                }
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } finally {
            closeSilently();
        }
    }

    private void writeLineDirect(String line) {
        PrintWriter currentWriter = writer;
        if (currentWriter == null) {
            return;
        }
        currentWriter.println(line);
    }

    @Override
    public void close() {
        if (!markClosed()) {
            return;
        }
        if (sendThread == null) {
            closeSilently();
            sendQueue.offer(CLOSE_MARKER);
            return;
        }
        sendQueue.clear();
        sendQueue.offer(QUIT_MESSAGE);
    }

    private void closeFromRemote() {
        if (!markClosed()) {
            return;
        }
        sendQueue.clear();
        sendQueue.offer(CLOSE_MARKER);
        closeSilently();
    }

    private boolean markClosed() {
        synchronized (lifecycleLock) {
            if (closed) {
                return false;
            }
            closed = true;
            return true;
        }
    }

    private void closeSilently() {
        closeQuietly(reader);
        if (writer != null) {
            writer.close();
        }
        closeQuietly(socket);
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignored) {
            // no-op
        }
    }

    static ServerMessage parseServerMessage(String line) {
        if (line == null || line.trim().isEmpty()) {
            return ServerMessage.unknown(line);
        }
        String[] parts = line.split("\\|");
        String type = parts[0];
        try {
            switch (type) {
                case "CONNECTED":
                    return ServerMessage.connected(line);
                case "WAITING":
                    return ServerMessage.waiting(line, parts.length >= 2 ? parts[1] : "");
                case "MATCHED":
                    return ServerMessage.matched(
                            line,
                            parts.length >= 2 ? parts[1] : "",
                            parts.length >= 3 ? parts[2] : ""
                    );
                case "ASSIGN":
                    if (parts.length < 2) {
                        return ServerMessage.unknown(line);
                    }
                    return ServerMessage.assign(line, Integer.parseInt(parts[1]));
                case "STATE":
                    if (parts.length < 6) {
                        return ServerMessage.unknown(line);
                    }
                    return ServerMessage.state(line, new ServerState(
                            Integer.parseInt(parts[1]),
                            Boolean.parseBoolean(parts[2]),
                            Integer.parseInt(parts[3]),
                            Boolean.parseBoolean(parts[4]),
                            parts[5]
                    ));
                case "RESULT":
                    if (parts.length < 3) {
                        return ServerMessage.unknown(line);
                    }
                    return ServerMessage.result(line, new ServerState(
                            Integer.parseInt(parts[1]),
                            false,
                            Integer.parseInt(parts[2]),
                            false,
                            PHASE_RESULT
                    ));
                case "PEER_LEFT":
                    return ServerMessage.peerLeft(line);
                case "PONG":
                    return ServerMessage.pong(line);
                case "ERROR":
                    return ServerMessage.error(line, parts.length >= 2 ? parts[1] : "unknown error");
                default:
                    return ServerMessage.unknown(line);
            }
        } catch (NumberFormatException exception) {
            return ServerMessage.error(line, "invalid numeric payload");
        }
    }

    public interface Listener {
        void onConnected();

        void onWaiting(String matchDifficulty);

        void onMatched(String matchDifficulty, String roomId);

        void onAssignedPlayer(int playerIndex);

        void onStateUpdate(ServerState state);

        void onResult(int playerOneScore, int playerTwoScore);

        void onPeerLeft();

        void onDisconnected(String reason);

        void onError(String message);
    }

    public static final class ServerState {
        public final int playerOneScore;
        public final boolean playerOneAlive;
        public final int playerTwoScore;
        public final boolean playerTwoAlive;
        public final String phase;

        public ServerState(int playerOneScore, boolean playerOneAlive,
                           int playerTwoScore, boolean playerTwoAlive, String phase) {
            this.playerOneScore = playerOneScore;
            this.playerOneAlive = playerOneAlive;
            this.playerTwoScore = playerTwoScore;
            this.playerTwoAlive = playerTwoAlive;
            this.phase = phase;
        }
    }

    static final class ServerMessage {
        final MessageType type;
        final String rawLine;
        final int playerIndex;
        final ServerState state;
        final String difficulty;
        final String roomId;
        final String errorMessage;

        private ServerMessage(MessageType type, String rawLine, int playerIndex,
                              ServerState state, String difficulty, String roomId,
                              String errorMessage) {
            this.type = type;
            this.rawLine = rawLine;
            this.playerIndex = playerIndex;
            this.state = state;
            this.difficulty = difficulty;
            this.roomId = roomId;
            this.errorMessage = errorMessage;
        }

        static ServerMessage connected(String rawLine) {
            return new ServerMessage(MessageType.CONNECTED, rawLine, 0, null, null, null, null);
        }

        static ServerMessage waiting(String rawLine, String difficulty) {
            return new ServerMessage(MessageType.WAITING, rawLine, 0, null, difficulty, null, null);
        }

        static ServerMessage matched(String rawLine, String difficulty, String roomId) {
            return new ServerMessage(MessageType.MATCHED, rawLine, 0, null, difficulty, roomId, null);
        }

        static ServerMessage assign(String rawLine, int playerIndex) {
            return new ServerMessage(MessageType.ASSIGN, rawLine, playerIndex, null, null, null, null);
        }

        static ServerMessage state(String rawLine, ServerState state) {
            return new ServerMessage(MessageType.STATE, rawLine, 0, state, null, null, null);
        }

        static ServerMessage result(String rawLine, ServerState state) {
            return new ServerMessage(MessageType.RESULT, rawLine, 0, state, null, null, null);
        }

        static ServerMessage peerLeft(String rawLine) {
            return new ServerMessage(MessageType.PEER_LEFT, rawLine, 0, null, null, null, null);
        }

        static ServerMessage pong(String rawLine) {
            return new ServerMessage(MessageType.PONG, rawLine, 0, null, null, null, null);
        }

        static ServerMessage error(String rawLine, String errorMessage) {
            return new ServerMessage(MessageType.ERROR, rawLine, 0, null, null, null, errorMessage);
        }

        static ServerMessage unknown(String rawLine) {
            return new ServerMessage(MessageType.UNKNOWN, rawLine, 0, null, null, null, null);
        }
    }

    enum MessageType {
        CONNECTED,
        WAITING,
        MATCHED,
        ASSIGN,
        STATE,
        RESULT,
        PEER_LEFT,
        PONG,
        ERROR,
        UNKNOWN
    }
}
