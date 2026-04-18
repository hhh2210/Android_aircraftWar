package edu.hitsz;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import edu.hitsz.audio.AudioSettings;
import edu.hitsz.rank.RankActivity;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_ONLINE_CONFIG = "online_config";
    private static final String KEY_HOST = "host";
    private static final String KEY_PORT = "port";

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
        findViewById(R.id.mode_online).setOnClickListener(v -> {
            onlineModeSelected = true;
            updateModeSelectionUi();
            Toast.makeText(this, R.string.mode_online_selected, Toast.LENGTH_SHORT).show();
        });
    }

    private void launchGame(String difficulty) {
        if (onlineModeSelected) {
            showOnlineConfigDialog(difficulty);
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

    private void showOnlineConfigDialog(String difficulty) {
        EditText hostInput = new EditText(this);
        hostInput.setHint(R.string.online_dialog_host_hint);
        hostInput.setInputType(InputType.TYPE_CLASS_TEXT);
        hostInput.setSingleLine(true);
        hostInput.setText(getSharedPreferences(PREF_ONLINE_CONFIG, MODE_PRIVATE)
                .getString(KEY_HOST, OnlineGameActivity.DEFAULT_HOST));

        EditText portInput = new EditText(this);
        portInput.setHint(R.string.online_dialog_port_hint);
        portInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        portInput.setSingleLine(true);
        portInput.setText(String.valueOf(getSharedPreferences(PREF_ONLINE_CONFIG, MODE_PRIVATE)
                .getInt(KEY_PORT, OnlineGameActivity.DEFAULT_PORT)));

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int paddingPx = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(paddingPx, paddingPx / 2, paddingPx, 0);
        container.addView(hostInput);
        container.addView(portInput);

        new AlertDialog.Builder(this)
                .setTitle(R.string.online_dialog_title)
                .setView(container)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.online_dialog_confirm, (dialog, which) -> {
                    String host = hostInput.getText().toString().trim();
                    String portText = portInput.getText().toString().trim();
                    if (host.isEmpty()) {
                        host = OnlineGameActivity.DEFAULT_HOST;
                    }
                    int port;
                    try {
                        port = Integer.parseInt(portText);
                    } catch (NumberFormatException exception) {
                        Toast.makeText(this, R.string.online_dialog_invalid_port, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    getSharedPreferences(PREF_ONLINE_CONFIG, MODE_PRIVATE)
                            .edit()
                            .putString(KEY_HOST, host)
                            .putInt(KEY_PORT, port)
                            .apply();
                    Intent intent = new Intent(this, OnlineGameActivity.class);
                    intent.putExtra(GameDifficulty.EXTRA_DIFFICULTY, difficulty);
                    intent.putExtra(OnlineGameActivity.EXTRA_HOST, host);
                    intent.putExtra(OnlineGameActivity.EXTRA_PORT, port);
                    startActivity(intent);
                })
                .show();
    }
}
