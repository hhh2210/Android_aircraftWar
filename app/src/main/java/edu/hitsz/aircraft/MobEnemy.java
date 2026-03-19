package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.observer.BombObserver;
import edu.hitsz.observer.BombResult;
import edu.hitsz.strategy.NoShootStrategy;

/**
 * 普通敌机
 * 不可射击
 *
 * @author hitsz
 */
public class MobEnemy extends AbstractAircraft implements BombObserver {

    /**
     * 普通敌机的分数
     */
    private static final int SCORE = 10;

    public MobEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        // 设置不发射策略
        this.setShootStrategy(new NoShootStrategy());
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
     * 普通敌机直接消失，返回10分
     *
     * @return BombResult 炸弹反应结果
     */
    @Override
    public BombResult reactToBomb() {
        System.out.println("普通敌机被炸弹摧毁！");
        vanish();
        return new BombResult(true, 0, SCORE);
    }

}
