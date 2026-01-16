package entity;

/*
Name: Bullet.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Bullet fired by player and enemies
*/

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class Bullet extends Entity {

    private static final int BULLET_RADIUS = 6;
    private static final float STROKE_WIDTH = 3f;
    private static final double TRAIL_LENGTH = 0.015; // trail extends backward from bullet position
    private static final Color PLAYER_BULLET_COLOR = new Color(100, 200, 255);
    private static final Color ENEMY_BULLET_COLOR = new Color(255, 80, 80);

    private final double speed;
    private final double damage;
    private final double vx; // velocity x component
    private final double vy; // velocity y component
    private final boolean fromPlayer; // tracks bullet origin for collision detection

    // velocity (vx, vy) is pre-calculated on creation for performance
    // avoids repeated angle/speed calculations during movement
    public Bullet(double x, double y, double vx, double vy, double speed, double damage, boolean fromPlayer) {
        super(x, y, BULLET_RADIUS, 1); // bullets have 1 hp (destroyed on first hit)
        this.vx = vx;
        this.vy = vy;
        this.speed = speed;
        this.damage = damage;
        this.fromPlayer = fromPlayer;
    }

    // simple position update using pre-calculated velocity
    public void update(double deltaSeconds) {
        x += vx * deltaSeconds;
        y += vy * deltaSeconds;
    }

    // renders bullet as a line with motion trail for visual feedback
    // trail extends opposite to velocity direction
    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(fromPlayer ? PLAYER_BULLET_COLOR : ENEMY_BULLET_COLOR);
        g2.setStroke(new BasicStroke(STROKE_WIDTH));

        // calculate trail start position based on velocity direction
        int trailX = (int) (x - vx * TRAIL_LENGTH);
        int trailY = (int) (y - vy * TRAIL_LENGTH);
        g2.drawLine(trailX, trailY, (int) x, (int) y);
    }

    // checks if bullet has left the visible map area (with radius buffer)
    public boolean isOutOfBounds(int minX, int minY, int maxX, int maxY) {
        return x < minX - radius || x > maxX + radius ||
                y < minY - radius || y > maxY + radius;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public boolean isFromPlayer() {
        return fromPlayer;
    }

    public double getSpeed() {
        return speed;
    }

    public double getDamage() {
        return damage;
    }
}
