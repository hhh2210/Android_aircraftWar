package edu.hitsz.factory;

import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BloodProp;

/**
 * 加血道具工厂
 */
public class BloodPropFactory implements PropFactory {

    @Override
    public AbstractProp create(int locationX, int locationY, int speedX, int speedY) {
        return new BloodProp(locationX, locationY, speedX, speedY);
    }
}


