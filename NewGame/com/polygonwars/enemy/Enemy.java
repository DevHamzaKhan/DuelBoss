package com.polygonwars.enemy;

import com.polygonwars.entity.Entity;
import com.polygonwars.entity.Character;
import com.polygonwars.entity.Bullet;
import com.polygonwars.util.MathUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

public abstract class Enemy extends Entity {

    // Health bar rendering constants
    private static final int HEALTH_BAR_WIDTH = 40;
    private static final int HEALTH_BAR_HEIGHT = 6;
    private static final int HEALTH_BAR_OFFSET_Y = 12;

    protected double bodyDamage;
    protected double movementSpeed;
    protected Color customColor = null;
    protected double angle = -Math.PI / 2; // Default facing up

    public Enemy(double x,
            double y,
            double radius,
            double maxHealth,
            double bodyDamage,
            double movementSpeed) {
        super(x, y, radius, maxHealth);
        this.bodyDamage = bodyDamage;
        this.movementSpeed = movementSpeed;
    }

    public abstract void update(double deltaSeconds,
            Character player,
            List<Bullet> bullets,
            int mapWidth,
            int mapHeight);

    /**
     * Returns the score value awarded when this enemy is killed.
     * Subclasses override to provide their specific value.
     */
    public abstract int getScoreValue();
    
    /**
     * Gets the custom color for this enemy, or null if using default.
     */
    public Color getCustomColor() {
        return customColor;
    }

    protected void moveTowards(double targetX,
            double targetY,
            double deltaSeconds,
            int mapWidth,
            int mapHeight) {
        double dx = targetX - x;
        double dy = targetY - y;
        if (dx != 0 || dy != 0) {
            angle = Math.atan2(dy, dx);
        }
        moveWithDirection(dx, dy, deltaSeconds, mapWidth, mapHeight);
    }

    protected void moveWithDirection(double dx,
            double dy,
            double deltaSeconds,
            int mapWidth,
            int mapHeight) {
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len > 0) {
            dx /= len;
            dy /= len;

            x += dx * movementSpeed * deltaSeconds;
            y += dy * movementSpeed * deltaSeconds;
        }

        clampToMap(mapWidth, mapHeight);
    }

    protected void faceTowards(double targetX, double targetY) {
        double dx = targetX - x;
        double dy = targetY - y;
        if (dx != 0 || dy != 0) {
            angle = Math.atan2(dy, dx);
        }
    }

    public boolean collidesWith(Character player) {
        double dx = x - player.getX();
        double dy = y - player.getY();
        double distanceSq = dx * dx + dy * dy;
        double combinedRadius = radius + player.getRadius();
        return distanceSq <= combinedRadius * combinedRadius;
    }

    public void onCollideWithPlayer(Character player) {
        player.takeDamage(bodyDamage);
    }

    public void draw(Graphics2D g2) {
        java.awt.geom.AffineTransform old = g2.getTransform();
        g2.translate(x, y);
        g2.rotate(angle + Math.PI / 2); // +PI/2 so "up" is default orientation

        drawBody(g2);

        g2.setTransform(old);

        // Health bar above enemy
        drawHealthBar(g2);
    }

    protected abstract void drawBody(Graphics2D g2);

    protected void drawHealthBar(Graphics2D g2) {
        double hpPercent = MathUtils.clamp(healthLeft / maxHealth, 0.0, 1.0);

        int xLeft = (int) (x - HEALTH_BAR_WIDTH / 2.0);
        int yTop = (int) (y - radius - HEALTH_BAR_OFFSET_Y);

        g2.setColor(new Color(40, 40, 40, 220));
        g2.fillRect(xLeft - 1, yTop - 1, HEALTH_BAR_WIDTH + 2, HEALTH_BAR_HEIGHT + 2);

        g2.setColor(new Color(90, 0, 0));
        g2.fillRect(xLeft, yTop, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);

        int filledWidth = (int) (HEALTH_BAR_WIDTH * hpPercent);
        g2.setColor(new Color(0, 220, 0));
        g2.fillRect(xLeft, yTop, filledWidth, HEALTH_BAR_HEIGHT);
    }
}
