package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.strategy.StraightShootStrategy;
import edu.hitsz.strategy.ScatterShootStrategy;

/**
 * 火力道具
 * 使英雄机升级射击策略：
 * - 直射 -> 散射（3发）
 * - 散射 -> 增加子弹数量
 */
public class BulletProp extends AbstractProp {

    public BulletProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {
        // 火力道具生效，升级射击策略
        if (heroAircraft.getShootStrategy() instanceof StraightShootStrategy) {
            // 从直射升级到散射（3发）
            heroAircraft.setScatterMode(3);
            System.out.println("FireSupply active! 直射 -> 散射(3发)");
            // 启动定时器，5秒后恢复直射模式
            heroAircraft.startPropEffectTimer();
        } else if (heroAircraft.getShootStrategy() instanceof ScatterShootStrategy) {
            // 如果已经是散射，增加子弹数量
            heroAircraft.increasePower();
            System.out.println("FireSupply active! 散射子弹数量+1");
            // 重新启动定时器
            heroAircraft.startPropEffectTimer();
        } else {
            // 其他情况，增加火力
            heroAircraft.increasePower();
            System.out.println("FireSupply active! 火力增强");
            // 重新启动定时器
            heroAircraft.startPropEffectTimer();
        }
    }
}