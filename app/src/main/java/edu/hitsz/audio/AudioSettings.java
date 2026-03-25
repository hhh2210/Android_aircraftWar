package edu.hitsz.audio;

import android.content.Context;
import android.content.SharedPreferences;

public final class AudioSettings {

    private static final String PREFS_NAME = "audio_settings";
    private static final String KEY_AUDIO_ENABLED = "audio_enabled";

    private AudioSettings() {
    }

    public static boolean isAudioEnabled(Context context) {
        return getPreferences(context).getBoolean(KEY_AUDIO_ENABLED, true);
    }

    public static void setAudioEnabled(Context context, boolean enabled) {
        getPreferences(context).edit().putBoolean(KEY_AUDIO_ENABLED, enabled).apply();
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
