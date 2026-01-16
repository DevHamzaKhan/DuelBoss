import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

public class ShooterEnemy extends Enemy {

    private double timeSinceLastShot = 0;

    // Shooter-specific stats
    private final double fireIntervalSeconds = 1.5; // time between shots
    private final double bulletSpeed = 900;
    private final double bulletDamage = 5; // half damage: 10 -> 5

    public ShooterEnemy(double x,
            double y,
            double radius,
            double maxHealth,
            double bodyDamage,
            double movementSpeed) {
        super(x, y, radius, maxHealth, bodyDamage, movementSpeed);
    }

    @Override
    public void update(double deltaSeconds,
            Character player,
            List<Bullet> bullets,
            int mapWidth,
            int mapHeight) {

        double dx = player.getX() - x;
        double dy = player.getY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        // Always face the player
        faceTowards(player.getX(), player.getY());

        // Movement:
        // - If farther than 500px: move in until we get closer.
        // - If between 500 and 400: move toward the player while also shooting.
        // - If within 400px: stop moving and just act as a turret.
        if (dist > 400) {
            moveTowards(player.getX(), player.getY(), deltaSeconds, mapWidth, mapHeight);
        }

        // Shooting logic
        // Can only shoot when within 500px of the character.
        if (dist <= 500) {
            timeSinceLastShot += deltaSeconds;
            if (timeSinceLastShot >= fireIntervalSeconds) {
                timeSinceLastShot = 0;

                // Recompute direction after any movement this frame
                double sdx = player.getX() - x;
                double sdy = player.getY() - y;
                double len = Math.sqrt(sdx * sdx + sdy * sdy);
                if (len == 0) {
                    sdx = 1;
                    sdy = 0;
                    len = 1;
                }

                double vx = (sdx / len) * bulletSpeed;
                double vy = (sdy / len) * bulletSpeed;

                Bullet bullet = new Bullet(x, y, vx, vy, bulletSpeed, bulletDamage, false);
                bullets.add(bullet);
            }
        }
    }

    @Override
    protected void drawBody(Graphics2D g2) {
        int r = (int) radius;

        // Draw a simple regular pentagon
        int sides = 5;
        int[] xs = new int[sides];
        int[] ys = new int[sides];
        for (int i = 0; i < sides; i++) {
            double ang = -Math.PI / 2 + i * 2 * Math.PI / sides;
            xs[i] = (int) (Math.cos(ang) * r);
            ys[i] = (int) (Math.sin(ang) * r);
        }

        Polygon pentagon = new Polygon(xs, ys, sides);

        g2.setColor(new Color(180, 120, 255));
        g2.fillPolygon(pentagon);

        g2.setColor(Color.DARK_GRAY);
        g2.drawPolygon(pentagon);
    }
}
