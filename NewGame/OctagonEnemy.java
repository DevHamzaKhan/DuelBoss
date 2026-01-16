import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

public class OctagonEnemy extends Enemy {

    public OctagonEnemy(double x,
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
                       java.util.List<Bullet> bullets,
                       int mapWidth,
                       int mapHeight) {
        // Just move towards the player (tanky and slow)
        moveTowards(player.getX(), player.getY(), deltaSeconds, mapWidth, mapHeight);
    }

    @Override
    protected void drawBody(Graphics2D g2) {
        int r = (int) radius;

        // Draw a regular octagon (8 sides)
        int sides = 8;
        int[] xs = new int[sides];
        int[] ys = new int[sides];
        
        for (int i = 0; i < sides; i++) {
            double ang = i * (2 * Math.PI / sides) - Math.PI / 2.0; // Start from top
            xs[i] = (int)(Math.cos(ang) * r);
            ys[i] = (int)(Math.sin(ang) * r);
        }
        
        Polygon octagon = new Polygon(xs, ys, sides);

        // Use custom color if set, otherwise use default color (purple/blue for octagon)
        if (customColor != null) {
            g2.setColor(customColor);
        } else {
            g2.setColor(new Color(150, 80, 200)); // Purple color for octagon
        }
        g2.fillPolygon(octagon);

        g2.setColor(Color.DARK_GRAY);
        g2.drawPolygon(octagon);
    }
}

