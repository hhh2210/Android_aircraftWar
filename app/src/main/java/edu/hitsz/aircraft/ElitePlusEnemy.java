package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.observer.BombObserver;
import edu.hitsz.observer.BombResult;
import edu.hitsz.strategy.ScatterShootStrategy;

/**
 * 超级精英敌机
 * 特点：
 * - 每隔一定周期随机产生
 * - 向屏幕下方左右移动
 * - 散射弹道：同时发射3颗子弹，呈扇形
 * - 坠毁掉落<=1个道具
 */
public class ElitePlusEnemy extends AbstractAircraft implements BombObserver {
    private static final int SCATTER_SPREAD_SPEED = 3;

    /**
     * 子弹伤害
     */
    private int power = 30;

    /**
     * 子弹射击方向 (向下发射：1)
     */
    private int direction = 1;

    /**
     * 炸弹对超级精英敌机造成的伤害
     */
    private static final int BOMB_DAMAGE = 30;

    /**
     * 超级精英敌机的分数
     */
    private static final int SCORE = 30;

    public ElitePlusEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.setShootStrategy(new ScatterShootStrategy(3, power, direction, SCATTER_SPREAD_SPEED, false));
    }

    @Override
    public void forward() {
        super.forward();
        // 判定 y 轴向下飞行出界
        if (locationY >= Main.WINDOW_HEIGHT) {
            vanish();
        }
    }

    /**
     * 响应炸弹道具
     * 超级精英敌机血量减少30，如果血量<=0则消失并返回30分
     *
     * @return BombResult 炸弹反应结果
     */
    @Override
    public BombResult reactToBomb() {
        decreaseHp(BOMB_DAMAGE);
        boolean vanished = notValid();
        if (vanished) {
            System.out.println("超级精英敌机被炸弹摧毁！");
            return new BombResult(true, BOMB_DAMAGE, SCORE);
        } else {
            System.out.println("超级精英敌机受到炸弹伤害，剩余血量：" + getHp());
            return new BombResult(false, BOMB_DAMAGE, 0);
        }
    }

}
