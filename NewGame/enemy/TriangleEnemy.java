/*
Name: TriangleEnemy.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Triangle enemy that chases player.
*/

package enemy;

import entity.Character;
import entity.Bullet;
import util.Utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

public class TriangleEnemy extends Enemy {

    private static final int SCORE_VALUE = 10;
    public static final Color DEFAULT_COLOR = new Color(255, 80, 80);

    // explosion phase: triangles fly outward briefly when spawned from hexagon
    // death
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

    // starts explosion phase where triangle flies outward before chasing player
    public void startExplosionPhase(double dirX, double dirY, double durationSeconds, double speed) {
        double[] normalized = Utils.normalize(dirX, dirY);
        this.explodeDirX = normalized[0];
        this.explodeDirY = normalized[1];
        this.explodeTimeRemaining = Math.max(0, durationSeconds);
        this.explodeSpeed = speed;
    }

    // moves toward player unless in explosion phase
    @Override
    public void update(double deltaSeconds,
            Character player,
            List<Bullet> bullets,
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

    // draws triangle shape pointing in movement direction
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
