package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.observer.BombObserver;
import edu.hitsz.observer.BombResult;
import edu.hitsz.strategy.StraightShootStrategy;

/**
 * 精英敌机
 * 可以射击
 */
public class EliteEnemy extends AbstractAircraft implements BombObserver {

    /**
     * 子弹伤害
     */
    private int power = 10;

    /**
     * 子弹射击方向 (向下发射：1，向上发射：-1)
     */
    private int direction = 1;

    /**
     * 精英敌机的分数
     */
    private static final int SCORE = 20;

    public EliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        // 设置直射策略：发射1颗子弹，向下
        this.setShootStrategy(new StraightShootStrategy(1, power, direction, false));
    }

    @Override
    public void forward() {
        super.forward();
        // 判定 y 轴向下飞行出界
        if (locationY >= Main.WINDOW_HEIGHT ) {
            vanish();
        }
    }

    /**
     * 响应炸弹道具
     * 精英敌机直接消失，返回20分
     *
     * @return BombResult 炸弹反应结果
     */
    @Override
    public BombResult reactToBomb() {
        System.out.println("精英敌机被炸弹摧毁！");
        vanish();
        return new BombResult(true, 0, SCORE);
    }

}