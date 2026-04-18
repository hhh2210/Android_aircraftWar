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
import java.util.ArrayDeque;
import java.util.Queue;

public class OnlineMatchClient implements Closeable {

    public static final String PHASE_WAITING = "WAITING";
    public static final String PHASE_ACTIVE = "ACTIVE";
    public static final String PHASE_RESULT = "RESULT";

    private static final int CONNECT_TIMEOUT_MS = 5000;

    private final String host;
    private final int port;
    private final Listener listener;
    private final Object sendLock = new Object();
    private final Queue<String> pendingMessages = new ArrayDeque<>();

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Thread readThread;
    private volatile boolean closed;

    public OnlineMatchClient(String host, int port, Listener listener) {
        this.host = host;
        this.port = port;
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
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), true);
            listener.onConnected();
            sendLine("JOIN");
            flushPendingMessages();
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
            closeSilently();
        }
    }

    private void dispatchServerMessage(ServerMessage message) {
        switch (message.type) {
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
            case MATCH_FULL:
                listener.onMatchFull();
                break;
            case ERROR:
                listener.onError(message.errorMessage);
                break;
            case UNKNOWN:
            default:
                listener.onError("unknown server message: " + message.rawLine);
                break;
        }
    }

    private void sendLine(String line) {
        synchronized (sendLock) {
            if (writer == null) {
                pendingMessages.offer(line);
                return;
            }
            writer.println(line);
        }
    }

    private void flushPendingMessages() {
        synchronized (sendLock) {
            while (writer != null && !pendingMessages.isEmpty()) {
                writer.println(pendingMessages.poll());
            }
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        sendLine("QUIT");
        closeSilently();
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
                case "MATCH_FULL":
                    return ServerMessage.matchFull(line);
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

        void onAssignedPlayer(int playerIndex);

        void onStateUpdate(ServerState state);

        void onResult(int playerOneScore, int playerTwoScore);

        void onPeerLeft();

        void onMatchFull();

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
        final String errorMessage;

        private ServerMessage(MessageType type, String rawLine, int playerIndex,
                              ServerState state, String errorMessage) {
            this.type = type;
            this.rawLine = rawLine;
            this.playerIndex = playerIndex;
            this.state = state;
            this.errorMessage = errorMessage;
        }

        static ServerMessage assign(String rawLine, int playerIndex) {
            return new ServerMessage(MessageType.ASSIGN, rawLine, playerIndex, null, null);
        }

        static ServerMessage state(String rawLine, ServerState state) {
            return new ServerMessage(MessageType.STATE, rawLine, 0, state, null);
        }

        static ServerMessage result(String rawLine, ServerState state) {
            return new ServerMessage(MessageType.RESULT, rawLine, 0, state, null);
        }

        static ServerMessage peerLeft(String rawLine) {
            return new ServerMessage(MessageType.PEER_LEFT, rawLine, 0, null, null);
        }

        static ServerMessage matchFull(String rawLine) {
            return new ServerMessage(MessageType.MATCH_FULL, rawLine, 0, null, null);
        }

        static ServerMessage error(String rawLine, String errorMessage) {
            return new ServerMessage(MessageType.ERROR, rawLine, 0, null, errorMessage);
        }

        static ServerMessage unknown(String rawLine) {
            return new ServerMessage(MessageType.UNKNOWN, rawLine, 0, null, null);
        }
    }

    enum MessageType {
        ASSIGN,
        STATE,
        RESULT,
        PEER_LEFT,
        MATCH_FULL,
        ERROR,
        UNKNOWN
    }
}
