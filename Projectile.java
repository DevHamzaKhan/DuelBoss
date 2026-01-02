import java.awt.*;

public class Projectile {
    private double x, y;
    private double velocityX, velocityY;
    private int damage;
    private Color color;
    private int width = 10;
    private int height = 6;

    public Projectile(double x, double y, double velocityX, double velocityY, int damage, Color color) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.damage = damage;
        this.color = color;
    }

    public void update() {
        x += velocityX;
        y += velocityY;
    }

    public void draw(Graphics2D g) {
        g.setColor(color);
        g.fillOval((int)x, (int)y, width, height);
    }

    public boolean isOffScreen() {
        return x < -20 || x > Main.WIDTH + 20 || y < -20 || y > Main.HEIGHT + 20;
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, width, height);
    }

    public int getDamage() {
        return damage;
    }

    public double getVelocityX() {
        return velocityX;
    }
}
