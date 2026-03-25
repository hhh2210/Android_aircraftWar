package edu.hitsz;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import edu.hitsz.application.BaseGame;
import edu.hitsz.application.EasyGame;
import edu.hitsz.application.HardGame;
import edu.hitsz.application.NormalGame;

public class GameActivity extends AppCompatActivity {

    private BaseGame gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String difficulty = normalizeDifficulty(getIntent().getStringExtra(GameDifficulty.EXTRA_DIFFICULTY));
        gameView = createGameViewByDifficulty(difficulty);
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
        super.onDestroy();
    }

    BaseGame createGameViewByDifficulty(String difficulty) {
        switch (difficulty) {
            case GameDifficulty.NORMAL:
                return new NormalGame(this);
            case GameDifficulty.HARD:
                return new HardGame(this);
            case GameDifficulty.EASY:
            default:
                return new EasyGame(this);
        }
    }

    private String normalizeDifficulty(String difficulty) {
        if (difficulty == null) {
            return GameDifficulty.EASY;
        }
        return GameDifficulty.normalize(difficulty.trim().toLowerCase(Locale.ROOT));
    }
}
