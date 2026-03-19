package edu.hitsz.application.gamemode;

/**
 * 简单模式
 * 特点：
 * 1. 无Boss敌机
 * 2. 难度不随时间增加
 * 3. 敌机数量少
 * 4. 敌机血量和速度较低
 * 5. 英雄机血量较高
 * 6. 精英敌机产生概率较低
 */
public class EasyMode extends AbstractGameMode {
    
    /**
     * 简单模式不会触发Boss
     */
    private static final int INITIAL_BOSS_THRESHOLD = Integer.MAX_VALUE;
    
    public EasyMode() {
        // 初始化参数
    }
    
    @Override
    protected void loadMap() {
        // 使用默认背景图
        this.mapConfig = "images/bg.jpg";
        System.out.println("简单模式：加载地图 - " + mapConfig);
    }
    
    @Override
    protected void configureHero() {
        // 英雄机血量较高：400
        this.heroInitialHp = 400;
        System.out.println("简单模式：配置英雄机 - 初始血量：" + heroInitialHp);
    }
    
    @Override
    protected void configureEnemyFactory() {
        // 敌机数量最大值：3
        this.maxEnemyCount = 3;
        
        // 敌机产生周期较长：800ms
        this.enemySpawnInterval = 800;
        
        // 敌机速度系数：0.7（较慢）
        this.enemySpeedFactor = 0.7;
        
        // 敌机血量系数：0.7（较少）
        this.enemyHpFactor = 0.7;
        
        // 精英敌机产生概率：20%
        this.eliteSpawnRate = 0.2;
        
        // Boss相关（简单模式不出现Boss）
        this.bossScoreThreshold = INITIAL_BOSS_THRESHOLD;
        this.currentBossHp = 0;
        
        System.out.println("简单模式：配置敌机工厂");
        System.out.println("  - 最大敌机数量：" + maxEnemyCount);
        System.out.println("  - 敌机产生周期：" + enemySpawnInterval + "ms");
        System.out.println("  - 敌机速度系数：" + enemySpeedFactor);
        System.out.println("  - 敌机血量系数：" + enemyHpFactor);
        System.out.println("  - 精英敌机概率：" + (eliteSpawnRate * 100) + "%");
    }
    
    @Override
    public void updateDifficultyOverTime(int score, long time) {
        // 简单模式难度不随时间增加，无需实现
    }
    
    @Override
    public boolean checkBossTrigger(int score) {
        // 简单模式不触发Boss
        return false;
    }
    
    @Override
    public int getBossHp() {
        // 简单模式没有Boss
        return 0;
    }
}

