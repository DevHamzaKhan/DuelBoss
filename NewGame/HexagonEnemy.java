import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

public class HexagonEnemy extends Enemy {

    public HexagonEnemy(double x,
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
        // Tanky chaser: just move toward the player
        moveTowards(player.getX(), player.getY(), deltaSeconds, mapWidth, mapHeight);
    }

    @Override
    public void draw(Graphics2D g2) {
        int cx = (int) x;
        int cy = (int) y;
        int r = (int) radius;

        int sides = 6;
        int[] xs = new int[sides];
        int[] ys = new int[sides];
        for (int i = 0; i < sides; i++) {
            double angle = -Math.PI / 2 + i * 2 * Math.PI / sides;
            xs[i] = cx + (int) (Math.cos(angle) * r);
            ys[i] = cy + (int) (Math.sin(angle) * r);
        }

        Polygon hex = new Polygon(xs, ys, sides);

        g2.setColor(new Color(120, 200, 120));
        g2.fillPolygon(hex);

        g2.setColor(Color.DARK_GRAY);
        g2.drawPolygon(hex);

        // Health bar above enemy
        drawHealthBar(g2);
    }
}


