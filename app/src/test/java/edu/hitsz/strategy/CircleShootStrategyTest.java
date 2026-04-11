package edu.hitsz.strategy;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CircleShootStrategyTest {

    private static final int SPAWN_OFFSET = 40;

    private static class StubAircraft extends AbstractAircraft {
        StubAircraft(int x, int y) {
            super(x, y, 0, 0, 100);
        }

        @Override
        public void forward() {
        }
    }

    @Test
    public void shoot_bulletsSpawnOutsideAircraftCenter() {
        int cx = 200;
        int cy = 300;
        CircleShootStrategy strategy = new CircleShootStrategy(8, 10, 5, true);
        List<BaseBullet> bullets = strategy.shoot(new StubAircraft(cx, cy));

        assertEquals(8, bullets.size());
        for (BaseBullet b : bullets) {
            double dx = b.getLocationX() - cx;
            double dy = b.getLocationY() - cy;
            double dist = Math.sqrt(dx * dx + dy * dy);
            assertTrue("Bullet spawned too close to aircraft center: dist=" + dist,
                    dist >= SPAWN_OFFSET - 1);
        }
    }

    @Test
    public void shoot_singleBulletStillOffsets() {
        CircleShootStrategy strategy = new CircleShootStrategy(1, 10, 5, true);
        List<BaseBullet> bullets = strategy.shoot(new StubAircraft(100, 100));

        assertEquals(1, bullets.size());
        BaseBullet b = bullets.get(0);
        double dx = b.getLocationX() - 100;
        double dy = b.getLocationY() - 100;
        double dist = Math.sqrt(dx * dx + dy * dy);
        assertTrue("Single bullet should still be offset", dist >= SPAWN_OFFSET - 1);
    }

    @Test
    public void shoot_bulletCountMatchesParameter() {
        for (int count : new int[]{1, 4, 8, 12, 16}) {
            CircleShootStrategy strategy = new CircleShootStrategy(count, 10, 5, false);
            List<BaseBullet> bullets = strategy.shoot(new StubAircraft(200, 200));
            assertEquals("bullet count should match for count=" + count, count, bullets.size());
        }
    }

    @Test
    public void shoot_speedVectorMatchesAngle() {
        CircleShootStrategy strategy = new CircleShootStrategy(4, 10, 5, true);
        List<BaseBullet> bullets = strategy.shoot(new StubAircraft(500, 500));

        assertEquals(4, bullets.size());
        BaseBullet right = bullets.get(0);
        assertTrue("angle=0 should have positive speedX", right.getSpeedX() > 0);
        assertEquals(0, right.getSpeedY());

        BaseBullet down = bullets.get(1);
        assertEquals(0, down.getSpeedX());
        assertTrue("angle=PI/2 should have positive speedY", down.getSpeedY() > 0);
    }
}
