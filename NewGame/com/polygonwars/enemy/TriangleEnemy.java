package com.polygonwars.enemy;

import com.polygonwars.entity.Character;
import com.polygonwars.entity.Bullet;
import com.polygonwars.util.MathUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

public class TriangleEnemy extends Enemy {

    private static final int SCORE_VALUE = 10;
    private static final Color DEFAULT_COLOR = new Color(255, 80, 80);

    // Explosion phase state (when spawned from HexagonEnemy)
    private double explodeTimeRemaining = 0.0;
    private double explodeDirX = 0.0;
    private double explodeDirY = 0.0;
    private double explodeSpeed = 0.0;

    public TriangleEnemy(double x,
            double y,
            double radius,
            double maxHealth,
            double bodyDamage,
            double movementSpeed) {
        super(x, y, radius, maxHealth, bodyDamage, movementSpeed);
    }

    @Override
    public int getScoreValue() {
        return SCORE_VALUE;
    }

    /**
     * Configure this triangle to start in an explosion phase, flying outward
     * for the given duration (seconds) in the given direction at the given speed.
     */
    public void startExplosionPhase(double dirX, double dirY, double durationSeconds, double speed) {
        double[] normalized = MathUtils.normalize(dirX, dirY);
        this.explodeDirX = normalized[0];
        this.explodeDirY = normalized[1];
        this.explodeTimeRemaining = Math.max(0, durationSeconds);
        this.explodeSpeed = speed;
    }

    @Override
    public void update(double deltaSeconds,
            Character player,
            java.util.List<Bullet> bullets,
            int mapWidth,
            int mapHeight) {
        if (explodeTimeRemaining > 0) {
            double moveX = explodeDirX * explodeSpeed;
            double moveY = explodeDirY * explodeSpeed;
            moveWithDirection(moveX, moveY, deltaSeconds, mapWidth, mapHeight);
            explodeTimeRemaining -= deltaSeconds;
        } else {
            moveTowards(player.getX(), player.getY(), deltaSeconds, mapWidth, mapHeight);
        }
    }

    @Override
    protected void drawBody(Graphics2D g2) {
        int r = (int) radius;
        int[] xPoints = { 0, -r, r };
        int[] yPoints = { -r, r, r };
        Polygon triangle = new Polygon(xPoints, yPoints, 3);

        g2.setColor(customColor != null ? customColor : DEFAULT_COLOR);
        g2.fillPolygon(triangle);

        g2.setColor(Color.DARK_GRAY);
        g2.drawPolygon(triangle);
    }
}
