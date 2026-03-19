package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 直射策略
 * 用于：精英敌机、英雄机(无道具)
 * 发射1-N颗直线子弹
 * 
 * @author hitsz
 */
public class StraightShootStrategy implements ShootStrategy {
    
    /**
     * 子弹发射数量
     */
    private int shootNum;
    
    /**
     * 子弹伤害
     */
    private int power;
    
    /**
     * 子弹射击方向 (向上发射：-1，向下发射：1)
     */
    private int direction;
    
    /**
     * 是否为英雄子弹
     */
    private boolean isHero;
    
    /**
     * 构造函数
     * @param shootNum 子弹发射数量
     * @param power 子弹伤害
     * @param direction 射击方向 (向上：-1，向下：1)
     * @param isHero 是否为英雄子弹
     */
    public StraightShootStrategy(int shootNum, int power, int direction, boolean isHero) {
        this.shootNum = shootNum;
        this.power = power;
        this.direction = direction;
        this.isHero = isHero;
    }
    
    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + direction * 2;
        int speedX = 0;
        int speedY = aircraft.getSpeedY() + direction * 5;
        
        BaseBullet bullet;
        for (int i = 0; i < shootNum; i++) {
            // 子弹发射位置相对飞机位置向前偏移
            // 多个子弹横向分散
            int bulletX = x + (i * 2 - shootNum + 1) * 10;
            
            if (isHero) {
                bullet = new HeroBullet(bulletX, y, speedX, speedY, power);
            } else {
                bullet = new EnemyBullet(bulletX, y, speedX, speedY, power);
            }
            res.add(bullet);
        }
        
        return res;
    }
    
    /**
     * 获取当前发射数量
     */
    public int getShootNum() {
        return shootNum;
    }
    
    /**
     * 设置发射数量
     */
    public void setShootNum(int shootNum) {
        this.shootNum = shootNum;
    }
}

