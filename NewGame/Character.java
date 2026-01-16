import java.awt.Color;
import java.awt.Graphics2D;

public class Character {

    // Core stats
    private double maxHealth = 100;
    private double healthLeft = 100;
    private double bodyDamage = 10;

    private double bulletSpeed = 1200; // units per second (doubled)
    private double bulletDamage = 20;
    private double fireRate = 3; // bullets per second (tripled)
    private double movementSpeed = 500; // units per second (doubled)
    
    // Upgrade levels (1-10 scale)
    private int maxHealthLevel = 1;
    private int bulletSpeedLevel = 1;
    private int fireRateLevel = 1;
    private int movementSpeedLevel = 1;
    private int bulletDamageLevel = 1;
    
    // Base stats for scaling
    private static final double BASE_MAX_HEALTH = 200; // Double HP: 100 -> 200
    private static final double BASE_BULLET_SPEED = 600;
    private static final double BASE_FIRE_RATE = 1;
    private static final double BASE_MOVEMENT_SPEED = 340; // Faster than square enemy (320): 250 -> 340
    private static final double BASE_BULLET_DAMAGE = 20;

    // Position
    private double x;
    private double y;

    private final int radius = 20;

    public Character(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        applyUpgrades();
        // Start at full health
        healthLeft = maxHealth;
    }
    
    private void applyUpgrades() {
        // Calculate stats based on levels (1-10 scale, linear scaling)
        maxHealth = BASE_MAX_HEALTH * (1 + (maxHealthLevel - 1) * 0.2); // 20% per level
        bulletSpeed = BASE_BULLET_SPEED * (1 + (bulletSpeedLevel - 1) * 0.15); // 15% per level
        fireRate = BASE_FIRE_RATE * (1 + (fireRateLevel - 1) * 0.2); // 20% per level
        movementSpeed = BASE_MOVEMENT_SPEED * (1 + (movementSpeedLevel - 1) * 0.15); // 15% per level
        bulletDamage = BASE_BULLET_DAMAGE * (1 + (bulletDamageLevel - 1) * 0.15); // 15% per level
    }
    
    public void upgradeMaxHealth() {
        if (maxHealthLevel < 10) {
            maxHealthLevel++;
            double currentPercent = healthLeft / maxHealth;
            applyUpgrades();
            healthLeft = maxHealth * currentPercent; // Keep same % health
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
        // Always heals +20 HP
        healthLeft = Math.min(maxHealth, healthLeft + 20);
    }
    
    // Getters for upgrade levels
    public int getMaxHealthLevel() { return maxHealthLevel; }
    public int getBulletSpeedLevel() { return bulletSpeedLevel; }
    public int getFireRateLevel() { return fireRateLevel; }
    public int getMovementSpeedLevel() { return movementSpeedLevel; }
    public int getBulletDamageLevel() { return bulletDamageLevel; }
    // Health regen is not a level, just +20 HP per purchase

    public void update(double dirX, double dirY, double deltaSeconds, int mapWidth, int mapHeight) {
        double len = Math.sqrt(dirX * dirX + dirY * dirY);
        if (len > 0) {
            dirX /= len;
            dirY /= len;
        }

        // Calculate desired position
        double newX = x + dirX * movementSpeed * deltaSeconds;
        double newY = y + dirY * movementSpeed * deltaSeconds;

        // Clamp to map borders BEFORE moving (keep full character inside, accounting for radius)
        double minX = radius;
        double maxX = mapWidth - radius;
        double minY = radius;
        double maxY = mapHeight - radius;

        // Clamp the new position
        if (newX < minX) newX = minX;
        if (newX > maxX) newX = maxX;
        if (newY < minY) newY = minY;
        if (newY > maxY) newY = maxY;

        // Apply clamped position
        x = newX;
        y = newY;
    }

    public void draw(Graphics2D g2) {
        int drawX = (int) (x - radius);
        int drawY = (int) (y - radius);

        g2.setColor(new Color(50, 200, 255));
        g2.fillOval(drawX, drawY, radius * 2, radius * 2);

        g2.setColor(Color.WHITE);
        g2.drawOval(drawX, drawY, radius * 2, radius * 2);
    }

    public void takeDamage(double amount) {
        healthLeft -= amount;
        if (healthLeft < 0) {
            healthLeft = 0;
        }
    }

    // Getters for position and stats
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public double getHealthLeft() {
        return healthLeft;
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

    public int getRadius() {
        return radius;
    }
}


