package entity;

/*
Name: Character.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Player character class with upgrade system and sprite rendering. Features scalable stats based on upgrade levels, image loading from classpath/filesystem, and rotation-based movement and aiming.
*/

import util.MathUtils;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class Character extends Entity {

    private static final int CHARACTER_RADIUS = 20;
    private static final String SPRITE_PATH = "/Images/player.png";
    private static final String SPRITE_PATH_FALLBACK = "Images/player.png";

    // base stats for scaling - all upgrades are multiplicative on these base values
    // this design ensures consistent scaling: level 1 = base, level 10 = base * (1 + 9 * multiplier)
    private static final double BASE_MAX_HEALTH = 200;
    private static final double BASE_BULLET_SPEED = 600;
    private static final double BASE_FIRE_RATE = 1;
    private static final double BASE_MOVEMENT_SPEED = 340;
    private static final double BASE_BULLET_DAMAGE = 20;

    // upgrade multipliers per level - each level adds this percentage to the base stat
    // example: health level 5 = 200 * (1 + (5-1) * 0.2) = 200 * 1.8 = 360 hp
    private static final double HEALTH_MULTIPLIER_PER_LEVEL = 0.2;
    private static final double BULLET_SPEED_MULTIPLIER_PER_LEVEL = 0.15;
    private static final double FIRE_RATE_MULTIPLIER_PER_LEVEL = 0.2;
    private static final double MOVEMENT_SPEED_MULTIPLIER_PER_LEVEL = 0.15;
    private static final double BULLET_DAMAGE_MULTIPLIER_PER_LEVEL = 0.15;

    private static final int MAX_UPGRADE_LEVEL = 10;
    private static final double HEALTH_BUY_AMOUNT = 20;
    private static final double BODY_DAMAGE = 10;

    private BufferedImage sprite;
    private double angle = -Math.PI / 2; // default facing up (-90 degrees)

    // current stats (calculated from upgrades) - recalculated whenever upgrades change
    private double bulletSpeed;
    private double bulletDamage;
    private double fireRate;
    private double movementSpeed;

    // upgrade levels (1-10 scale) - starts at 1 (base stats), can go up to 10
    private int maxHealthLevel = 1;
    private int bulletSpeedLevel = 1;
    private int fireRateLevel = 1;
    private int movementSpeedLevel = 1;
    private int bulletDamageLevel = 1;

    public Character(double startX, double startY) {
        super(startX, startY, CHARACTER_RADIUS, BASE_MAX_HEALTH);
        applyUpgrades(); // calculate initial stats based on level 1 upgrades
        healthLeft = maxHealth; // start with full health
        loadSprite();
    }

    // attempts to load sprite from multiple sources for maximum compatibility
    // tries classpath first (works in JAR), falls back to filesystem (works in IDE)
    private void loadSprite() {
        // try loading from classpath first (works when packaged as jar)
        InputStream stream = getClass().getResourceAsStream(SPRITE_PATH);
        if (stream != null) {
            try {
                sprite = ImageIO.read(stream);
                return;
            } catch (IOException e) {
                System.err.println("Failed to load sprite from classpath: " + e.getMessage());
            }
        }

        // fallback to file system (works during development)
        try {
            sprite = ImageIO.read(new File(SPRITE_PATH_FALLBACK));
        } catch (IOException e) {
            System.err.println("Warning: Could not load player sprite from " + SPRITE_PATH_FALLBACK);
            System.err.println("Player will not be visible. Ensure the image file exists.");
        }
    }

    // calculates angle to target using atan2 for proper quadrant handling
    public void setAngleToward(double targetX, double targetY) {
        double dx = targetX - x;
        double dy = targetY - y;
        angle = Math.atan2(dy, dx); // atan2 handles all four quadrants correctly
    }

    // recalculates all stats based on current upgrade levels using multiplicative scaling
    // called after every upgrade to update character's active stats
    private void applyUpgrades() {
        maxHealth = BASE_MAX_HEALTH * (1 + (maxHealthLevel - 1) * HEALTH_MULTIPLIER_PER_LEVEL);
        bulletSpeed = BASE_BULLET_SPEED * (1 + (bulletSpeedLevel - 1) * BULLET_SPEED_MULTIPLIER_PER_LEVEL);
        fireRate = BASE_FIRE_RATE * (1 + (fireRateLevel - 1) * FIRE_RATE_MULTIPLIER_PER_LEVEL);
        movementSpeed = BASE_MOVEMENT_SPEED * (1 + (movementSpeedLevel - 1) * MOVEMENT_SPEED_MULTIPLIER_PER_LEVEL);
        bulletDamage = BASE_BULLET_DAMAGE * (1 + (bulletDamageLevel - 1) * BULLET_DAMAGE_MULTIPLIER_PER_LEVEL);
    }

    // increases max health level and maintains current health percentage
    // preserves health percentage so player doesn't lose progress on upgrade
    public void upgradeMaxHealth() {
        if (maxHealthLevel < MAX_UPGRADE_LEVEL) {
            maxHealthLevel++;
            double currentPercent = healthLeft / maxHealth; // save current health percentage
            applyUpgrades(); // recalculate max health
            healthLeft = maxHealth * currentPercent; // restore percentage with new max
        }
    }

    public void upgradeBulletSpeed() {
        if (bulletSpeedLevel < MAX_UPGRADE_LEVEL) {
            bulletSpeedLevel++;
            applyUpgrades();
        }
    }

    public void upgradeFireRate() {
        if (fireRateLevel < MAX_UPGRADE_LEVEL) {
            fireRateLevel++;
            applyUpgrades();
        }
    }

    public void upgradeMovementSpeed() {
        if (movementSpeedLevel < MAX_UPGRADE_LEVEL) {
            movementSpeedLevel++;
            applyUpgrades();
        }
    }

    public void upgradeBulletDamage() {
        if (bulletDamageLevel < MAX_UPGRADE_LEVEL) {
            bulletDamageLevel++;
            applyUpgrades();
        }
    }

    public void buyHealth() {
        healthLeft = Math.min(maxHealth, healthLeft + HEALTH_BUY_AMOUNT); // caps at max health
    }

    // updates player position based on directional input, normalizing diagonal movement
    // uses MathUtils.normalize to ensure diagonal movement isn't faster than cardinal
    public void update(double dirX, double dirY, double deltaSeconds, int mapWidth, int mapHeight) {
        double[] normalized = MathUtils.normalize(dirX, dirY);
        x += normalized[0] * movementSpeed * deltaSeconds;
        y += normalized[1] * movementSpeed * deltaSeconds;
        clampToMap(mapWidth, mapHeight); // prevent moving outside map
    }

    // renders sprite with rotation using affinetransform
    // saves/restores transform to avoid affecting other rendering
    @Override
    public void draw(Graphics2D g2) {
        if (sprite == null) {
            return; // skip rendering if sprite failed to load
        }

        AffineTransform oldTransform = g2.getTransform();
        g2.translate(x, y); // move to entity position
        g2.rotate(angle + Math.PI / 2); // +pi/2 adjusts sprite orientation (sprite faces up by default)

        int width = sprite.getWidth();
        int height = sprite.getHeight();
        g2.drawImage(sprite, -width / 2, -height / 2, null); // center sprite on position

        g2.setTransform(oldTransform); // restore original transform
    }

    // Getters for upgrade levels
    public int getMaxHealthLevel() {
        return maxHealthLevel;
    }

    public int getBulletSpeedLevel() {
        return bulletSpeedLevel;
    }

    public int getFireRateLevel() {
        return fireRateLevel;
    }

    public int getMovementSpeedLevel() {
        return movementSpeedLevel;
    }

    public int getBulletDamageLevel() {
        return bulletDamageLevel;
    }

    // Getters for stats
    public double getBodyDamage() {
        return BODY_DAMAGE;
    }

    public double getBulletSpeed() {
        return bulletSpeed;
    }

    public double getBulletDamage() {
        return bulletDamage;
    }

    public double getFireRate() {
        return fireRate;
    }

    public double getMovementSpeed() {
        return movementSpeed;
    }
}
