package edu.hitsz.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.HashMap;
import java.util.Map;

import edu.hitsz.R;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.ElitePlusEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import edu.hitsz.prop.BloodProp;
import edu.hitsz.prop.BombProp;
import edu.hitsz.prop.BulletPlusProp;
import edu.hitsz.prop.BulletProp;

public final class ImageManager {

    private static final Map<String, Bitmap> CLASSNAME_IMAGE_MAP = new HashMap<>();

    public static Bitmap BACKGROUND_IMAGE;
    public static Bitmap BG_IMAGE;
    public static Bitmap BG2_IMAGE;
    public static Bitmap BG3_IMAGE;
    public static Bitmap BG4_IMAGE;
    public static Bitmap BG5_IMAGE;
    public static Bitmap HERO_IMAGE;
    public static Bitmap HERO_BULLET_IMAGE;
    public static Bitmap ENEMY_BULLET_IMAGE;
    public static Bitmap MOB_ENEMY_IMAGE;
    public static Bitmap ELITE_ENEMY_IMAGE;
    public static Bitmap ELITE_PLUS_ENEMY_IMAGE;
    public static Bitmap BOSS_ENEMY_IMAGE;
    public static Bitmap PROP_BLOOD_IMAGE;
    public static Bitmap PROP_BOMB_IMAGE;
    public static Bitmap PROP_BULLET_IMAGE;
    public static Bitmap PROP_BULLET_PLUS_IMAGE;

    private static boolean initialized;

    private ImageManager() {
    }

    public static synchronized void init(Context context) {
        if (initialized) {
            return;
        }
        Context appContext = context.getApplicationContext();
        BG_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.bg);
        BG2_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.bg2);
        BG3_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.bg3);
        BG4_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.bg4);
        BG5_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.bg5);
        BACKGROUND_IMAGE = BG_IMAGE;

        HERO_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.hero);
        MOB_ENEMY_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.mob);
        ELITE_ENEMY_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.elite);
        ELITE_PLUS_ENEMY_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.elite_plus);
        BOSS_ENEMY_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.boss);
        HERO_BULLET_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.bullet_hero);
        ENEMY_BULLET_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.bullet_enemy);
        PROP_BLOOD_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.prop_blood);
        PROP_BOMB_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.prop_bomb);
        PROP_BULLET_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.prop_bullet);
        PROP_BULLET_PLUS_IMAGE = BitmapFactory.decodeResource(appContext.getResources(), R.drawable.prop_bullet_plus);

        CLASSNAME_IMAGE_MAP.put(HeroAircraft.class.getName(), HERO_IMAGE);
        CLASSNAME_IMAGE_MAP.put(MobEnemy.class.getName(), MOB_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(EliteEnemy.class.getName(), ELITE_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(ElitePlusEnemy.class.getName(), ELITE_PLUS_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BossEnemy.class.getName(), BOSS_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(HeroBullet.class.getName(), HERO_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(EnemyBullet.class.getName(), ENEMY_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BloodProp.class.getName(), PROP_BLOOD_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BombProp.class.getName(), PROP_BOMB_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BulletProp.class.getName(), PROP_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BulletPlusProp.class.getName(), PROP_BULLET_PLUS_IMAGE);
        initialized = true;
    }

    public static Bitmap get(String className){
        return CLASSNAME_IMAGE_MAP.get(className);
    }

    public static Bitmap get(Object obj){
        if (obj == null){
            return null;
        }
        return get(obj.getClass().getName());
    }
}
