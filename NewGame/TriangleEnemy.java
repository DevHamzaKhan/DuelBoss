import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

public class TriangleEnemy extends Enemy {

    // If > 0, this triangle is in an "explosion" phase where it flies outward
    // along (explodeDirX, explodeDirY) at explodeSpeed, instead of homing.
    private double explodeTimeRemaining = 0.0;
    private double explodeDirX = 0.0;
    private double explodeDirY = 0.0;
    private double explodeSpeed = 0.0;

    public TriangleEnemy(double x,
            double y,
            double radius,
            double maxHealth,
            double bodyDamage,
            double movementSpeed) {
        super(x, y, radius, maxHealth, bodyDamage, movementSpeed);
    }

    /**
     * Configure this triangle to start in an explosion phase, flying outward
     * for the given duration (seconds) in the given direction at the given speed.
     */
    public void startExplosionPhase(double dirX, double dirY, double durationSeconds, double speed) {
        double len = Math.sqrt(dirX * dirX + dirY * dirY);
        if (len == 0) {
            dirX = 1;
            dirY = 0;
            len = 1;
        }
        this.explodeDirX = dirX / len;
        this.explodeDirY = dirY / len;
        this.explodeTimeRemaining = Math.max(0, durationSeconds);
        this.explodeSpeed = speed;
    }

    @Override
    public void update(double deltaSeconds,
            Character player,
            java.util.List<Bullet> bullets,
            int mapWidth,
            int mapHeight) {
        if (explodeTimeRemaining > 0) {
            // Move straight outwards for the explosion phase
            double moveX = explodeDirX * explodeSpeed;
            double moveY = explodeDirY * explodeSpeed;
            moveWithDirection(moveX, moveY, deltaSeconds, mapWidth, mapHeight);
            explodeTimeRemaining -= deltaSeconds;
        } else {
            // Normal homing behavior toward the player
            moveTowards(player.getX(), player.getY(), deltaSeconds, mapWidth, mapHeight);
        }
    }

    @Override
    protected void drawBody(Graphics2D g2) {
        int r = (int) radius;

        // Triangle pointing up (will rotate to face movement direction)
        int[] xs = { 0, -r, r };
        int[] ys = { -r, r, r };

        Polygon triangle = new Polygon(xs, ys, 3);

        // Use custom color if set, otherwise use default red
        if (customColor != null) {
            g2.setColor(customColor);
        } else {
            g2.setColor(new Color(255, 80, 80));
        }
        g2.fillPolygon(triangle);

        g2.setColor(Color.DARK_GRAY);
        g2.drawPolygon(triangle);
    }
}
