import java.awt.Color;
import java.awt.Graphics2D;

public class Bullet {

    // Core stats
    private double maxHealth = 1; // For future use
    private double healthLeft = 1;
    private double bodyDamage = 0;

    private final double speed;
    private final double damage;

    // Position and velocity
    private double x;
    private double y;
    private double vx;
    private double vy;

    private final int radius = 6;

    public Bullet(double x, double y, double vx, double vy, double speed, double damage) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.speed = speed;
        this.damage = damage;
    }

    public void update(double deltaSeconds) {
        x += vx * deltaSeconds;
        y += vy * deltaSeconds;
    }

    public void draw(Graphics2D g2) {
        int drawX = (int) (x - radius);
        int drawY = (int) (y - radius);

        g2.setColor(new Color(255, 210, 70));
        g2.fillOval(drawX, drawY, radius * 2, radius * 2);

        g2.setColor(Color.ORANGE);
        g2.drawOval(drawX, drawY, radius * 2, radius * 2);
    }

    public boolean isOutOfBounds(int minX, int minY, int maxX, int maxY) {
        return x < minX - radius || x > maxX + radius || y < minY - radius || y > maxY + radius;
    }

    // Getters for stats (for future use)
    public double getMaxHealth() {
        return maxHealth;
    }

    public double getHealthLeft() {
        return healthLeft;
    }

    public double getBodyDamage() {
        return bodyDamage;
    }

    public double getSpeed() {
        return speed;
    }

    public double getDamage() {
        return damage;
    }
}


