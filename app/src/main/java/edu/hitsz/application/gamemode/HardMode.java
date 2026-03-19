package edu.hitsz.application.gamemode;

/**
 * 困难模式
 * 特点：
 * 1. 有Boss敌机，每次召唤提升Boss机血量
 * 2. 难度随时间增加（比普通模式更快）
 * 3. 敌机数量多
 * 4. 敌机血量和速度较高
 * 5. 英雄机血量较低
 * 6. 精英敌机产生概率较高
 */
public class HardMode extends AbstractGameMode {
    
    /**
     * Boss出现的初始分数阈值
     */
    private static final int INITIAL_BOSS_THRESHOLD = 300;
    
    /**
     * Boss初始血量
     */
    private static final int INITIAL_BOSS_HP = 350;
    
    /**
     * Boss每次出现血量增加值
     */
    private static final int BOSS_HP_INCREMENT = 50;
    
    /**
     * 上一次Boss出现的分数
     */
    private int lastBossScore = 0;
    
    /**
     * 难度提升的时间间隔（ms）
     */
    private static final long DIFFICULTY_INCREASE_INTERVAL = 20000; // 每20秒提升一次难度
    
    /**
     * 上一次难度提升的时间
     */
    private long lastDifficultyIncreaseTime = 0;
    
    public HardMode() {
        // 初始化参数
    }
    
    @Override
    protected void loadMap() {
        // 使用背景图3
        this.mapConfig = "images/bg3.jpg";
        System.out.println("困难模式：加载地图 - " + mapConfig);
    }
    
    @Override
    protected void configureHero() {
        // 英雄机血量较低：200
        this.heroInitialHp = 200;
        System.out.println("困难模式：配置英雄机 - 初始血量：" + heroInitialHp);
    }
    
    @Override
    protected void configureEnemyFactory() {
        // 敌机数量最大值：7
        this.maxEnemyCount = 7;
        
        // 敌机产生周期较短：400ms
        this.enemySpawnInterval = 400;
        
        // 敌机速度系数：1.3（较快）
        this.enemySpeedFactor = 1.3;
        
        // 敌机血量系数：1.3（较多）
        this.enemyHpFactor = 1.3;
        
        // 精英敌机产生概率：40%
        this.eliteSpawnRate = 0.4;
        
        // Boss相关
        this.bossScoreThreshold = INITIAL_BOSS_THRESHOLD;
        this.currentBossHp = INITIAL_BOSS_HP;
        
        System.out.println("困难模式：配置敌机工厂");
        System.out.println("  - 最大敌机数量：" + maxEnemyCount);
        System.out.println("  - 敌机产生周期：" + enemySpawnInterval + "ms");
        System.out.println("  - 敌机速度系数：" + enemySpeedFactor);
        System.out.println("  - 敌机血量系数：" + enemyHpFactor);
        System.out.println("  - 精英敌机概率：" + (eliteSpawnRate * 100) + "%");
        System.out.println("  - Boss分数阈值：" + bossScoreThreshold);
        System.out.println("  - Boss初始血量：" + currentBossHp);
    }
    
    @Override
    public void updateDifficultyOverTime(int score, long time) {
        // 每20秒提升一次难度（比普通模式更频繁）
        if (time - lastDifficultyIncreaseTime >= DIFFICULTY_INCREASE_INTERVAL) {
            lastDifficultyIncreaseTime = time;
            
            // 提升敌机速度（最多提升到2.0倍）
            if (enemySpeedFactor < 2.0) {
                enemySpeedFactor += 0.08;
            }
            
            // 提升敌机血量（最多提升到2.0倍）
            if (enemyHpFactor < 2.0) {
                enemyHpFactor += 0.08;
            }
            
            // 缩短敌机产生周期（最短300ms）
            if (enemySpawnInterval > 300) {
                enemySpawnInterval -= 20;
            }
            
            // 提升精英敌机概率（最多60%）
            if (eliteSpawnRate < 0.6) {
                eliteSpawnRate += 0.03;
            }
            
            System.out.println("困难模式：难度大幅提升！");
            System.out.println("  - 敌机速度系数：" + String.format("%.2f", enemySpeedFactor));
            System.out.println("  - 敌机血量系数：" + String.format("%.2f", enemyHpFactor));
            System.out.println("  - 敌机产生周期：" + enemySpawnInterval + "ms");
            System.out.println("  - 精英敌机概率：" + String.format("%.1f%%", eliteSpawnRate * 100));
            setDifficultyNotice(String.format("困难模式难度+1：速度×%.2f 血量×%.2f", enemySpeedFactor, enemyHpFactor));
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
        // 困难模式：每次Boss出现，血量递增
        int hp = INITIAL_BOSS_HP + (bossAppearCount * BOSS_HP_INCREMENT);
        System.out.println("困难模式：Boss血量提升至 " + hp + "（第" + (bossAppearCount + 1) + "次出现）");
        return hp;
    }
}
