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
    
    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + 20;  // 子弹从飞机下方发出
        
        // 环射弹道：发射N颗子弹，呈360度环形分布
        double angleStep = 2 * Math.PI / bulletCount;  // 每颗子弹之间的角度差
        
        BaseBullet bullet;
        for (int i = 0; i < bulletCount; i++) {
            double angle = i * angleStep;
            // 计算每颗子弹的速度分量
            int bulletSpeedX = (int) (bulletSpeed * Math.cos(angle));
            int bulletSpeedY = (int) (bulletSpeed * Math.sin(angle));
            
            if (isHero) {
                bullet = new HeroBullet(x, y, bulletSpeedX, bulletSpeedY, power);
            } else {
                bullet = new EnemyBullet(x, y, bulletSpeedX, bulletSpeedY, power);
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

