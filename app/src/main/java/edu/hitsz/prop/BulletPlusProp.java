package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;

/**
 * 超级火力道具
 * 使英雄机获得环射能力
 * 发射20颗子弹，呈360度环形分布
 */
public class BulletPlusProp extends AbstractProp {

    public BulletPlusProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {
        // 超级火力道具生效，切换到环射模式
        heroAircraft.setCircleMode(20);
        System.out.println("SuperFireSupply active! 环射模式(20发)");
        // 启动定时器，5秒后恢复直射模式
        heroAircraft.startPropEffectTimer();
    }
}

