package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.ElitePlusEnemy;

/**
 * 超级精英敌机工厂
 */
public class ElitePlusEnemyFactory implements EnemyFactory {

    @Override
    public AbstractAircraft create(int locationX, int locationY, int speedX, int speedY, int hp) {
        return new ElitePlusEnemy(locationX, locationY, speedX, speedY, hp);
    }
}