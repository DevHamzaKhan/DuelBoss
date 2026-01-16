import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

public abstract class Enemy {

    protected double maxHealth;
    protected double healthLeft;
    protected double bodyDamage;
    protected double movementSpeed;

    protected double x;
    protected double y;
    protected double radius;
    protected Color customColor = null; // If set, use this color instead of default

    public Enemy(double x,
                 double y,
                 double radius,
                 double maxHealth,
                 double bodyDamage,
                 double movementSpeed) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.maxHealth = maxHealth;
        this.healthLeft = maxHealth;
        this.bodyDamage = bodyDamage;
        this.movementSpeed = movementSpeed;
    }

    public abstract void update(double deltaSeconds,
                                Character player,
                                List<Bullet> bullets,
                                int mapWidth,
                                int mapHeight);

    public abstract void draw(Graphics2D g2);

    protected void moveTowards(double targetX,
                               double targetY,
                               double deltaSeconds,
                               int mapWidth,
                               int mapHeight) {
        double dx = targetX - x;
        double dy = targetY - y;
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

        // Clamp inside map
        double minX = radius;
        double maxX = mapWidth - radius;
        double minY = radius;
        double maxY = mapHeight - radius;

        if (x < minX) x = minX;
        if (x > maxX) x = maxX;
        if (y < minY) y = minY;
        if (y > maxY) y = maxY;
    }

    public boolean isAlive() {
        return healthLeft > 0;
    }

    public void takeDamage(double amount) {
        healthLeft -= amount;
        if (healthLeft < 0) {
            healthLeft = 0;
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

    protected void drawHealthBar(Graphics2D g2) {
        int barWidth = 40;
        int barHeight = 6;

        double hpPercent = healthLeft / maxHealth;
        if (hpPercent < 0) hpPercent = 0;
        if (hpPercent > 1) hpPercent = 1;

        int xLeft = (int) (x - barWidth / 2.0);
        int yTop = (int) (y - radius - 12);

        // Background
        g2.setColor(new Color(40, 40, 40, 220));
        g2.fillRect(xLeft - 1, yTop - 1, barWidth + 2, barHeight + 2);

        // Empty bar
        g2.setColor(new Color(90, 0, 0));
        g2.fillRect(xLeft, yTop, barWidth, barHeight);

        // Filled portion
        int filled = (int) (barWidth * hpPercent);
        g2.setColor(new Color(0, 220, 0));
        g2.fillRect(xLeft, yTop, filled, barHeight);
    }
}


