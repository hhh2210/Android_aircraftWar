package edu.hitsz.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

import edu.hitsz.R;

public class AudioManager {

    private static final float DEFAULT_VOLUME = 0.6f;
    private static final int MAX_SOUND_STREAMS = 4;

    private final Context appContext;

    private MediaPlayer bgmPlayer;
    private SoundPool soundPool;
    private int currentBgmResId;
    private int bulletHitSoundId;
    private int bombExplosionSoundId;
    private boolean effectsLoaded;
    private int loadedEffectCount;

    public AudioManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public void startGameBgm() {
        startLoopingBgm(R.raw.bgm_game);
    }

    public void startBossBgm() {
        startLoopingBgm(R.raw.bgm_boss);
    }

    public void stopBgm() {
        if (bgmPlayer != null) {
            if (bgmPlayer.isPlaying()) {
                bgmPlayer.stop();
            }
            bgmPlayer.release();
            bgmPlayer = null;
        }
        currentBgmResId = 0;
    }

    public void pauseAll() {
        if (bgmPlayer != null && bgmPlayer.isPlaying()) {
            bgmPlayer.pause();
        }
    }

    public void resumeAll() {
        if (!AudioSettings.isAudioEnabled(appContext) || bgmPlayer == null) {
            return;
        }
        if (!bgmPlayer.isPlaying()) {
            bgmPlayer.start();
        }
    }

    public void playBulletHit() {
        playEffect(ensureSoundPoolLoaded().bulletHitSoundId);
    }

    public void playBombExplosion() {
        playEffect(ensureSoundPoolLoaded().bombExplosionSoundId);
    }

    public void release() {
        stopBgm();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        bulletHitSoundId = 0;
        bombExplosionSoundId = 0;
        effectsLoaded = false;
        loadedEffectCount = 0;
    }

    private void startLoopingBgm(int bgmResId) {
        if (!AudioSettings.isAudioEnabled(appContext)) {
            stopBgm();
            return;
        }
        if (currentBgmResId == bgmResId && bgmPlayer != null) {
            if (!bgmPlayer.isPlaying()) {
                bgmPlayer.start();
            }
            return;
        }
        stopBgm();
        MediaPlayer player = MediaPlayer.create(appContext, bgmResId);
        if (player == null) {
            currentBgmResId = 0;
            return;
        }
        player.setLooping(true);
        player.setVolume(DEFAULT_VOLUME, DEFAULT_VOLUME);
        player.start();
        bgmPlayer = player;
        currentBgmResId = bgmResId;
    }

    private void playEffect(int soundId) {
        if (!AudioSettings.isAudioEnabled(appContext) || soundPool == null || soundId == 0 || !effectsLoaded) {
            return;
        }
        soundPool.play(soundId, DEFAULT_VOLUME, DEFAULT_VOLUME, 1, 0, 1.0f);
    }

    private AudioManager ensureSoundPoolLoaded() {
        if (soundPool != null) {
            return this;
        }
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(MAX_SOUND_STREAMS)
                .build();
        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            if (status == 0) {
                loadedEffectCount++;
                effectsLoaded = loadedEffectCount >= 2;
            }
        });
        bulletHitSoundId = soundPool.load(appContext, R.raw.sfx_bullet_hit, 1);
        bombExplosionSoundId = soundPool.load(appContext, R.raw.sfx_bomb_explosion, 1);
        return this;
    }
}
