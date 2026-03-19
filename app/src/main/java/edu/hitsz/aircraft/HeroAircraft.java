package edu.hitsz.aircraft;

import edu.hitsz.strategy.StraightShootStrategy;
import edu.hitsz.strategy.ScatterShootStrategy;
import edu.hitsz.strategy.CircleShootStrategy;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;

/**
 * 英雄飞机，游戏玩家操控 (单例模式)
 */
public class HeroAircraft extends AbstractAircraft {

    private static volatile HeroAircraft instance; // 唯一实例

    /**
     * 子弹伤害
     */
    private int power = 30;

    /**
     * 子弹射击方向 (向上发射：-1，向下发射：1)
     */
    private int direction = -1;

    /**
     * 道具效果定时器
     */
    private static ScheduledExecutorService propTimerExecutor = new ScheduledThreadPoolExecutor(1);
    private ScheduledFuture<?> propEffectFuture;

    /**
     * 道具效果持续时间（毫秒）
     */
    private static final long PROP_EFFECT_DURATION = 5000; // 5秒

    /** 私有构造，外部不可直接 new */
    private HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        // 初始化为直射策略：发射1颗子弹
        this.setShootStrategy(new StraightShootStrategy(1, power, direction, true));
    }

    /**
     * 获取唯一实例，首次调用时创建
     */
    public static HeroAircraft getInstance(int locationX, int locationY, int speedX, int speedY, int hp){
        if(instance == null){
            synchronized (HeroAircraft.class){
                if(instance == null){
                    instance = new HeroAircraft(locationX, locationY, speedX, speedY, hp);
                }
            }
        }
        return instance;
    }

    // 可选：不带参数访问，假设已初始化
    public static HeroAircraft getInstance(){
        return instance;
    }

    @Override
    public void forward() {
        // 英雄机由鼠标控制，不通过forward函数移动
    }

    /**
     * 增加火力（增加子弹发射数量）
     * 从直射策略升级到散射策略
     */
    public void increasePower() {
        if (this.getShootStrategy() instanceof StraightShootStrategy) {
            StraightShootStrategy currentStrategy = (StraightShootStrategy) this.getShootStrategy();
            int currentShootNum = currentStrategy.getShootNum();
            // 增加发射数量
            currentStrategy.setShootNum(currentShootNum + 1);
        } else if (this.getShootStrategy() instanceof ScatterShootStrategy) {
            ScatterShootStrategy currentStrategy = (ScatterShootStrategy) this.getShootStrategy();
            int currentShootNum = currentStrategy.getShootNum();
            // 增加发射数量
            currentStrategy.setShootNum(currentShootNum + 1);
        }
    }

    /**
     * 调整基础子弹伤害，重新应用当前射击策略。
     * @param power 新的基础伤害
     */
    public void setPower(int power) {
        this.power = Math.max(1, power);
        // 重新设置当前策略以更新伤害
        if (this.getShootStrategy() instanceof StraightShootStrategy) {
            StraightShootStrategy strategy = (StraightShootStrategy) this.getShootStrategy();
            this.setShootStrategy(new StraightShootStrategy(strategy.getShootNum(), this.power, direction, true));
        } else if (this.getShootStrategy() instanceof ScatterShootStrategy) {
            ScatterShootStrategy strategy = (ScatterShootStrategy) this.getShootStrategy();
            this.setShootStrategy(new ScatterShootStrategy(strategy.getShootNum(), this.power, direction, 2, true));
        } else if (this.getShootStrategy() instanceof CircleShootStrategy) {
            CircleShootStrategy strategy = (CircleShootStrategy) this.getShootStrategy();
            this.setShootStrategy(new CircleShootStrategy(strategy.getBulletCount(), this.power, 5, true));
        }
    }

    /**
     * 设置为散射模式（火力道具）
     * @param shootNum 子弹数量
     */
    public void setScatterMode(int shootNum) {
        this.setShootStrategy(new ScatterShootStrategy(shootNum, power, direction, 2, true));
    }

    /**
     * 设置为环射模式（超级火力道具）
     * @param bulletCount 子弹数量
     */
    public void setCircleMode(int bulletCount) {
        this.setShootStrategy(new CircleShootStrategy(bulletCount, power, 5, true));
    }

    /**
     * 重置为直射模式
     * @param shootNum 子弹数量
     */
    public void setStraightMode(int shootNum) {
        this.setShootStrategy(new StraightShootStrategy(shootNum, power, direction, true));
    }

    /**
     * 启动道具效果定时器
     * 道具效果持续一段时间后自动恢复为直射模式
     */
    public void startPropEffectTimer() {
        // 如果之前有定时任务，先取消
        if (propEffectFuture != null && !propEffectFuture.isDone()) {
            propEffectFuture.cancel(false);
        }

        // 启动新的定时任务
        propEffectFuture = propTimerExecutor.schedule(() -> {
            // 恢复为直射模式（1发子弹）
            setStraightMode(1);
            System.out.println("道具效果结束，恢复直射模式");
        }, PROP_EFFECT_DURATION, TimeUnit.MILLISECONDS);
    }

    /**
     * 停止道具效果定时器
     */
    public void stopPropEffectTimer() {
        if (propEffectFuture != null && !propEffectFuture.isDone()) {
            propEffectFuture.cancel(false);
        }
    }

}
