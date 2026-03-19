package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 不发射策略
 * 用于：普通敌机
 * 
 * @author hitsz
 */
public class NoShootStrategy implements ShootStrategy {
    
    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        // 返回空列表，不发射子弹
        return new LinkedList<>();
    }
}

