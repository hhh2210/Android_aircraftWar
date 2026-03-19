package edu.hitsz.factory;

import edu.hitsz.prop.AbstractProp;

/**
 * 道具工厂接口：用于创建不同类型的道具
 */
public interface PropFactory {
    /**
     * 创建道具
     * @param locationX x 坐标
     * @param locationY y 坐标
     * @param speedX x 轴速度
     * @param speedY y 轴速度
     * @return 道具实例
     */
    AbstractProp create(int locationX, int locationY, int speedX, int speedY);
}


