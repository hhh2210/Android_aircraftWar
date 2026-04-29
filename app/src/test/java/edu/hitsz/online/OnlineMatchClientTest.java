package edu.hitsz.online;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OnlineMatchClientTest {

    @Test
    public void parseWaitingMessageExtractsDifficulty() {
        OnlineMatchClient.ServerMessage message =
                OnlineMatchClient.parseServerMessage("WAITING|HARD");

        assertEquals(OnlineMatchClient.MessageType.WAITING, message.type);
        assertEquals("HARD", message.difficulty);
    }

    @Test
    public void parseMatchedMessageExtractsRoomId() {
        OnlineMatchClient.ServerMessage message =
                OnlineMatchClient.parseServerMessage("MATCHED|NORMAL|room-123");

        assertEquals(OnlineMatchClient.MessageType.MATCHED, message.type);
        assertEquals("NORMAL", message.difficulty);
        assertEquals("room-123", message.roomId);
    }

    @Test
    public void parseAssignMessageExtractsPlayerIndex() {
        OnlineMatchClient.ServerMessage message = OnlineMatchClient.parseServerMessage("ASSIGN|2");

        assertEquals(OnlineMatchClient.MessageType.ASSIGN, message.type);
        assertEquals(2, message.playerIndex);
    }

    @Test
    public void parseStateMessageExtractsScoresAliveFlagsAndPhase() {
        OnlineMatchClient.ServerMessage message =
                OnlineMatchClient.parseServerMessage("STATE|120|true|85|false|ACTIVE");

        assertEquals(OnlineMatchClient.MessageType.STATE, message.type);
        assertEquals(120, message.state.playerOneScore);
        assertTrue(message.state.playerOneAlive);
        assertEquals(85, message.state.playerTwoScore);
        assertFalse(message.state.playerTwoAlive);
        assertEquals(OnlineMatchClient.PHASE_ACTIVE, message.state.phase);
    }

    @Test
    public void parseResultMessageMarksResultPhase() {
        OnlineMatchClient.ServerMessage message =
                OnlineMatchClient.parseServerMessage("RESULT|300|260");

        assertEquals(OnlineMatchClient.MessageType.RESULT, message.type);
        assertEquals(300, message.state.playerOneScore);
        assertEquals(260, message.state.playerTwoScore);
        assertEquals(OnlineMatchClient.PHASE_RESULT, message.state.phase);
    }

    @Test
    public void parseInvalidNumericPayloadReturnsErrorMessage() {
        OnlineMatchClient.ServerMessage message =
                OnlineMatchClient.parseServerMessage("STATE|oops|true|85|false|ACTIVE");

        assertEquals(OnlineMatchClient.MessageType.ERROR, message.type);
        assertEquals("invalid numeric payload", message.errorMessage);
    }

    @Test
    public void parseUnknownMessageFallsBackToUnknownType() {
        OnlineMatchClient.ServerMessage message =
                OnlineMatchClient.parseServerMessage("PING");

        assertEquals(OnlineMatchClient.MessageType.UNKNOWN, message.type);
    }

    @Test
    public void clientSendsJoinGameOverAndQuitThroughSocketQueue() throws Exception {
        LinkedBlockingQueue<String> receivedMessages = new LinkedBlockingQueue<>();
        CountDownLatch connected = new CountDownLatch(1);
        CountDownLatch resultReceived = new CountDownLatch(1);

        try (ServerSocket serverSocket = new ServerSocket(0)) {
            Thread fakeServer = new Thread(() -> runFakeServer(serverSocket, receivedMessages),
                    "online-match-client-test-server");
            fakeServer.start();

            OnlineMatchClient client = new OnlineMatchClient(
                    "127.0.0.1",
                    serverSocket.getLocalPort(),
                    "easy",
                    new TestListener(connected, resultReceived)
            );

            client.connect();

            assertTrue(connected.await(2, TimeUnit.SECONDS));
            assertEquals("JOIN|easy", receivedMessages.poll(2, TimeUnit.SECONDS));

            client.sendGameOver(42);

            assertEquals("GAME_OVER|42", receivedMessages.poll(2, TimeUnit.SECONDS));
            assertTrue(resultReceived.await(2, TimeUnit.SECONDS));

            client.close();

            assertEquals("QUIT", receivedMessages.poll(2, TimeUnit.SECONDS));
            assertNull(receivedMessages.poll(200, TimeUnit.MILLISECONDS));
            fakeServer.join(2000);
        }
    }

    private void runFakeServer(ServerSocket serverSocket, LinkedBlockingQueue<String> receivedMessages) {
        try (Socket socket = serverSocket.accept();
             BufferedReader reader = new BufferedReader(new InputStreamReader(
                     socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            writer.println("CONNECTED|fake-server");
            String line;
            while ((line = reader.readLine()) != null) {
                receivedMessages.offer(line);
                if ("GAME_OVER|42".equals(line)) {
                    writer.println("RESULT|42|7");
                }
                if ("QUIT".equals(line)) {
                    return;
                }
            }
        } catch (IOException ignored) {
            // Test server stops when the client closes the socket.
        }
    }

    private static class TestListener implements OnlineMatchClient.Listener {
        private final CountDownLatch connected;
        private final CountDownLatch resultReceived;

        private TestListener(CountDownLatch connected, CountDownLatch resultReceived) {
            this.connected = connected;
            this.resultReceived = resultReceived;
        }

        @Override
        public void onConnected() {
            connected.countDown();
        }

        @Override
        public void onWaiting(String matchDifficulty) {
        }

        @Override
        public void onMatched(String matchDifficulty, String roomId) {
        }

        @Override
        public void onAssignedPlayer(int playerIndex) {
        }

        @Override
        public void onStateUpdate(OnlineMatchClient.ServerState state) {
        }

        @Override
        public void onResult(int playerOneScore, int playerTwoScore) {
            assertEquals(42, playerOneScore);
            assertEquals(7, playerTwoScore);
            resultReceived.countDown();
        }

        @Override
        public void onPeerLeft() {
        }

        @Override
        public void onDisconnected(String reason) {
            assertNotNull(reason);
        }

        @Override
        public void onError(String message) {
            assertNotNull(message);
        }
    }
}
