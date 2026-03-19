package edu.hitsz.factory;

import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BulletPlusProp;

/**
 * 超级火力道具工厂
 */
public class BulletPlusPropFactory implements PropFactory {

    @Override
    public AbstractProp create(int locationX, int locationY, int speedX, int speedY) {
        return new BulletPlusProp(locationX, locationY, speedX, speedY);
    }
}

