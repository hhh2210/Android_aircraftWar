package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import edu.hitsz.application.BaseGame;
import edu.hitsz.application.EasyGame;
import edu.hitsz.application.HardGame;
import edu.hitsz.application.NormalGame;
import edu.hitsz.online.OnlineMatchClient;

public class OnlineGameActivity extends AppCompatActivity {

    public static final String EXTRA_HOST = "edu.hitsz.extra.HOST";
    public static final String EXTRA_PORT = "edu.hitsz.extra.PORT";
    public static final String DEFAULT_HOST = "10.0.2.2";
    public static final int DEFAULT_PORT = 9999;

    private static final int MSG_GAME_OVER = 1;

    private BaseGame gameView;
    private OnlineMatchClient matchClient;
    private int localPlayerIndex;
    private boolean localGameOverSent;
    private boolean terminalDialogShown;

    private final Handler gameOverHandler = new Handler(Looper.getMainLooper(), this::handleGameMessage);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String difficulty = normalizeDifficulty(getIntent().getStringExtra(GameDifficulty.EXTRA_DIFFICULTY));
        String host = getIntent().getStringExtra(EXTRA_HOST);
        int port = getIntent().getIntExtra(EXTRA_PORT, DEFAULT_PORT);
        if (host == null || host.trim().isEmpty()) {
            host = DEFAULT_HOST;
        }

        gameView = createGameViewByDifficulty(difficulty);
        gameView.attachOnlineScoreSyncListener(score -> {
            if (matchClient != null) {
                matchClient.sendScore(score);
            }
        });
        gameView.updateOnlineState(0, false, getString(R.string.online_status_connecting));
        setContentView(gameView);

        String finalHost = host;
        matchClient = new OnlineMatchClient(finalHost, port, new OnlineMatchClient.Listener() {
            @Override
            public void onConnected() {
                runOnUiThread(() -> gameView.updateOnlineState(0, false,
                        getString(R.string.online_status_waiting)));
            }

            @Override
            public void onAssignedPlayer(int playerIndex) {
                localPlayerIndex = playerIndex;
            }

            @Override
            public void onStateUpdate(OnlineMatchClient.ServerState state) {
                runOnUiThread(() -> applyServerState(state));
            }

            @Override
            public void onResult(int playerOneScore, int playerTwoScore) {
                runOnUiThread(() -> showResultDialog(playerOneScore, playerTwoScore));
            }

            @Override
            public void onPeerLeft() {
                runOnUiThread(() -> showTerminalDialog(
                        getString(R.string.online_status_peer_left),
                        getString(R.string.online_status_peer_left)
                ));
            }

            @Override
            public void onMatchFull() {
                runOnUiThread(() -> showTerminalDialog(
                        getString(R.string.online_status_match_full),
                        getString(R.string.online_status_match_full)
                ));
            }

            @Override
            public void onDisconnected(String reason) {
                runOnUiThread(() -> {
                    if (!terminalDialogShown) {
                        showTerminalDialog(
                                getString(R.string.online_status_connection_failed,
                                        safeReason(reason)),
                                getString(R.string.online_status_connection_failed,
                                        safeReason(reason))
                        );
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    if (!terminalDialogShown) {
                        showTerminalDialog(
                                getString(R.string.online_status_error, safeReason(message)),
                                getString(R.string.online_status_error, safeReason(message))
                        );
                    }
                });
            }
        });
        matchClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.onHostResume();
        }
    }

    @Override
    protected void onPause() {
        if (gameView != null) {
            gameView.onHostPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (matchClient != null) {
            matchClient.close();
        }
        if (gameView != null) {
            gameView.releaseResources();
        }
        super.onDestroy();
    }

    private BaseGame createGameViewByDifficulty(String difficulty) {
        switch (difficulty) {
            case GameDifficulty.NORMAL:
                return new NormalGame(this, gameOverHandler, MSG_GAME_OVER);
            case GameDifficulty.HARD:
                return new HardGame(this, gameOverHandler, MSG_GAME_OVER);
            case GameDifficulty.EASY:
            default:
                return new EasyGame(this, gameOverHandler, MSG_GAME_OVER);
        }
    }

    private boolean handleGameMessage(Message message) {
        if (message.what != MSG_GAME_OVER || isFinishing() || isDestroyed()) {
            return true;
        }
        int score = message.arg1;
        if (!localGameOverSent && matchClient != null) {
            localGameOverSent = true;
            matchClient.sendGameOver(score);
        }
        gameView.updateOnlineStatus(getString(R.string.online_status_game_over_waiting));
        return true;
    }

    private void applyServerState(OnlineMatchClient.ServerState state) {
        int playerIndex = localPlayerIndex <= 0 ? 1 : localPlayerIndex;
        int opponentScore = playerIndex == 1 ? state.playerTwoScore : state.playerOneScore;
        boolean opponentAlive = playerIndex == 1 ? state.playerTwoAlive : state.playerOneAlive;
        String status = mapPhaseToStatus(state.phase);
        if (localGameOverSent && OnlineMatchClient.PHASE_ACTIVE.equals(state.phase)) {
            status = getString(R.string.online_status_game_over_waiting);
        }
        gameView.updateOnlineState(opponentScore, opponentAlive, status);
    }

    private String mapPhaseToStatus(String phase) {
        if (OnlineMatchClient.PHASE_ACTIVE.equals(phase)) {
            return getString(R.string.online_status_active);
        }
        if (OnlineMatchClient.PHASE_RESULT.equals(phase)) {
            return getString(R.string.online_result_title);
        }
        return getString(R.string.online_status_waiting);
    }

    private void showResultDialog(int playerOneScore, int playerTwoScore) {
        if (terminalDialogShown || isFinishing() || isDestroyed()) {
            return;
        }
        terminalDialogShown = true;
        int playerIndex = localPlayerIndex <= 0 ? 1 : localPlayerIndex;
        int selfScore = playerIndex == 1 ? playerOneScore : playerTwoScore;
        int opponentScore = playerIndex == 1 ? playerTwoScore : playerOneScore;
        gameView.updateOnlineState(opponentScore, false, getString(R.string.online_result_title));
        new AlertDialog.Builder(this)
                .setTitle(R.string.online_result_title)
                .setMessage(getString(R.string.online_result_message, selfScore, opponentScore))
                .setCancelable(false)
                .setPositiveButton(R.string.online_result_confirm, (dialog, which) -> {
                    finish();
                    startActivity(new Intent(this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                })
                .show();
    }

    private void showTerminalDialog(String title, String statusText) {
        if (terminalDialogShown || isFinishing() || isDestroyed()) {
            return;
        }
        terminalDialogShown = true;
        gameView.updateOnlineStatus(statusText);
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(statusText)
                .setCancelable(false)
                .setPositiveButton(R.string.online_result_confirm, (dialog, which) -> finish())
                .show();
    }

    private String normalizeDifficulty(String difficulty) {
        if (difficulty == null) {
            return GameDifficulty.EASY;
        }
        return GameDifficulty.normalize(difficulty.trim().toLowerCase(Locale.ROOT));
    }

    private String safeReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return "unknown";
        }
        return reason;
    }
}
