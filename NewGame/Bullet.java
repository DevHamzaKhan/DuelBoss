import java.awt.Color;
import java.awt.Graphics2D;

public class Bullet extends Entity {

    private static final int BULLET_RADIUS = 6;

    private final double speed;
    private final double damage;
    private double vx;
    private double vy;
    private final boolean fromPlayer;

    public Bullet(double x, double y, double vx, double vy, double speed, double damage, boolean fromPlayer) {
        super(x, y, BULLET_RADIUS, 1);
        this.vx = vx;
        this.vy = vy;
        this.speed = speed;
        this.damage = damage;
        this.fromPlayer = fromPlayer;
    }

    public void update(double deltaSeconds) {
        x += vx * deltaSeconds;
        y += vy * deltaSeconds;
    }

    @Override
    public void draw(Graphics2D g2) {
        int drawX = (int) (x - radius);
        int drawY = (int) (y - radius);
        int size = (int) (radius * 2);

        g2.setColor(new Color(255, 210, 70));
        g2.fillOval(drawX, drawY, size, size);

        g2.setColor(Color.ORANGE);
        g2.drawOval(drawX, drawY, size, size);
    }

    public boolean isOutOfBounds(int minX, int minY, int maxX, int maxY) {
        return x < minX - radius || x > maxX + radius || y < minY - radius || y > maxY + radius;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public boolean isFromPlayer() {
        return fromPlayer;
    }

    public double getSpeed() {
        return speed;
    }

    public double getDamage() {
        return damage;
    }
}
