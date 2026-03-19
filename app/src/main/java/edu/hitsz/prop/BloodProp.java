package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;

/**
 * 加血道具
 * 使英雄机恢复一定血量，但不超过初始值
 */
public class BloodProp extends AbstractProp {

    /**
     * 恢复血量值
     */
    private int healAmount = 30;

    public BloodProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {
        // 恢复英雄机血量，但不超过最大血量
        int currentHp = heroAircraft.getHp();
        int maxHp = heroAircraft.getMaxHp();
        int newHp = Math.min(currentHp + healAmount, maxHp);
        heroAircraft.increaseHp(newHp - currentHp);
        System.out.println("BloodSupply active! HP: " + currentHp + " -> " + newHp);
    }
}