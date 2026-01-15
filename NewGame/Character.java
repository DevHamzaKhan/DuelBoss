import java.awt.Color;
import java.awt.Graphics2D;

public class Character {

    // Core stats
    private double maxHealth = 100;
    private double healthLeft = 100;
    private double bodyDamage = 10;

    private double bulletSpeed = 600; // units per second
    private double bulletDamage = 20;
    private double fireRate = 1; // bullets per second
    private double movementSpeed = 250; // units per second

    // Position
    private double x;
    private double y;

    private final int radius = 20;

    public Character(double startX, double startY) {
        this.x = startX;
        this.y = startY;
    }

    public void update(double dirX, double dirY, double deltaSeconds, int mapWidth, int mapHeight) {
        double len = Math.sqrt(dirX * dirX + dirY * dirY);
        if (len > 0) {
            dirX /= len;
            dirY /= len;
        }

        x += dirX * movementSpeed * deltaSeconds;
        y += dirY * movementSpeed * deltaSeconds;

        // Clamp to map borders (keep full character inside)
        double minX = radius;
        double maxX = mapWidth - radius;
        double minY = radius;
        double maxY = mapHeight - radius;

        if (x < minX) x = minX;
        if (x > maxX) x = maxX;
        if (y < minY) y = minY;
        if (y > maxY) y = maxY;
    }

    public void draw(Graphics2D g2) {
        int drawX = (int) (x - radius);
        int drawY = (int) (y - radius);

        g2.setColor(new Color(50, 200, 255));
        g2.fillOval(drawX, drawY, radius * 2, radius * 2);

        g2.setColor(Color.WHITE);
        g2.drawOval(drawX, drawY, radius * 2, radius * 2);
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
}


