package enemy;

/*
Name: SpawnerEnemy.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Strategic enemy that generates additional enemies over time. Spawns triangles and squares in pattern, creating escalating threat if not eliminated quickly. Star-shaped for high visibility.
*/

import entity.Character;
import entity.Bullet;
import util.MathUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

public class SpawnerEnemy extends Enemy {

    private static final int SCORE_VALUE = 40;
    private static final double SPAWN_INTERVAL_SECONDS = 3.0; // time between spawns
    private static final double SPAWN_RANGE = 500.0; // only spawns when player is nearby
    private static final double KEEP_DISTANCE = 300; // maintains this distance from player
    private static final double STAR_INNER_RATIO = 0.4; // ratio for star inner points
    private static final int STAR_POINTS = 10; // 5-pointed star (10 vertices)
    private static final Color BODY_COLOR = new Color(255, 255, 0);
    private static final Color BORDER_COLOR = new Color(200, 200, 0);
    private static final Color SPAWN_COLOR = new Color(255, 255, 0); // color for spawned enemies

    private double timeSinceLastSpawn = 0;
    private int spawnCount = 0; // tracks spawn pattern: triangle, triangle, square, repeat

    public SpawnerEnemy(double x,
            double y,
            double radius,
            double maxHealth,
            double bodyDamage,
            double movementSpeed) {
        super(x, y, radius, maxHealth, bodyDamage, movementSpeed);
    }

    @Override
    public int getScoreValue() {
        return SCORE_VALUE;
    }

    @Override
    public void update(double deltaSeconds,
            Character player,
            List<Bullet> bullets,
            int mapWidth,
            int mapHeight) {
        double distance = MathUtils.distance(x, y, player.getX(), player.getY());
        faceTowards(player.getX(), player.getY());

        // maintain safe distance from player while staying in spawn range
        if (distance > KEEP_DISTANCE) {
            moveTowards(player.getX(), player.getY(), deltaSeconds, mapWidth, mapHeight);
        }

        timeSinceLastSpawn += deltaSeconds; // always increment timer
    }

    // spawns enemies if within range of player and spawn timer is ready
    // must be called externally from game loop (not in update)
    public boolean trySpawn(Character player, List<Enemy> collector) {
        double distance = MathUtils.distance(x, y, player.getX(), player.getY());

        if (distance > SPAWN_RANGE) {
            return false; // too far from player
        }

        if (timeSinceLastSpawn >= SPAWN_INTERVAL_SECONDS) {
            timeSinceLastSpawn = 0;
            spawnEnemy(collector);
            spawnCount++;
            return true;
        }

        return false;
    }

    // spawns enemies in repeating pattern: triangle, triangle, square
    // spawned enemies inherit yellow color for visual distinction
    private void spawnEnemy(List<Enemy> collector) {
        // pattern: triangle, triangle, square (repeats)
        if (spawnCount % 3 == 2) {
            SquareEnemy square = new SquareEnemy(x, y, 22, 60, 7.5, 320, 150);
            square.customColor = SPAWN_COLOR; // mark as spawned
            collector.add(square);
        } else {
            TriangleEnemy triangle = new TriangleEnemy(x, y, 24, 50, 5, 260);
            triangle.customColor = SPAWN_COLOR; // mark as spawned
            collector.add(triangle);
        }
    }

    @Override
    protected void drawBody(Graphics2D g2) {
        int r = (int) radius;
        int[] xPoints = new int[STAR_POINTS];
        int[] yPoints = new int[STAR_POINTS];

        for (int i = 0; i < STAR_POINTS; i++) {
            double angle = i * Math.PI / 5.0 - Math.PI / 2.0;
            double dist = (i % 2 == 0) ? r : r * STAR_INNER_RATIO;
            xPoints[i] = (int) (Math.cos(angle) * dist);
            yPoints[i] = (int) (Math.sin(angle) * dist);
        }

        Polygon star = new Polygon(xPoints, yPoints, STAR_POINTS);
        g2.setColor(BODY_COLOR);
        g2.fillPolygon(star);
        g2.setColor(BORDER_COLOR);
        g2.drawPolygon(star);
    }
}
