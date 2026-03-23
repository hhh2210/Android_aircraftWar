package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_easy).setOnClickListener(view -> launchGame(GameDifficulty.EASY));
        findViewById(R.id.button_normal).setOnClickListener(view -> launchGame(GameDifficulty.NORMAL));
        findViewById(R.id.button_hard).setOnClickListener(view -> launchGame(GameDifficulty.HARD));
    }

    private void launchGame(String difficulty) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameDifficulty.EXTRA_DIFFICULTY, difficulty);
        startActivity(intent);
    }
}
