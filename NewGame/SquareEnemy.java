import java.awt.Color;
import java.awt.Graphics2D;

public class SquareEnemy extends Enemy {

    // How far away it can "see" bullets to dodge (in pixels, from its center)
    private final double dodgeRadius;

    public SquareEnemy(double x,
            double y,
            double halfSize,
            double maxHealth,
            double bodyDamage,
            double movementSpeed,
            double dodgeRadius) {
        super(x, y, halfSize, maxHealth, bodyDamage, movementSpeed);
        this.dodgeRadius = dodgeRadius;
    }

    @Override
    public void update(double deltaSeconds,
            Character player,
            java.util.List<Bullet> bullets,
            int mapWidth,
            int mapHeight) {

        // Find closest bullet within dodgeRadius
        Bullet closest = null;
        double closestDistSq = Double.MAX_VALUE;

        for (Bullet bullet : bullets) {
            double dx = bullet.getX() - x;
            double dy = bullet.getY() - y;
            double distSq = dx * dx + dy * dy;
            if (distSq <= dodgeRadius * dodgeRadius && distSq < closestDistSq) {
                closestDistSq = distSq;
                closest = bullet;
            }
        }

        // Always face the player
        faceTowards(player.getX(), player.getY());

        if (closest != null) {
            // There is at least one nearby bullet: only dodge, don't move toward player.
            double vx = closest.getVx();
            double vy = closest.getVy();
            double len = Math.sqrt(vx * vx + vy * vy);
            if (len > 0) {
                double dirX = vx / len;
                double dirY = vy / len;

                // Perpendicular direction to the bullet trajectory
                double dodgeX = -dirY;
                double dodgeY = dirX;

                moveWithDirection(dodgeX, dodgeY, deltaSeconds, mapWidth, mapHeight);
            }
            // If bullet velocity is zero, just don't move this frame.
        } else {
            // No bullets close: move straight toward the player.
            moveTowards(player.getX(), player.getY(), deltaSeconds, mapWidth, mapHeight);
        }
    }

    public double getDodgeRadius() {
        return dodgeRadius;
    }

    @Override
    protected void drawBody(Graphics2D g2) {
        int half = (int) radius;
        int size = half * 2;

        // Use custom color if set, otherwise use default orange
        if (customColor != null) {
            g2.setColor(customColor);
        } else {
            g2.setColor(new Color(255, 150, 80));
        }
        g2.fillRect(-half, -half, size, size);

        g2.setColor(Color.BLACK);
        g2.drawRect(-half, -half, size, size);
    }
}
