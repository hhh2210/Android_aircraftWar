package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import edu.hitsz.audio.AudioSettings;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initAudioSwitch();
        findViewById(R.id.button_easy).setOnClickListener(view -> launchGame(GameDifficulty.EASY));
        findViewById(R.id.button_normal).setOnClickListener(view -> launchGame(GameDifficulty.NORMAL));
        findViewById(R.id.button_hard).setOnClickListener(view -> launchGame(GameDifficulty.HARD));
    }

    private void initAudioSwitch() {
        SwitchCompat audioSwitch = findViewById(R.id.switch_music);
        audioSwitch.setChecked(AudioSettings.isAudioEnabled(this));
        audioSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                AudioSettings.setAudioEnabled(this, isChecked)
        );
    }

    private void launchGame(String difficulty) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameDifficulty.EXTRA_DIFFICULTY, difficulty);
        startActivity(intent);
    }
}
