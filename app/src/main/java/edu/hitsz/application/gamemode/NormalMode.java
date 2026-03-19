package edu.hitsz.application.gamemode;

/**
 * 普通模式
 * 特点：
 * 1. 有Boss敌机，每次召唤不改变Boss机血量
 * 2. 难度随时间增加
 * 3. 敌机数量中等
 * 4. 敌机血量和速度中等
 * 5. 英雄机血量中等
 * 6. 精英敌机产生概率中等
 */
public class NormalMode extends AbstractGameMode {
    
    /**
     * Boss出现的初始分数阈值
     */
    private static final int INITIAL_BOSS_THRESHOLD = 500;
    
    /**
     * Boss固定血量
     */
    private static final int BOSS_HP = 300;
    
    /**
     * 上一次Boss出现的分数
     */
    private int lastBossScore = 0;
    
    /**
     * 难度提升的时间间隔（ms）
     */
    private static final long DIFFICULTY_INCREASE_INTERVAL = 30000; // 每30秒提升一次难度
    
    /**
     * 上一次难度提升的时间
     */
    private long lastDifficultyIncreaseTime = 0;
    
    public NormalMode() {
        // 初始化参数
    }
    
    @Override
    protected void loadMap() {
        // 使用背景图2
        this.mapConfig = "images/bg2.jpg";
        System.out.println("普通模式：加载地图 - " + mapConfig);
    }
    
    @Override
    protected void configureHero() {
        // 英雄机血量中等：300
        this.heroInitialHp = 300;
        System.out.println("普通模式：配置英雄机 - 初始血量：" + heroInitialHp);
    }
    
    @Override
    protected void configureEnemyFactory() {
        // 敌机数量最大值：5
        this.maxEnemyCount = 5;
        
        // 敌机产生周期：600ms
        this.enemySpawnInterval = 600;
        
        // 敌机速度系数：1.0（正常）
        this.enemySpeedFactor = 1.0;
        
        // 敌机血量系数：1.0（正常）
        this.enemyHpFactor = 1.0;
        
        // 精英敌机产生概率：30%
        this.eliteSpawnRate = 0.3;
        
        // Boss相关
        this.bossScoreThreshold = INITIAL_BOSS_THRESHOLD;
        this.currentBossHp = BOSS_HP;
        
        System.out.println("普通模式：配置敌机工厂");
        System.out.println("  - 最大敌机数量：" + maxEnemyCount);
        System.out.println("  - 敌机产生周期：" + enemySpawnInterval + "ms");
        System.out.println("  - 敌机速度系数：" + enemySpeedFactor);
        System.out.println("  - 敌机血量系数：" + enemyHpFactor);
        System.out.println("  - 精英敌机概率：" + (eliteSpawnRate * 100) + "%");
        System.out.println("  - Boss分数阈值：" + bossScoreThreshold);
        System.out.println("  - Boss血量：" + currentBossHp);
    }
    
    @Override
    public void updateDifficultyOverTime(int score, long time) {
        // 每30秒提升一次难度
        if (time - lastDifficultyIncreaseTime >= DIFFICULTY_INCREASE_INTERVAL) {
            lastDifficultyIncreaseTime = time;
            
            // 提升敌机速度（最多提升到1.5倍）
            if (enemySpeedFactor < 1.5) {
                enemySpeedFactor += 0.05;
            }
            
            // 提升敌机血量（最多提升到1.5倍）
            if (enemyHpFactor < 1.5) {
                enemyHpFactor += 0.05;
            }
            
            // 缩短敌机产生周期（最短400ms）
            if (enemySpawnInterval > 400) {
                enemySpawnInterval -= 20;
            }
            
            // 提升精英敌机概率（最多40%）
            if (eliteSpawnRate < 0.4) {
                eliteSpawnRate += 0.02;
            }
            
            System.out.println("普通模式：难度提升！");
            System.out.println("  - 敌机速度系数：" + String.format("%.2f", enemySpeedFactor));
            System.out.println("  - 敌机血量系数：" + String.format("%.2f", enemyHpFactor));
            System.out.println("  - 敌机产生周期：" + enemySpawnInterval + "ms");
            System.out.println("  - 精英敌机概率：" + String.format("%.1f%%", eliteSpawnRate * 100));
            setDifficultyNotice(String.format("普通模式难度提升：速度×%.2f 血量×%.2f", enemySpeedFactor, enemyHpFactor));
        }
    }
    
    @Override
    public boolean checkBossTrigger(int score) {
        // 当分数达到阈值且距离上次Boss出现超过阈值时触发
        if (score >= bossScoreThreshold && score - lastBossScore >= bossScoreThreshold) {
            lastBossScore += bossScoreThreshold;
            return true;
        }
        return false;
    }
    
    @Override
    public int getBossHp() {
        // 普通模式Boss血量固定不变
        return BOSS_HP;
    }
}
