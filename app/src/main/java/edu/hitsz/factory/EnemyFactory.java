package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractAircraft;

/**
 * 敌机工厂接口：用于创建不同类型的敌机
 */
public interface EnemyFactory {
    /**
     * 创建敌机
     * @param locationX x 坐标
     * @param locationY y 坐标
     * @param speedX x 轴速度
     * @param speedY y 轴速度
     * @param hp 初始生命值
     * @return 敌机实例
     */
    AbstractAircraft create(int locationX, int locationY, int speedX, int speedY, int hp);
}


