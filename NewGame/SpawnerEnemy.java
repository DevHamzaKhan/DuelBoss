import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

public class SpawnerEnemy extends Enemy {

    private double timeSinceLastSpawn = 0;
    private int spawnCount = 0; // Track which enemy to spawn next (0=triangle, 1=triangle, 2=square, repeat)
    
    // Spawner-specific stats
    private final double spawnIntervalSeconds = 3.0; // spawn every 3 seconds
    private final double spawnRange = 500.0; // within 500 pixels of player

    public SpawnerEnemy(double x,
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
        // Calculate distance to player
        double dx = player.getX() - x;
        double dy = player.getY() - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Only move towards player, but don't chase aggressively
        if (distance > 300) {
            moveTowards(player.getX(), player.getY(), deltaSeconds, mapWidth, mapHeight);
        }
        
        // Update spawn timer
        timeSinceLastSpawn += deltaSeconds;
    }
    
    /**
     * Spawn enemies if within range of player and spawn timer is ready.
     * Returns true if enemies were spawned.
     */
    public boolean trySpawn(Character player, List<Enemy> collector) {
        double dx = player.getX() - x;
        double dy = player.getY() - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // Only spawn if within 500 pixels of player
        if (distance > spawnRange) {
            return false;
        }
        
        // Check if it's time to spawn
        if (timeSinceLastSpawn >= spawnIntervalSeconds) {
            // Reset timer after spawning
            timeSinceLastSpawn = 0;
            
            // Spawn based on pattern: triangle, triangle, square (repeat)
            // spawnCount % 3: 0=triangle, 1=triangle, 2=square
            Color brightYellow = new Color(255, 255, 0); // Bright yellow
            
            if (spawnCount % 3 == 2) {
                // Spawn square enemy (bright yellow)
                double halfSize = 22;
                double dodgeRadius = 150;
                SquareEnemy square = new SquareEnemy(x, y, halfSize, 60, 7.5, 320, dodgeRadius);
                square.customColor = brightYellow;
                collector.add(square);
            } else {
                // Spawn triangle enemy (bright yellow)
                double r = 24;
                TriangleEnemy triangle = new TriangleEnemy(x, y, r, 50, 5, 260);
                triangle.customColor = brightYellow;
                collector.add(triangle);
            }
            
            spawnCount++;
            return true;
        }
        
        return false;
    }

    @Override
    public void draw(Graphics2D g2) {
        int cx = (int) x;
        int cy = (int) y;
        int r = (int) radius;

        // Draw a 5-pointed star (bright yellow)
        int[] xs = new int[10];
        int[] ys = new int[10];
        
        // Calculate star points
        for (int i = 0; i < 10; i++) {
            double angle = i * Math.PI / 5.0 - Math.PI / 2.0; // Start from top
            double dist = (i % 2 == 0) ? r : r * 0.4; // Alternate between outer and inner radius
            xs[i] = cx + (int)(Math.cos(angle) * dist);
            ys[i] = cy + (int)(Math.sin(angle) * dist);
        }
        
        Polygon star = new Polygon(xs, ys, 10);

        // Bright yellow color
        g2.setColor(new Color(255, 255, 0)); // Bright yellow
        g2.fillPolygon(star);

        g2.setColor(new Color(200, 200, 0)); // Slightly darker yellow for border
        g2.drawPolygon(star);

        // Health bar above enemy
        drawHealthBar(g2);
    }
}

