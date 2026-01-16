import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

public abstract class Enemy extends Entity {

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
        int barWidth = 40;
        int barHeight = 6;

        double hpPercent = healthLeft / maxHealth;
        if (hpPercent < 0)
            hpPercent = 0;
        if (hpPercent > 1)
            hpPercent = 1;

        int xLeft = (int) (x - barWidth / 2.0);
        int yTop = (int) (y - radius - 12);

        g2.setColor(new Color(40, 40, 40, 220));
        g2.fillRect(xLeft - 1, yTop - 1, barWidth + 2, barHeight + 2);

        g2.setColor(new Color(90, 0, 0));
        g2.fillRect(xLeft, yTop, barWidth, barHeight);

        int filled = (int) (barWidth * hpPercent);
        g2.setColor(new Color(0, 220, 0));
        g2.fillRect(xLeft, yTop, filled, barHeight);
    }
}
