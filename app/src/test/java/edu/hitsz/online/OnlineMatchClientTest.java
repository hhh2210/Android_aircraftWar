package edu.hitsz.online;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OnlineMatchClientTest {

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
    public void parseMatchFullMessageKeepsType() {
        OnlineMatchClient.ServerMessage message =
                OnlineMatchClient.parseServerMessage("MATCH_FULL");

        assertEquals(OnlineMatchClient.MessageType.MATCH_FULL, message.type);
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
}
