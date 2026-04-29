package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ScatterShootStrategyTest {

    private static final int HERO_SPAWN_OFFSET_Y = 30;
    private static final int ENEMY_SPAWN_OFFSET_Y = 22;

    private static class StubAircraft extends AbstractAircraft {
        StubAircraft(int x, int y) {
            super(x, y, 0, 0, 100);
        }

        @Override
        public void forward() {
        }
    }

    @Test
    public void shoot_heroBulletsSpawnAboveAircraft() {
        int cx = 200;
        int cy = 300;
        ScatterShootStrategy strategy = new ScatterShootStrategy(3, 10, -1, 2, true);
        List<BaseBullet> bullets = strategy.shoot(new StubAircraft(cx, cy));

        assertEquals(3, bullets.size());
        int expectedY = cy + (-1) * HERO_SPAWN_OFFSET_Y;
        for (BaseBullet b : bullets) {
            assertEquals("Bullet Y should be offset above aircraft", expectedY, b.getLocationY());
            assertTrue("Hero bullet should fly upward", b.getSpeedY() < 0);
        }
    }

    @Test
    public void shoot_enemyBulletsSpawnBelowAircraft() {
        int cx = 200;
        int cy = 100;
        ScatterShootStrategy strategy = new ScatterShootStrategy(3, 10, 1, 2, false);
        List<BaseBullet> bullets = strategy.shoot(new StubAircraft(cx, cy));

        assertEquals(3, bullets.size());
        int expectedY = cy + ENEMY_SPAWN_OFFSET_Y;
        for (BaseBullet b : bullets) {
            assertEquals("Bullet Y should be offset below aircraft", expectedY, b.getLocationY());
        }
    }

    @Test
    public void shoot_singleBulletGoesCenter() {
        ScatterShootStrategy strategy = new ScatterShootStrategy(1, 10, -1, 2, true);
        List<BaseBullet> bullets = strategy.shoot(new StubAircraft(200, 300));

        assertEquals(1, bullets.size());
        assertEquals(200, bullets.get(0).getLocationX());
        assertEquals(0, bullets.get(0).getSpeedX());
    }

    @Test
    public void shoot_bulletCountMatchesParameter() {
        for (int num : new int[]{1, 3, 5, 7}) {
            ScatterShootStrategy strategy = new ScatterShootStrategy(num, 10, -1, 2, true);
            List<BaseBullet> bullets = strategy.shoot(new StubAircraft(200, 200));
            assertEquals("bullet count should match for num=" + num, num, bullets.size());
        }
    }

    @Test
    public void shoot_spreadCreatesHorizontalVariation() {
        ScatterShootStrategy strategy = new ScatterShootStrategy(5, 10, -1, 2, true);
        List<BaseBullet> bullets = strategy.shoot(new StubAircraft(200, 200));

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        for (BaseBullet b : bullets) {
            minX = Math.min(minX, b.getLocationX());
            maxX = Math.max(maxX, b.getLocationX());
        }
        assertTrue("Scatter should create horizontal spread", maxX - minX > 0);
    }
}
