package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.strategy.ShootStrategy;

import java.util.List;

/**
 * 所有种类飞机的抽象父类：
 * 敌机（BOSS, ELITE, MOB），英雄飞机
 *
 * @author hitsz
 */
public abstract class AbstractAircraft extends AbstractFlyingObject {
    /**
     * 生命值
     */
    protected int maxHp;
    protected int hp;

    /**
     * 射击策略
     */
    private ShootStrategy shootStrategy;

    public AbstractAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY);
        this.hp = hp;
        this.maxHp = hp;
    }

    public void decreaseHp(int decrease){
        hp -= decrease;
        if(hp <= 0){
            hp=0;
            vanish();
        }
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    /**
     * 修改最大生命值，当前生命值超过新上限时同步收敛。
     * @param maxHp 新的最大生命值
     */
    public void setMaxHp(int maxHp) {
        this.maxHp = Math.max(0, maxHp);
        if (hp > this.maxHp) {
            hp = this.maxHp;
        }
    }

    /**
     * 重置生命值上限与当前生命值。
     * @param hp 新的生命值
     */
    public void resetHp(int hp) {
        this.maxHp = Math.max(0, hp);
        this.hp = this.maxHp;
        this.isValid = true; // revive when resetting hp
    }

    /**
     * 增加生命值
     * @param increase 增加的生命值
     */
    public void increaseHp(int increase){
        hp += increase;
        if(hp > maxHp){
            hp = maxHp;
        }
    }

    /**
     * 设置射击策略
     * @param shootStrategy 射击策略
     */
    public void setShootStrategy(ShootStrategy shootStrategy) {
        this.shootStrategy = shootStrategy;
    }

    /**
     * 获取射击策略
     * @return 当前射击策略
     */
    public ShootStrategy getShootStrategy() {
        return shootStrategy;
    }

    /**
     * 飞机射击方法，委托给策略对象执行
     * @return 子弹列表
     */
    public List<BaseBullet> shoot() {
        if (shootStrategy == null) {
            throw new IllegalStateException("射击策略未设置！飞机类型：" + this.getClass().getSimpleName());
        }
        return shootStrategy.shoot(this);
    }

}
