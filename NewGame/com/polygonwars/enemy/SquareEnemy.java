package com.polygonwars.enemy;

import com.polygonwars.entity.Character;
import com.polygonwars.entity.Bullet;
import com.polygonwars.util.MathUtils;

import java.awt.Color;
import java.awt.Graphics2D;

public class SquareEnemy extends Enemy {

    private static final int SCORE_VALUE = 20;
    private static final Color DEFAULT_COLOR = new Color(255, 150, 80);

    private final double dodgeRadius;

    public SquareEnemy(double x,
            double y,
            double halfSize,
            double maxHealth,
            double bodyDamage,
            double movementSpeed,
            double dodgeRadius) {
        super(x, y, halfSize, maxHealth, bodyDamage, movementSpeed);
        this.dodgeRadius = dodgeRadius;
    }

    @Override
    public int getScoreValue() {
        return SCORE_VALUE;
    }

    @Override
    public void update(double deltaSeconds,
            Character player,
            java.util.List<Bullet> bullets,
            int mapWidth,
            int mapHeight) {

        Bullet closestBullet = findClosestBulletInRange(bullets);
        faceTowards(player.getX(), player.getY());

        if (closestBullet != null) {
            dodgeBullet(closestBullet, deltaSeconds, mapWidth, mapHeight);
        } else {
            moveTowards(player.getX(), player.getY(), deltaSeconds, mapWidth, mapHeight);
        }
    }

    private Bullet findClosestBulletInRange(java.util.List<Bullet> bullets) {
        Bullet closest = null;
        double closestDistSq = Double.MAX_VALUE;
        double dodgeRadiusSq = dodgeRadius * dodgeRadius;

        for (Bullet bullet : bullets) {
            double distSq = MathUtils.distanceSquared(x, y, bullet.getX(), bullet.getY());
            if (distSq <= dodgeRadiusSq && distSq < closestDistSq) {
                closestDistSq = distSq;
                closest = bullet;
            }
        }
        return closest;
    }

    private void dodgeBullet(Bullet bullet, double deltaSeconds, int mapWidth, int mapHeight) {
        double[] normalized = MathUtils.normalize(bullet.getVx(), bullet.getVy());
        if (normalized[0] != 0 || normalized[1] != 0) {
            // Move perpendicular to bullet trajectory
            double dodgeX = -normalized[1];
            double dodgeY = normalized[0];
            moveWithDirection(dodgeX, dodgeY, deltaSeconds, mapWidth, mapHeight);
        }
    }

    public double getDodgeRadius() {
        return dodgeRadius;
    }

    @Override
    protected void drawBody(Graphics2D g2) {
        int half = (int) radius;
        int size = half * 2;

        g2.setColor(customColor != null ? customColor : DEFAULT_COLOR);
        g2.fillRect(-half, -half, size, size);

        g2.setColor(Color.BLACK);
        g2.drawRect(-half, -half, size, size);
    }
}
