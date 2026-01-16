import java.awt.Color;
import java.awt.Graphics2D;

public class CircleEnemy extends Enemy {

    // Radius of the force field around the enemy (larger than the body radius)
    private final double forceFieldRadius;

    public CircleEnemy(double x,
                       double y,
                       double radius,
                       double maxHealth,
                       double explosionDamage,
                       double movementSpeed,
                       double forceFieldRadius) {
        super(x, y, radius, maxHealth, explosionDamage, movementSpeed);
        this.forceFieldRadius = forceFieldRadius;
    }

    @Override
    public void update(double deltaSeconds,
                       Character player,
                       java.util.List<Bullet> bullets,
                       int mapWidth,
                       int mapHeight) {
        // If player enters the force field, explode immediately
        double dx = player.getX() - x;
        double dy = player.getY() - y;
        double distanceSq = dx * dx + dy * dy;
        double triggerRadius = forceFieldRadius + player.getRadius();

        if (distanceSq <= triggerRadius * triggerRadius) {
            // Explode: damage player and kill self
            player.takeDamage(bodyDamage);
            healthLeft = 0;
            return;
        }

        // Otherwise, move slowly toward the player
        moveWithDirection(dx, dy, deltaSeconds, mapWidth, mapHeight);
    }

    @Override
    public void draw(Graphics2D g2) {
        int cx = (int) x;
        int cy = (int) y;
        int bodyR = (int) radius;

        // Draw the force field (larger, faint circle)
        int ffR = (int) forceFieldRadius;
        g2.setColor(new Color(150, 150, 255, 40));
        g2.fillOval(cx - ffR, cy - ffR, ffR * 2, ffR * 2);

        g2.setColor(new Color(120, 120, 255, 120));
        g2.drawOval(cx - ffR, cy - ffR, ffR * 2, ffR * 2);

        // Draw the main body
        g2.setColor(new Color(120, 120, 255));
        g2.fillOval(cx - bodyR, cy - bodyR, bodyR * 2, bodyR * 2);

        g2.setColor(Color.WHITE);
        g2.drawOval(cx - bodyR, cy - bodyR, bodyR * 2, bodyR * 2);

        // Health bar above enemy
        drawHealthBar(g2);
    }
}


