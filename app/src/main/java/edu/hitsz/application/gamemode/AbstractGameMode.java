package edu.hitsz.application.gamemode;

/**
 * 抽象游戏模式类 - 模板模式
 * 定义游戏难度的模板方法和钩子方法
 * 
 * 难度设计因素：
 * 1. 敌机数量的最大值
 * 2. 敌机的属性值（血量、速度）
 * 3. 英雄机的初始血量
 * 4. 敌机的射击周期
 * 5. 精英敌机的产生概率
 * 6. 普通和精英敌机的产生周期
 * 7. Boss敌机产生的分数阈值
 * 8. Boss敌机的血量
 */
public abstract class AbstractGameMode {
    
    /**
     * 地图配置（背景图片）
     */
    protected String mapConfig;
    
    /**
     * 敌机产生周期（ms）
     */
    protected int enemySpawnInterval;
    
    /**
     * Boss出现的分数阈值
     */
    protected int bossScoreThreshold;
    
    /**
     * 敌机速度系数
     */
    protected double enemySpeedFactor;
    
    /**
     * 敌机血量系数
     */
    protected double enemyHpFactor;
    
    /**
     * 屏幕中敌机的最大数量
     */
    protected int maxEnemyCount;
    
    /**
     * 精英敌机产生概率（0-1之间）
     */
    protected double eliteSpawnRate;
    
    /**
     * 英雄机初始血量
     */
    protected int heroInitialHp;
    
    /**
     * 当前Boss血量（困难模式下会动态提升）
     */
    protected int currentBossHp;
    
    /**
     * Boss已出现次数
     */
    protected int bossAppearCount;
    
    /**
     * 游戏开始时间
     */
    protected long gameStartTime;

    /**
     * 难度提升提示（供界面显示）
     */
    private String difficultyNotice;
    
    /**
     * 模板方法：启动游戏
     * 定义游戏初始化的基本流程
     */
    public final void startGame() {
        gameStartTime = System.currentTimeMillis();
        bossAppearCount = 0;
        loadMap();
        configureHero();
        configureEnemyFactory();
        System.out.println("游戏模式：" + this.getClass().getSimpleName() + " 已启动");
    }
    
    /**
     * 加载地图（钩子方法）
     * 子类可以选择不同的背景
     */
    protected abstract void loadMap();
    
    /**
     * 配置英雄机（钩子方法）
     * 不同难度下英雄机的初始属性不同
     */
    protected abstract void configureHero();
    
    /**
     * 配置敌机工厂参数（钩子方法）
     * 不同难度下敌机的生成参数不同
     */
    protected abstract void configureEnemyFactory();
    
    /**
     * 随时间更新难度（钩子方法）
     * 普通和困难模式会随时间提升难度
     * 
     * @param score 当前得分
     * @param time 游戏运行时间（ms）
     */
    public abstract void updateDifficultyOverTime(int score, long time);
    
    /**
     * 检查是否应该触发Boss（钩子方法）
     * 简单模式返回false，普通和困难模式根据分数判断
     * 
     * @param score 当前得分
     * @return 是否应该生成Boss
     */
    public abstract boolean checkBossTrigger(int score);
    
    /**
     * 获取Boss血量（钩子方法）
     * 普通模式返回固定值，困难模式返回递增值
     * 
     * @return Boss血量
     */
    public abstract int getBossHp();
    
    // Getter 方法
    
    public String getMapConfig() {
        return mapConfig;
    }
    
    public int getEnemySpawnInterval() {
        return enemySpawnInterval;
    }
    
    public int getBossScoreThreshold() {
        return bossScoreThreshold;
    }
    
    public double getEnemySpeedFactor() {
        return enemySpeedFactor;
    }
    
    public double getEnemyHpFactor() {
        return enemyHpFactor;
    }
    
    public int getMaxEnemyCount() {
        return maxEnemyCount;
    }
    
    public double getEliteSpawnRate() {
        return eliteSpawnRate;
    }
    
    public int getHeroInitialHp() {
        return heroInitialHp;
    }
    
    public int getCurrentBossHp() {
        return currentBossHp;
    }
    
    public int getBossAppearCount() {
        return bossAppearCount;
    }
    
    /**
     * 设置难度提示信息
     * @param notice 提示文本
     */
    protected void setDifficultyNotice(String notice) {
        this.difficultyNotice = notice;
    }

    /**
     * 获取并清空难度提示信息（一次性）
     * @return 提示文本，若无则为null
     */
    public String pollDifficultyNotice() {
        String notice = difficultyNotice;
        difficultyNotice = null;
        return notice;
    }
    
    /**
     * Boss出现后调用，更新出现次数
     */
    public void onBossAppear() {
        bossAppearCount++;
    }
}
