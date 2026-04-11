package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 环射策略
 * 用于：Boss敌机、英雄机(超级火力道具)
 * 发射N颗子弹呈360度环形分布
 * 
 * @author hitsz
 */
public class CircleShootStrategy implements ShootStrategy {
    
    /**
     * 子弹发射数量
     */
    private int bulletCount;
    
    /**
     * 子弹伤害
     */
    private int power;
    
    /**
     * 子弹速度
     */
    private int bulletSpeed;
    
    /**
     * 是否为英雄子弹
     */
    private boolean isHero;
    
    /**
     * 构造函数
     * @param bulletCount 子弹数量
     * @param power 子弹伤害
     * @param bulletSpeed 子弹速度
     * @param isHero 是否为英雄子弹
     */
    public CircleShootStrategy(int bulletCount, int power, int bulletSpeed, boolean isHero) {
        this.bulletCount = bulletCount;
        this.power = power;
        this.bulletSpeed = bulletSpeed;
        this.isHero = isHero;
    }
    
    private static final int SPAWN_OFFSET = 40;

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int cx = aircraft.getLocationX();
        int cy = aircraft.getLocationY();

        double angleStep = 2 * Math.PI / bulletCount;

        for (int i = 0; i < bulletCount; i++) {
            double angle = i * angleStep;
            double cosA = Math.cos(angle);
            double sinA = Math.sin(angle);

            int bulletSpeedX = (int) (bulletSpeed * cosA);
            int bulletSpeedY = (int) (bulletSpeed * sinA);
            int spawnX = cx + (int) (SPAWN_OFFSET * cosA);
            int spawnY = cy + (int) (SPAWN_OFFSET * sinA);

            BaseBullet bullet;
            if (isHero) {
                bullet = new HeroBullet(spawnX, spawnY, bulletSpeedX, bulletSpeedY, power);
            } else {
                bullet = new EnemyBullet(spawnX, spawnY, bulletSpeedX, bulletSpeedY, power);
            }
            res.add(bullet);
        }

        return res;
    }
    
    /**
     * 获取子弹数量
     */
    public int getBulletCount() {
        return bulletCount;
    }
    
    /**
     * 设置子弹数量
     */
    public void setBulletCount(int bulletCount) {
        this.bulletCount = bulletCount;
    }
}

