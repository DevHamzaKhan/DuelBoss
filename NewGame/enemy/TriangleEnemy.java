package enemy;

/*
Name: TriangleEnemy.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Basic chase enemy with explosion phase mechanic. Can be spawned from hexagon splits with initial outward velocity before transitioning to normal chase behavior. Simplest enemy type for early waves.
*/

import entity.Character;
import entity.Bullet;
import util.MathUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

public class TriangleEnemy extends Enemy {

    private static final int SCORE_VALUE = 10;
    private static final Color DEFAULT_COLOR = new Color(255, 80, 80);

    // explosion phase state (when spawned from hexagonenemy death)
    // triangles initially fly outward before chasing player
    private double explodeTimeRemaining = 0.0; // time left in explosion phase
    private double explodeDirX = 0.0; // normalized direction x
    private double explodeDirY = 0.0; // normalized direction y
    private double explodeSpeed = 0.0; // explosion velocity

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
        // if in explosion phase, move outward without chasing player
        if (explodeTimeRemaining > 0) {
            double moveX = explodeDirX * explodeSpeed;
            double moveY = explodeDirY * explodeSpeed;
            moveWithDirection(moveX, moveY, deltaSeconds, mapWidth, mapHeight);
            explodeTimeRemaining -= deltaSeconds;
        } else {
            // normal behavior: chase player
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
