package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 散射策略
 * 用于：超级精英敌机、英雄机(火力道具)
 * 发射N颗子弹呈扇形分布
 * 
 * @author hitsz
 */
public class ScatterShootStrategy implements ShootStrategy {
    
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
     * 横向扩散速度
     */
    private int spreadSpeedX;
    
    /**
     * 是否为英雄子弹
     */
    private boolean isHero;
    
    /**
     * 构造函数
     * @param shootNum 子弹发射数量
     * @param power 子弹伤害
     * @param direction 射击方向 (向上：-1，向下：1)
     * @param spreadSpeedX 横向扩散速度
     * @param isHero 是否为英雄子弹
     */
    public ScatterShootStrategy(int shootNum, int power, int direction, int spreadSpeedX, boolean isHero) {
        this.shootNum = shootNum;
        this.power = power;
        this.direction = direction;
        this.spreadSpeedX = spreadSpeedX;
        this.isHero = isHero;
    }
    
    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + direction * 2;
        int speedY = aircraft.getSpeedY() + direction * 5;
        
        BaseBullet bullet;
        
        // 散射弹道：发射多颗子弹，呈扇形分布
        if (shootNum == 1) {
            // 只有一颗子弹，直射
            if (isHero) {
                bullet = new HeroBullet(x, y, 0, speedY, power);
            } else {
                bullet = new EnemyBullet(x, y, 0, speedY, power);
            }
            res.add(bullet);
        } else {
            // 多颗子弹，扇形分布
            for (int i = 0; i < shootNum; i++) {
                // 计算横向速度，形成扇形
                int speedX = (i - shootNum / 2) * spreadSpeedX;
                int bulletX = x + (i - shootNum / 2) * 10;
                
                if (isHero) {
                    bullet = new HeroBullet(bulletX, y, speedX, speedY, power);
                } else {
                    bullet = new EnemyBullet(bulletX, y, speedX, speedY, power);
                }
                res.add(bullet);
            }
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

