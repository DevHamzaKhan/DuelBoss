import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Character extends Entity {

    private static final int CHARACTER_RADIUS = 20;

    private BufferedImage sprite;
    private double angle = -Math.PI / 2; // Default facing up

    // Core stats
    private double bodyDamage = 10;
    private double bulletSpeed = 1200;
    private double bulletDamage = 20;
    private double fireRate = 3;
    private double movementSpeed = 500;

    // Upgrade levels (1-10 scale)
    private int maxHealthLevel = 1;
    private int bulletSpeedLevel = 1;
    private int fireRateLevel = 1;
    private int movementSpeedLevel = 1;
    private int bulletDamageLevel = 1;

    // Base stats for scaling
    private static final double BASE_MAX_HEALTH = 200;
    private static final double BASE_BULLET_SPEED = 600;
    private static final double BASE_FIRE_RATE = 1;
    private static final double BASE_MOVEMENT_SPEED = 340;
    private static final double BASE_BULLET_DAMAGE = 20;

    public Character(double startX, double startY) {
        super(startX, startY, CHARACTER_RADIUS, BASE_MAX_HEALTH);
        applyUpgrades();
        healthLeft = maxHealth;
        loadSprite();
    }

    private void loadSprite() {
        try {
            sprite = ImageIO.read(new File("Images/player.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAngleToward(double targetX, double targetY) {
        double dx = targetX - x;
        double dy = targetY - y;
        angle = Math.atan2(dy, dx);
    }

    private void applyUpgrades() {
        maxHealth = BASE_MAX_HEALTH * (1 + (maxHealthLevel - 1) * 0.2);
        bulletSpeed = BASE_BULLET_SPEED * (1 + (bulletSpeedLevel - 1) * 0.15);
        fireRate = BASE_FIRE_RATE * (1 + (fireRateLevel - 1) * 0.2);
        movementSpeed = BASE_MOVEMENT_SPEED * (1 + (movementSpeedLevel - 1) * 0.15);
        bulletDamage = BASE_BULLET_DAMAGE * (1 + (bulletDamageLevel - 1) * 0.15);
    }

    public void upgradeMaxHealth() {
        if (maxHealthLevel < 10) {
            maxHealthLevel++;
            double currentPercent = healthLeft / maxHealth;
            applyUpgrades();
            healthLeft = maxHealth * currentPercent;
        }
    }

    public void upgradeBulletSpeed() {
        if (bulletSpeedLevel < 10) {
            bulletSpeedLevel++;
            applyUpgrades();
        }
    }

    public void upgradeFireRate() {
        if (fireRateLevel < 10) {
            fireRateLevel++;
            applyUpgrades();
        }
    }

    public void upgradeMovementSpeed() {
        if (movementSpeedLevel < 10) {
            movementSpeedLevel++;
            applyUpgrades();
        }
    }

    public void upgradeBulletDamage() {
        if (bulletDamageLevel < 10) {
            bulletDamageLevel++;
            applyUpgrades();
        }
    }

    public void buyHealth() {
        healthLeft = Math.min(maxHealth, healthLeft + 20);
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

    public void update(double dirX, double dirY, double deltaSeconds, int mapWidth, int mapHeight) {
        double len = Math.sqrt(dirX * dirX + dirY * dirY);
        if (len > 0) {
            dirX /= len;
            dirY /= len;
        }

        x += dirX * movementSpeed * deltaSeconds;
        y += dirY * movementSpeed * deltaSeconds;

        clampToMap(mapWidth, mapHeight);
    }

    @Override
    public void draw(Graphics2D g2) {
        if (sprite == null)
            return;

        AffineTransform old = g2.getTransform();

        // Move to player position, rotate (add PI/2 because sprite tip faces up)
        g2.translate(x, y);
        g2.rotate(angle + Math.PI / 2);

        // Draw sprite centered
        int w = sprite.getWidth();
        int h = sprite.getHeight();
        g2.drawImage(sprite, -w / 2, -h / 2, null);

        g2.setTransform(old);
    }

    public double getBodyDamage() {
        return bodyDamage;
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
