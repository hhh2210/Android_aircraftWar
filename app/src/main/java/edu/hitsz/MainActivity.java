package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import edu.hitsz.audio.AudioSettings;
import edu.hitsz.rank.RankActivity;

public class MainActivity extends AppCompatActivity {

    private boolean onlineModeSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initAudioSwitch();
        initModeSelection();
        findViewById(R.id.button_easy).setOnClickListener(view -> launchGame(GameDifficulty.EASY));
        findViewById(R.id.button_normal).setOnClickListener(view -> launchGame(GameDifficulty.NORMAL));
        findViewById(R.id.button_hard).setOnClickListener(view -> launchGame(GameDifficulty.HARD));
        findViewById(R.id.button_rank).setOnClickListener(view ->
                startActivity(new Intent(this, RankActivity.class)));
    }

    private void initAudioSwitch() {
        SwitchCompat audioSwitch = findViewById(R.id.switch_music);
        audioSwitch.setChecked(AudioSettings.isAudioEnabled(this));
        audioSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                AudioSettings.setAudioEnabled(this, isChecked)
        );
    }

    private void initModeSelection() {
        updateModeSelectionUi();
        findViewById(R.id.mode_offline).setOnClickListener(v -> {
            onlineModeSelected = false;
            updateModeSelectionUi();
            Toast.makeText(this, R.string.mode_offline_selected, Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.mode_online).setOnClickListener(v ->
        {
            onlineModeSelected = true;
            updateModeSelectionUi();
            Toast.makeText(this, R.string.mode_online_selected, Toast.LENGTH_SHORT).show();
        });
    }

    private void launchGame(String difficulty) {
        if (onlineModeSelected) {
            Intent onlineIntent = new Intent(this, OnlineGameActivity.class);
            onlineIntent.putExtra(GameDifficulty.EXTRA_DIFFICULTY, difficulty);
            startActivity(onlineIntent);
            return;
        }
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameDifficulty.EXTRA_DIFFICULTY, difficulty);
        startActivity(intent);
    }

    private void updateModeSelectionUi() {
        findViewById(R.id.mode_offline).setAlpha(onlineModeSelected ? 0.45f : 1f);
        findViewById(R.id.mode_online).setAlpha(onlineModeSelected ? 1f : 0.45f);
    }
}
