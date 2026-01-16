package enemy;

/*
Name: Enemy.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Abstract base class for all enemy types.
*/

import entity.Entity;
import entity.Character;
import entity.Bullet;
import util.Utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.List;

public abstract class Enemy extends Entity {

    // health bar rendering constants
    private static final int HEALTH_BAR_WIDTH = 40;
    private static final int HEALTH_BAR_HEIGHT = 6;
    private static final int HEALTH_BAR_OFFSET_Y = 12; // distance above enemy

    protected double bodyDamage; // damage dealt on collision with player
    protected double movementSpeed; // pixels per second
    protected Color customColor = null; // overrides default color if set (used by spawned enemies)
    protected double angle = -Math.PI / 2; // current facing direction (default facing up)

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

    // returns the score value awarded when this enemy is killed
    // subclasses override to provide their specific value based on difficulty
    public abstract int getScoreValue();

    // gets the custom color for this enemy, or null if using default
    // used by spawned enemies to inherit spawner's color
    public Color getCustomColor() {
        return customColor;
    }

    // moves enemy towards target position, updating angle and position
    // automatically normalizes direction and clamps to map
    protected void moveTowards(double targetX,
            double targetY,
            double deltaSeconds,
            int mapWidth,
            int mapHeight) {
        double dx = targetX - x;
        double dy = targetY - y;
        if (dx != 0 || dy != 0) {
            angle = Math.atan2(dy, dx); // update facing direction
        }
        moveWithDirection(dx, dy, deltaSeconds, mapWidth, mapHeight);
    }

    // moves enemy in specified direction (not necessarily normalized)
    // handles normalization internally for consistent movement speed
    protected void moveWithDirection(double dx,
            double dy,
            double deltaSeconds,
            int mapWidth,
            int mapHeight) {
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len > 0) {
            dx /= len; // normalize direction
            dy /= len;

            x += dx * movementSpeed * deltaSeconds;
            y += dy * movementSpeed * deltaSeconds;
        }

        clampToMap(mapWidth, mapHeight); // ensure enemy stays in bounds
    }

    // updates facing direction without moving (used by enemies that strafe or
    // shoot)
    protected void faceTowards(double targetX, double targetY) {
        double dx = targetX - x;
        double dy = targetY - y;
        if (dx != 0 || dy != 0) {
            angle = Math.atan2(dy, dx);
        }
    }

    // checks circular collision with player using distance-squared for performance
    public boolean collidesWith(Character player) {
        double dx = x - player.getX();
        double dy = y - player.getY();
        double distanceSq = dx * dx + dy * dy;
        double combinedRadius = radius + player.getRadius();
        return distanceSq <= combinedRadius * combinedRadius; // avoids expensive sqrt
    }

    // called when enemy collides with player - default behavior is to deal body
    // damage
    public void onCollideWithPlayer(Character player) {
        player.takeDamage(bodyDamage);
    }

    // renders enemy with rotation, delegating body rendering to subclass
    // health bar is rendered in screen space (unrotated)
    public void draw(Graphics2D g2) {
        AffineTransform old = g2.getTransform();
        g2.translate(x, y);
        g2.rotate(angle + Math.PI / 2); // +pi/2 so "up" is default orientation

        drawBody(g2); // subclass defines specific shape

        g2.setTransform(old); // restore transform before drawing health bar

        // health bar above enemy
        drawHealthBar(g2);
    }

    // subclasses implement this to draw their specific shape
    // called with transform already applied (rotated to enemy's angle)
    protected abstract void drawBody(Graphics2D g2);

    // renders health bar above enemy with percentage-based fill
    protected void drawHealthBar(Graphics2D g2) {
        double hpPercent = Utils.clamp(healthLeft / maxHealth, 0.0, 1.0);

        int xLeft = (int) (x - HEALTH_BAR_WIDTH / 2.0);
        int yTop = (int) (y - radius - HEALTH_BAR_OFFSET_Y);

        // background/border
        g2.setColor(new Color(40, 40, 40, 220));
        g2.fillRect(xLeft - 1, yTop - 1, HEALTH_BAR_WIDTH + 2, HEALTH_BAR_HEIGHT + 2);

        // empty health (red background)
        g2.setColor(new Color(90, 0, 0));
        g2.fillRect(xLeft, yTop, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);

        // filled health (green)
        int filledWidth = (int) (HEALTH_BAR_WIDTH * hpPercent);
        g2.setColor(new Color(0, 220, 0));
        g2.fillRect(xLeft, yTop, filledWidth, HEALTH_BAR_HEIGHT);
    }
}
