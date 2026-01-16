package entity;

import util.MathUtils;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class Character extends Entity {

    private static final int CHARACTER_RADIUS = 20;
    private static final String SPRITE_PATH = "/Images/player.png";
    private static final String SPRITE_PATH_FALLBACK = "Images/player.png";

    // Base stats for scaling
    private static final double BASE_MAX_HEALTH = 200;
    private static final double BASE_BULLET_SPEED = 600;
    private static final double BASE_FIRE_RATE = 1;
    private static final double BASE_MOVEMENT_SPEED = 340;
    private static final double BASE_BULLET_DAMAGE = 20;

    // Upgrade multipliers per level
    private static final double HEALTH_MULTIPLIER_PER_LEVEL = 0.2;
    private static final double BULLET_SPEED_MULTIPLIER_PER_LEVEL = 0.15;
    private static final double FIRE_RATE_MULTIPLIER_PER_LEVEL = 0.2;
    private static final double MOVEMENT_SPEED_MULTIPLIER_PER_LEVEL = 0.15;
    private static final double BULLET_DAMAGE_MULTIPLIER_PER_LEVEL = 0.15;

    private static final int MAX_UPGRADE_LEVEL = 10;
    private static final double HEALTH_BUY_AMOUNT = 20;
    private static final double BODY_DAMAGE = 10;

    private BufferedImage sprite;
    private double angle = -Math.PI / 2;

    // Current stats (calculated from upgrades)
    private double bulletSpeed;
    private double bulletDamage;
    private double fireRate;
    private double movementSpeed;

    // Upgrade levels (1-10 scale)
    private int maxHealthLevel = 1;
    private int bulletSpeedLevel = 1;
    private int fireRateLevel = 1;
    private int movementSpeedLevel = 1;
    private int bulletDamageLevel = 1;

    public Character(double startX, double startY) {
        super(startX, startY, CHARACTER_RADIUS, BASE_MAX_HEALTH);
        applyUpgrades();
        healthLeft = maxHealth;
        loadSprite();
    }

    private void loadSprite() {
        // Try loading from classpath first (works when packaged as JAR)
        InputStream stream = getClass().getResourceAsStream(SPRITE_PATH);
        if (stream != null) {
            try {
                sprite = ImageIO.read(stream);
                return;
            } catch (IOException e) {
                System.err.println("Failed to load sprite from classpath: " + e.getMessage());
            }
        }

        // Fallback to file system (works during development)
        try {
            sprite = ImageIO.read(new java.io.File(SPRITE_PATH_FALLBACK));
        } catch (IOException e) {
            System.err.println("Warning: Could not load player sprite from " + SPRITE_PATH_FALLBACK);
            System.err.println("Player will not be visible. Ensure the image file exists.");
        }
    }

    public void setAngleToward(double targetX, double targetY) {
        double dx = targetX - x;
        double dy = targetY - y;
        angle = Math.atan2(dy, dx);
    }

    private void applyUpgrades() {
        maxHealth = BASE_MAX_HEALTH * (1 + (maxHealthLevel - 1) * HEALTH_MULTIPLIER_PER_LEVEL);
        bulletSpeed = BASE_BULLET_SPEED * (1 + (bulletSpeedLevel - 1) * BULLET_SPEED_MULTIPLIER_PER_LEVEL);
        fireRate = BASE_FIRE_RATE * (1 + (fireRateLevel - 1) * FIRE_RATE_MULTIPLIER_PER_LEVEL);
        movementSpeed = BASE_MOVEMENT_SPEED * (1 + (movementSpeedLevel - 1) * MOVEMENT_SPEED_MULTIPLIER_PER_LEVEL);
        bulletDamage = BASE_BULLET_DAMAGE * (1 + (bulletDamageLevel - 1) * BULLET_DAMAGE_MULTIPLIER_PER_LEVEL);
    }

    public void upgradeMaxHealth() {
        if (maxHealthLevel < MAX_UPGRADE_LEVEL) {
            maxHealthLevel++;
            double currentPercent = healthLeft / maxHealth;
            applyUpgrades();
            healthLeft = maxHealth * currentPercent;
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
        healthLeft = Math.min(maxHealth, healthLeft + HEALTH_BUY_AMOUNT);
    }

    public void update(double dirX, double dirY, double deltaSeconds, int mapWidth, int mapHeight) {
        double[] normalized = MathUtils.normalize(dirX, dirY);
        x += normalized[0] * movementSpeed * deltaSeconds;
        y += normalized[1] * movementSpeed * deltaSeconds;
        clampToMap(mapWidth, mapHeight);
    }

    @Override
    public void draw(Graphics2D g2) {
        if (sprite == null) {
            return;
        }

        AffineTransform oldTransform = g2.getTransform();
        g2.translate(x, y);
        g2.rotate(angle + Math.PI / 2);

        int width = sprite.getWidth();
        int height = sprite.getHeight();
        g2.drawImage(sprite, -width / 2, -height / 2, null);

        g2.setTransform(oldTransform);
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
