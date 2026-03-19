package edu.hitsz.factory;

import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BulletProp;

/**
 * 火力道具工厂
 */
public class BulletPropFactory implements PropFactory {

    @Override
    public AbstractProp create(int locationX, int locationY, int speedX, int speedY) {
        return new BulletProp(locationX, locationY, speedX, speedY);
    }
}


