package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.observer.BombObserver;
import edu.hitsz.observer.BombResult;
import edu.hitsz.strategy.CircleShootStrategy;

/**
 * Boss敌机
 * 特点：
 * - 分数达到设定阈值时出现，可多次出现
 * - 悬浮于界面上方左右移动
 * - 环射弹道：同时发射20颗子弹，呈环形
 * - 坠毁掉落<=3个道具
 */
public class BossEnemy extends AbstractAircraft implements BombObserver {
    private static final int BOSS_BULLET_SPEED = 10;

    /**
     * 子弹伤害
     */
    private int power = 10;

    /**
     * Boss敌机在屏幕上方的固定Y坐标范围
     */
    private static final int BOSS_Y_MIN = 100;
    private static final int BOSS_Y_MAX = 200;

    /**
     * 构造函数
     */
    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, 0, hp);  // Boss不向下移动，speedY设为0
        this.setShootStrategy(new CircleShootStrategy(12, power, BOSS_BULLET_SPEED, false));
    }

    @Override
    public void forward() {
        // Boss只左右移动
        locationX += speedX;

        // 左右边界检测，碰到边界反弹
        if (locationX <= 0 || locationX >= Main.WINDOW_WIDTH) {
            speedX = -speedX;
        }

        // 确保Boss保持在屏幕上方
        if (locationY > BOSS_Y_MAX) {
            locationY = BOSS_Y_MAX;
        }
    }

    /**
     * 响应炸弹道具
     * Boss敌机不受炸弹影响
     *
     * @return BombResult 炸弹反应结果
     */
    @Override
    public BombResult reactToBomb() {
        System.out.println("Boss敌机不受炸弹影响！");
        return new BombResult(false, 0, 0);
    }

}
