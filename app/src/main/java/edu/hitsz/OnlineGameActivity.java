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
import edu.hitsz.rank.RankActivity;
import edu.hitsz.rank.RankDbHelper;
import edu.hitsz.rank.RankSaveDialog;

public class OnlineGameActivity extends AppCompatActivity {

    // Replace this with the server machine's LAN IP when testing on real devices.
    public static final String SERVER_HOST = "10.0.2.2";
    public static final int EASY_PORT = 9999;
    public static final int NORMAL_PORT = 12000;
    public static final int HARD_PORT = 10001;

    private static final int MSG_GAME_OVER = 1;

    private BaseGame gameView;
    private OnlineMatchClient matchClient;
    private RankDbHelper rankDbHelper;
    private String currentDifficulty;
    private volatile int localPlayerIndex;
    private boolean localGameOverSent;
    private boolean terminalDialogShown;

    private final Handler gameOverHandler = new Handler(Looper.getMainLooper(), this::handleGameMessage);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDifficulty = normalizeDifficulty(getIntent().getStringExtra(GameDifficulty.EXTRA_DIFFICULTY));
        int port = portForDifficulty(currentDifficulty);
        rankDbHelper = new RankDbHelper(getApplicationContext());

        gameView = createGameViewByDifficulty(currentDifficulty);
        gameView.attachOnlineScoreSyncListener(score -> {
            if (matchClient != null) {
                matchClient.sendScore(score);
            }
        });
        gameView.setOnlineGameplayReady(false);
        gameView.updateOnlineState(0, false, getString(R.string.online_status_connecting));
        setContentView(gameView);

        matchClient = new OnlineMatchClient(SERVER_HOST, port, currentDifficulty, new OnlineMatchClient.Listener() {
            @Override
            public void onConnected() {
                runOnUiThreadIfAlive(() -> gameView.updateOnlineStatus(getString(R.string.online_status_waiting)));
            }

            @Override
            public void onWaiting(String matchDifficulty) {
                runOnUiThreadIfAlive(() -> {
                    gameView.setOnlineGameplayReady(false);
                    gameView.updateOnlineState(0, false, getString(R.string.online_status_waiting));
                });
            }

            @Override
            public void onMatched(String matchDifficulty, String roomId) {
                runOnUiThreadIfAlive(() -> gameView.updateOnlineStatus(getString(R.string.online_status_matched)));
            }

            @Override
            public void onAssignedPlayer(int playerIndex) {
                localPlayerIndex = playerIndex;
            }

            @Override
            public void onStateUpdate(OnlineMatchClient.ServerState state) {
                runOnUiThreadIfAlive(() -> applyServerState(state));
            }

            @Override
            public void onResult(int playerOneScore, int playerTwoScore) {
                runOnUiThreadIfAlive(() -> showResultDialog(playerOneScore, playerTwoScore));
            }

            @Override
            public void onPeerLeft() {
                runOnUiThreadIfAlive(() -> showTerminalDialog(
                        getString(R.string.online_status_peer_left),
                        getString(R.string.online_status_peer_left)
                ));
            }

            @Override
            public void onDisconnected(String reason) {
                runOnUiThreadIfAlive(() -> {
                    if (!terminalDialogShown) {
                        showTerminalDialog(
                                getString(R.string.online_status_connection_failed, safeReason(reason)),
                                getString(R.string.online_status_connection_failed, safeReason(reason))
                        );
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThreadIfAlive(() -> {
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
            matchClient = null;
        }
        if (gameView != null) {
            gameView.releaseResources();
            gameView = null;
        }
        if (rankDbHelper != null) {
            rankDbHelper.close();
            rankDbHelper = null;
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
        if (message.what != MSG_GAME_OVER || !isUiAlive()) {
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
        if (OnlineMatchClient.PHASE_ACTIVE.equals(state.phase)) {
            gameView.setOnlineGameplayReady(true);
        } else {
            gameView.setOnlineGameplayReady(false);
        }

        String status = mapStateToStatus(state.phase, opponentAlive);
        if (localGameOverSent && OnlineMatchClient.PHASE_ACTIVE.equals(state.phase)) {
            status = getString(R.string.online_status_game_over_waiting);
        }
        gameView.updateOnlineState(opponentScore, opponentAlive, status);
    }

    private String mapStateToStatus(String phase, boolean opponentAlive) {
        if (OnlineMatchClient.PHASE_RESULT.equals(phase)) {
            return getString(R.string.online_result_title);
        }
        if (OnlineMatchClient.PHASE_ACTIVE.equals(phase)) {
            if (!opponentAlive) {
                return getString(R.string.online_status_opponent_dead);
            }
            return getString(R.string.online_status_active);
        }
        return getString(R.string.online_status_waiting);
    }

    private void showResultDialog(int playerOneScore, int playerTwoScore) {
        if (terminalDialogShown || !isUiAlive()) {
            return;
        }
        terminalDialogShown = true;
        gameView.stopOnlineSession();
        int playerIndex = localPlayerIndex <= 0 ? 1 : localPlayerIndex;
        int selfScore = playerIndex == 1 ? playerOneScore : playerTwoScore;
        int opponentScore = playerIndex == 1 ? playerTwoScore : playerOneScore;
        gameView.updateOnlineState(opponentScore, false, getString(R.string.online_result_title));
        new AlertDialog.Builder(this)
                .setTitle(R.string.online_result_title)
                .setMessage(getString(R.string.online_result_message, selfScore, opponentScore))
                .setCancelable(false)
                .setPositiveButton(R.string.online_result_confirm, (dialog, which) -> {
                    showUsernameDialog(selfScore);
                })
                .show();
    }

    private void showUsernameDialog(int score) {
        if (!isUiAlive() || rankDbHelper == null) {
            return;
        }
        RankSaveDialog.show(this, rankDbHelper, score, currentDifficulty, () -> {
            startActivity(new Intent(this, RankActivity.class));
            finish();
        });
    }

    private void showTerminalDialog(String title, String statusText) {
        if (terminalDialogShown || !isUiAlive()) {
            return;
        }
        terminalDialogShown = true;
        gameView.stopOnlineSession();
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

    private int portForDifficulty(String difficulty) {
        switch (difficulty) {
            case GameDifficulty.NORMAL:
                return NORMAL_PORT;
            case GameDifficulty.HARD:
                return HARD_PORT;
            case GameDifficulty.EASY:
            default:
                return EASY_PORT;
        }
    }

    private String safeReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return "unknown";
        }
        return reason;
    }

    private void runOnUiThreadIfAlive(Runnable action) {
        runOnUiThread(() -> {
            if (isUiAlive()) {
                action.run();
            }
        });
    }

    private boolean isUiAlive() {
        return gameView != null && !isFinishing() && !isDestroyed();
    }

}
