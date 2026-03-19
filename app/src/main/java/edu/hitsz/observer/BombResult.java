package edu.hitsz.observer;

/**
 * 炸弹反应结果类
 * 封装观察者对炸弹道具的反应结果
 *
 * @author hitsz
 */
public class BombResult {
    /**
     * 对象是否应该消失
     */
    private final boolean shouldVanish;

    /**
     * 造成的伤害值
     */
    private final int damageDealt;

    /**
     * 获得的分数
     */
    private final int scoreGained;

    /**
     * 构造函数
     *
     * @param shouldVanish 对象是否应该消失
     * @param damageDealt 造成的伤害值
     * @param scoreGained 获得的分数
     */
    public BombResult(boolean shouldVanish, int damageDealt, int scoreGained) {
        this.shouldVanish = shouldVanish;
        this.damageDealt = damageDealt;
        this.scoreGained = scoreGained;
    }

    /**
     * 获取对象是否应该消失
     *
     * @return 是否应该消失
     */
    public boolean isShouldVanish() {
        return shouldVanish;
    }

    /**
     * 获取造成的伤害值
     *
     * @return 伤害值
     */
    public int getDamageDealt() {
        return damageDealt;
    }

    /**
     * 获取获得的分数
     *
     * @return 分数
     */
    public int getScoreGained() {
        return scoreGained;
    }
}

