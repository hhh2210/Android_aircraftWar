package edu.hitsz.bullet;

import edu.hitsz.observer.BombObserver;
import edu.hitsz.observer.BombResult;

/**
 * 敌机子弹
 * @Author hitsz
 */
public class EnemyBullet extends BaseBullet implements BombObserver {

    public EnemyBullet(int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY, power);
    }

    /**
     * 响应炸弹道具
     * 敌机子弹直接消失，不返回分数
     *
     * @return BombResult 炸弹反应结果
     */
    @Override
    public BombResult reactToBomb() {
        vanish();
        return new BombResult(true, 0, 0);
    }

}
