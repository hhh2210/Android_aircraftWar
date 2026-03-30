package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.hitsz.application.BaseGame;
import edu.hitsz.application.EasyGame;
import edu.hitsz.application.HardGame;
import edu.hitsz.application.NormalGame;
import edu.hitsz.rank.RankActivity;
import edu.hitsz.rank.RankDbHelper;
import edu.hitsz.rank.RankRecord;

public class GameActivity extends AppCompatActivity {

    private static final int MSG_GAME_OVER = 1;

    private BaseGame gameView;
    private RankDbHelper rankDbHelper;
    private String currentDifficulty;
    private final Handler gameOverHandler = new Handler(Looper.getMainLooper(), this::handleGameMessage);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDifficulty = normalizeDifficulty(getIntent().getStringExtra(GameDifficulty.EXTRA_DIFFICULTY));
        rankDbHelper = new RankDbHelper(getApplicationContext());
        gameView = createGameViewByDifficulty(currentDifficulty);
        setContentView(gameView);
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
        if (gameView != null) {
            gameView.releaseResources();
        }
        if (rankDbHelper != null) {
            rankDbHelper.close();
        }
        super.onDestroy();
    }

    BaseGame createGameViewByDifficulty(String difficulty) {
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
        rankDbHelper.insert(new RankRecord(score, currentDifficulty, getCurrentTimestamp()));
        startActivity(new Intent(this, RankActivity.class));
        finish();
        return true;
    }

    private String normalizeDifficulty(String difficulty) {
        if (difficulty == null) {
            return GameDifficulty.EASY;
        }
        return GameDifficulty.normalize(difficulty.trim().toLowerCase(Locale.ROOT));
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return format.format(new Date());
    }
}
