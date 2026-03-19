package edu.hitsz.observer;

/**
 * 炸弹观察者接口
 * 所有需要响应炸弹道具的对象都应实现此接口
 *
 * @author hitsz
 */
public interface BombObserver {
    /**
     * 响应炸弹道具生效的方法
     * 不同的观察者对炸弹有不同的反应：
     * - 普通敌机：直接消失，返回分数
     * - 精英敌机：直接消失，返回分数
     * - 超级精英敌机：血量减少，可能消失并返回分数
     * - Boss敌机：不受影响
     * - 敌机子弹：直接消失
     *
     * @return BombResult 炸弹反应结果，包含是否消失、造成的伤害和获得的分数
     */
    BombResult reactToBomb();
}

