package edu.hitsz.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.ElitePlusEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.audio.AudioManager;
import edu.hitsz.application.gamemode.AbstractGameMode;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.factory.BloodPropFactory;
import edu.hitsz.factory.BombPropFactory;
import edu.hitsz.factory.BossEnemyFactory;
import edu.hitsz.factory.BulletPlusPropFactory;
import edu.hitsz.factory.BulletPropFactory;
import edu.hitsz.factory.EliteEnemyFactory;
import edu.hitsz.factory.ElitePlusEnemyFactory;
import edu.hitsz.factory.EnemyFactory;
import edu.hitsz.factory.MobEnemyFactory;
import edu.hitsz.factory.PropFactory;
import edu.hitsz.observer.BombObserver;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BombProp;

public class BaseGame extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private static final int FRAME_INTERVAL = 40;

    private final SurfaceHolder surfaceHolder;
    private final AbstractGameMode gameMode;
    private final AudioManager audioManager;
    private final Paint hudPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint noticePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Thread renderThread;
    private volatile boolean running;
    private boolean initialized;
    private boolean gameOver;

    private int backGroundTop;
    private int time;
    private int cycleTime;
    private int cycleDuration = 600;
    private int enemyMaxNumber = 5;
    private int score;
    private boolean bossExists;

    private String floatingNotice;
    private long floatingNoticeExpireAt;

    private HeroAircraft heroAircraft;
    private final List<AbstractAircraft> enemyAircrafts = new LinkedList<>();
    private final List<BaseBullet> heroBullets = new LinkedList<>();
    private final List<BaseBullet> enemyBullets = new LinkedList<>();
    private final List<AbstractProp> props = new LinkedList<>();

    public BaseGame(Context context, AbstractGameMode gameMode) {
        super(context);
        this.gameMode = gameMode;
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        setFocusable(true);
        setKeepScreenOn(true);
        audioManager = new AudioManager(context);
        initPaints();
        gameMode.startGame();
        applyGameModeSettings();
        setOnTouchListener((view, motionEvent) -> handleTouch(motionEvent));
    }

    private void initPaints() {
        hudPaint.setColor(Color.WHITE);
        hudPaint.setTextSize(48f);
        hudPaint.setShadowLayer(6f, 2f, 2f, Color.BLACK);

        noticePaint.setColor(Color.rgb(220, 20, 60));
        noticePaint.setTextSize(52f);
        noticePaint.setTextAlign(Paint.Align.CENTER);
        noticePaint.setShadowLayer(8f, 2f, 2f, Color.BLACK);

        overlayPaint.setColor(Color.argb(170, 0, 0, 0));
    }

    private boolean handleTouch(MotionEvent motionEvent) {
        if (heroAircraft == null) {
            return true;
        }
        int action = motionEvent.getAction();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            float halfWidth = heroAircraft.getWidth() / 2f;
            float halfHeight = heroAircraft.getHeight() / 2f;
            float x = Math.max(halfWidth, Math.min(motionEvent.getX(), Main.WINDOW_WIDTH - halfWidth));
            float y = Math.max(halfHeight, Math.min(motionEvent.getY(), Main.WINDOW_HEIGHT - halfHeight));
            heroAircraft.setLocation(x, y);
            return true;
        }
        return false;
    }

    private void initializeGameIfNeeded() {
        if (initialized || Main.WINDOW_WIDTH <= 0 || Main.WINDOW_HEIGHT <= 0) {
            return;
        }
        ImageManager.init(getContext());
        heroAircraft = HeroAircraft.getInstance(
                Main.WINDOW_WIDTH / 2,
                Main.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight(),
                0,
                0,
                gameMode.getHeroInitialHp()
        );
        heroAircraft.resetHp(gameMode.getHeroInitialHp());
        heroAircraft.setLocation(Main.WINDOW_WIDTH / 2.0, Main.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight());
        heroAircraft.setStraightMode(1);
        heroAircraft.stopPropEffectTimer();
        initialized = true;
    }

    private void applyGameModeSettings() {
        enemyMaxNumber = gameMode.getMaxEnemyCount();
        cycleDuration = gameMode.getEnemySpawnInterval();
        String mapConfig = gameMode.getMapConfig();
        if (mapConfig.contains("bg2")) {
            ImageManager.BACKGROUND_IMAGE = ImageManager.BG2_IMAGE;
        } else if (mapConfig.contains("bg3")) {
            ImageManager.BACKGROUND_IMAGE = ImageManager.BG3_IMAGE;
        } else if (mapConfig.contains("bg4")) {
            ImageManager.BACKGROUND_IMAGE = ImageManager.BG4_IMAGE;
        } else if (mapConfig.contains("bg5")) {
            ImageManager.BACKGROUND_IMAGE = ImageManager.BG5_IMAGE;
        } else {
            ImageManager.BACKGROUND_IMAGE = ImageManager.BG_IMAGE;
        }
    }

    @Override
    public void run() {
        while (running) {
            long start = System.currentTimeMillis();
            if (initialized && !gameOver) {
                updateGame();
            }
            drawFrame();
            long cost = System.currentTimeMillis() - start;
            long sleep = FRAME_INTERVAL - cost;
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private void updateGame() {
        time += FRAME_INTERVAL;
        gameMode.updateDifficultyOverTime(score, time);
        cycleDuration = gameMode.getEnemySpawnInterval();
        String notice = gameMode.pollDifficultyNotice();
        if (notice != null && !notice.isEmpty()) {
            showTemporaryNotice(notice, 3000);
        }

        if (timeCountAndNewCycleJudge()) {
            spawnEnemies();
            shootAction();
        }

        bulletsMoveAction();
        aircraftsMoveAction();
        crashCheckAction();
        postProcessAction();

        if (heroAircraft.getHp() <= 0) {
            gameOver = true;
            heroAircraft.stopPropEffectTimer();
            audioManager.stopBgm();
        }
    }

    private boolean timeCountAndNewCycleJudge() {
        cycleTime += FRAME_INTERVAL;
        if (cycleTime >= cycleDuration) {
            cycleTime %= cycleDuration;
            return true;
        }
        return false;
    }

    private void spawnEnemies() {
        if (!bossExists && gameMode.checkBossTrigger(score)) {
            EnemyFactory factory = new BossEnemyFactory();
            int x = Main.WINDOW_WIDTH / 2;
            int y = Math.max(120, Main.WINDOW_HEIGHT / 6);
            int bossHp = gameMode.getBossHp();
            int bossSpeed = Math.max(3, (int) (5 * gameMode.getEnemySpeedFactor()));
            enemyAircrafts.add(factory.create(x, y, bossSpeed, 0, bossHp));
            bossExists = true;
            gameMode.onBossAppear();
            audioManager.startBossBgm();
        }

        if (enemyAircrafts.size() >= enemyMaxNumber) {
            return;
        }
        double rand = Math.random();
        double eliteRate = gameMode.getEliteSpawnRate();
        double speedFactor = gameMode.getEnemySpeedFactor();
        double hpFactor = gameMode.getEnemyHpFactor();

        if (rand < (1 - eliteRate)) {
            EnemyFactory factory = new MobEnemyFactory();
            int x = randomSpawnX(ImageManager.MOB_ENEMY_IMAGE);
            int y = randomSpawnY();
            enemyAircrafts.add(factory.create(x, y, 0, Math.max(4, (int) (10 * speedFactor)), Math.max(20, (int) (30 * hpFactor))));
        } else if (rand < (1 - eliteRate * 0.3)) {
            EnemyFactory factory = new EliteEnemyFactory();
            int x = randomSpawnX(ImageManager.ELITE_ENEMY_IMAGE);
            int y = randomSpawnY();
            enemyAircrafts.add(factory.create(x, y, Math.max(1, (int) (3 * speedFactor)), Math.max(4, (int) (10 * speedFactor)), Math.max(40, (int) (60 * hpFactor))));
        } else {
            EnemyFactory factory = new ElitePlusEnemyFactory();
            int x = randomSpawnX(ImageManager.ELITE_PLUS_ENEMY_IMAGE);
            int y = randomSpawnY();
            enemyAircrafts.add(factory.create(x, y, Math.max(1, (int) (3 * speedFactor)), Math.max(4, (int) (8 * speedFactor)), Math.max(50, (int) (90 * hpFactor))));
        }
    }

    private int randomSpawnX(Bitmap bitmap) {
        int available = Math.max(1, Main.WINDOW_WIDTH - bitmap.getWidth());
        return bitmap.getWidth() / 2 + (int) (Math.random() * available);
    }

    private int randomSpawnY() {
        return (int) (Math.random() * Main.WINDOW_HEIGHT * 0.08);
    }

    private void shootAction() {
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            enemyBullets.addAll(enemyAircraft.shoot());
        }
        heroBullets.addAll(heroAircraft.shoot());
    }

    private void bulletsMoveAction() {
        for (BaseBullet bullet : heroBullets) {
            bullet.forward();
        }
        for (BaseBullet bullet : enemyBullets) {
            bullet.forward();
        }
    }

    private void aircraftsMoveAction() {
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            enemyAircraft.forward();
        }
        for (AbstractProp prop : props) {
            prop.forward();
        }
    }

    private void crashCheckAction() {
        for (BaseBullet bullet : enemyBullets) {
            if (!bullet.notValid() && heroAircraft.crash(bullet)) {
                heroAircraft.decreaseHp(bullet.getPower());
                bullet.vanish();
                audioManager.playBulletHit();
            }
        }

        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) {
                continue;
            }
            for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                if (enemyAircraft.notValid()) {
                    continue;
                }
                if (enemyAircraft.crash(bullet)) {
                    enemyAircraft.decreaseHp(bullet.getPower());
                    bullet.vanish();
                    audioManager.playBulletHit();
                    if (enemyAircraft.notValid()) {
                        handleEnemyDestroyed(enemyAircraft);
                    }
                }
                if (!enemyAircraft.notValid() && (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft))) {
                    if (enemyAircraft instanceof BossEnemy) {
                        bossExists = false;
                    }
                    enemyAircraft.vanish();
                    heroAircraft.decreaseHp(Integer.MAX_VALUE);
                }
            }
        }

        for (AbstractProp prop : props) {
            if (!prop.notValid() && (heroAircraft.crash(prop) || prop.crash(heroAircraft))) {
                if (prop instanceof BombProp) {
                    BombProp bombProp = (BombProp) prop;
                    for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                        if (!enemyAircraft.notValid() && enemyAircraft instanceof BombObserver) {
                            bombProp.addObserver((BombObserver) enemyAircraft);
                        }
                    }
                    for (BaseBullet bullet : enemyBullets) {
                        if (!bullet.notValid() && bullet instanceof BombObserver) {
                            bombProp.addObserver((BombObserver) bullet);
                        }
                    }
                    bombProp.activate(heroAircraft);
                    audioManager.playBombExplosion();
                    score += bombProp.notifyObservers();
                } else {
                    prop.activate(heroAircraft);
                }
                prop.vanish();
            }
        }
    }

    private void handleEnemyDestroyed(AbstractAircraft enemyAircraft) {
        if (enemyAircraft instanceof MobEnemy) {
            score += 10;
        } else if (enemyAircraft instanceof EliteEnemy) {
            score += 20;
            if (Math.random() < 0.8) {
                createRandomProp(enemyAircraft.getLocationX(), enemyAircraft.getLocationY());
            }
        } else if (enemyAircraft instanceof ElitePlusEnemy) {
            score += 30;
            if (Math.random() < 0.9) {
                createRandomProp(enemyAircraft.getLocationX(), enemyAircraft.getLocationY());
            }
        } else if (enemyAircraft instanceof BossEnemy) {
            score += 100;
            bossExists = false;
            audioManager.startGameBgm();
            int propCount = (int) (Math.random() * 3) + 1;
            for (int i = 0; i < propCount; i++) {
                int offsetX = (int) (Math.random() * 60 - 30);
                int offsetY = (int) (Math.random() * 60 - 30);
                createRandomProp(enemyAircraft.getLocationX() + offsetX, enemyAircraft.getLocationY() + offsetY);
            }
        }
    }

    private void createRandomProp(int locationX, int locationY) {
        AbstractProp prop;
        double randomNum = Math.random();
        if (randomNum < 0.25) {
            PropFactory factory = new BloodPropFactory();
            prop = factory.create(locationX, locationY, 0, 5);
        } else if (randomNum < 0.5) {
            PropFactory factory = new BulletPropFactory();
            prop = factory.create(locationX, locationY, 0, 5);
        } else if (randomNum < 0.75) {
            PropFactory factory = new BulletPlusPropFactory();
            prop = factory.create(locationX, locationY, 0, 5);
        } else {
            PropFactory factory = new BombPropFactory();
            prop = factory.create(locationX, locationY, 0, 5);
        }
        props.add(prop);
    }

    private void postProcessAction() {
        enemyBullets.removeIf(edu.hitsz.basic.AbstractFlyingObject::notValid);
        heroBullets.removeIf(edu.hitsz.basic.AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(edu.hitsz.basic.AbstractFlyingObject::notValid);
        props.removeIf(edu.hitsz.basic.AbstractFlyingObject::notValid);
    }

    private void showTemporaryNotice(String notice, long durationMs) {
        floatingNotice = notice;
        floatingNoticeExpireAt = System.currentTimeMillis() + durationMs;
    }

    private void drawFrame() {
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas == null) {
            return;
        }
        try {
            canvas.drawColor(Color.BLACK);
            if (initialized) {
                drawBackground(canvas);
                drawObjects(canvas, enemyBullets);
                drawObjects(canvas, heroBullets);
                drawObjects(canvas, enemyAircrafts);
                drawObjects(canvas, props);
                drawHero(canvas);
                drawHud(canvas);
                drawFloatingNotice(canvas);
                if (gameOver) {
                    drawGameOver(canvas);
                }
            }
        } finally {
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawBackground(Canvas canvas) {
        Bitmap background = ImageManager.BACKGROUND_IMAGE == null ? ImageManager.BG_IMAGE : ImageManager.BACKGROUND_IMAGE;
        if (background == null) {
            return;
        }
        Rect first = new Rect(0, backGroundTop - Main.WINDOW_HEIGHT, Main.WINDOW_WIDTH, backGroundTop);
        Rect second = new Rect(0, backGroundTop, Main.WINDOW_WIDTH, backGroundTop + Main.WINDOW_HEIGHT);
        canvas.drawBitmap(background, null, first, null);
        canvas.drawBitmap(background, null, second, null);
        backGroundTop += 2;
        if (backGroundTop >= Main.WINDOW_HEIGHT) {
            backGroundTop = 0;
        }
    }

    private void drawObjects(Canvas canvas, List<? extends edu.hitsz.basic.AbstractFlyingObject> objects) {
        for (edu.hitsz.basic.AbstractFlyingObject object : objects) {
            Bitmap image = object.getImage();
            if (image == null) {
                continue;
            }
            float left = object.getLocationX() - image.getWidth() / 2f;
            float top = object.getLocationY() - image.getHeight() / 2f;
            canvas.drawBitmap(image, left, top, null);
        }
    }

    private void drawHero(Canvas canvas) {
        Bitmap hero = ImageManager.HERO_IMAGE;
        if (hero != null) {
            canvas.drawBitmap(hero, heroAircraft.getLocationX() - hero.getWidth() / 2f, heroAircraft.getLocationY() - hero.getHeight() / 2f, null);
        }
    }

    private void drawHud(Canvas canvas) {
        canvas.drawText("SCORE: " + score, 24f, 64f, hudPaint);
        canvas.drawText("LIFE: " + heroAircraft.getHp(), 24f, 124f, hudPaint);
        canvas.drawText(gameMode.getClass().getSimpleName().replace("Mode", ""), 24f, 184f, hudPaint);
    }

    private void drawFloatingNotice(Canvas canvas) {
        if (floatingNotice == null) {
            return;
        }
        if (System.currentTimeMillis() > floatingNoticeExpireAt) {
            floatingNotice = null;
            return;
        }
        canvas.drawText(floatingNotice, Main.WINDOW_WIDTH / 2f, 120f, noticePaint);
    }

    private void drawGameOver(Canvas canvas) {
        canvas.drawRect(0, 0, Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT, overlayPaint);
        noticePaint.setTextSize(72f);
        canvas.drawText("Game Over", Main.WINDOW_WIDTH / 2f, Main.WINDOW_HEIGHT / 2f, noticePaint);
        noticePaint.setTextSize(52f);
        canvas.drawText("Final Score: " + score, Main.WINDOW_WIDTH / 2f, Main.WINDOW_HEIGHT / 2f + 80f, noticePaint);
        canvas.drawText("Tap Back to exit", Main.WINDOW_WIDTH / 2f, Main.WINDOW_HEIGHT / 2f + 160f, noticePaint);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (Main.WINDOW_WIDTH <= 0 || Main.WINDOW_HEIGHT <= 0) {
            Main.WINDOW_WIDTH = Math.max(getWidth(), Main.WINDOW_WIDTH);
            Main.WINDOW_HEIGHT = Math.max(getHeight(), Main.WINDOW_HEIGHT);
        }
        initializeGameIfNeeded();
        if (!gameOver) {
            if (bossExists) {
                audioManager.startBossBgm();
            } else {
                audioManager.startGameBgm();
            }
        }
        running = true;
        renderThread = new Thread(this, "aircraft-war-loop");
        renderThread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Main.WINDOW_WIDTH = width;
        Main.WINDOW_HEIGHT = height;
        initializeGameIfNeeded();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        running = false;
        if (heroAircraft != null) {
            heroAircraft.stopPropEffectTimer();
        }
        if (renderThread != null) {
            try {
                renderThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        audioManager.release();
    }

    public void onHostPause() {
        audioManager.pauseAll();
    }

    public void onHostResume() {
        if (initialized && !gameOver) {
            if (bossExists) {
                audioManager.startBossBgm();
            } else {
                audioManager.startGameBgm();
            }
        }
    }

    public void releaseResources() {
        if (heroAircraft != null) {
            heroAircraft.stopPropEffectTimer();
        }
        audioManager.release();
    }
}
