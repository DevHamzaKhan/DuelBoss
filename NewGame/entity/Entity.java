package entity;

/*
Name: Entity.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Abstract base class for all game entities (player, bullets, enemies). Provides common properties like position, radius, health, and damage handling with circular collision detection.
*/

import java.awt.Graphics2D;

public abstract class Entity {

    // position and collision properties stored as doubles for smooth sub-pixel movement
    protected double x;
    protected double y;
    protected double radius;
    protected double maxHealth;
    protected double healthLeft;

    public Entity(double x, double y, double radius, double maxHealth) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.maxHealth = maxHealth;
        this.healthLeft = maxHealth;
    }

    public abstract void draw(Graphics2D g2);

    // reduces health by the specified amount, clamping to zero to prevent negative health
    public void takeDamage(double amount) {
        healthLeft -= amount;
        if (healthLeft < 0) {
            healthLeft = 0;
        }
    }

    // entity is considered alive as long as it has any health remaining
    public boolean isAlive() {
        return healthLeft > 0;
    }

    // clamps entity position to stay within map boundaries, preventing entities from escaping
    // uses radius to ensure entire circle stays within bounds
    protected void clampToMap(int mapWidth, int mapHeight) {
        double minX = radius;
        double maxX = mapWidth - radius;
        double minY = radius;
        double maxY = mapHeight - radius;

        if (x < minX) x = minX;
        if (x > maxX) x = maxX;
        if (y < minY) y = minY;
        if (y > maxY) y = maxY;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRadius() {
        return radius;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public double getHealthLeft() {
        return healthLeft;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
