package enemy;

/*
Name: OctagonEnemy.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Tanky enemy with simple chasing behavior
*/

import entity.Character;
import entity.Bullet;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

public class OctagonEnemy extends Enemy {

    private static final int SCORE_VALUE = 50;
    private static final int SIDES = 8;
    private static final Color DEFAULT_COLOR = new Color(150, 80, 200);

    public OctagonEnemy(double x,
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
        moveTowards(player.getX(), player.getY(), deltaSeconds, mapWidth, mapHeight);
    }

    @Override
    protected void drawBody(Graphics2D g2) {
        int r = (int) radius;
        int[] xPoints = new int[SIDES];
        int[] yPoints = new int[SIDES];

        // calculate octagon vertices evenly spaced around circle
        for (int i = 0; i < SIDES; i++) {
            double angle = i * (2 * Math.PI / SIDES) - Math.PI / 2.0;
            xPoints[i] = (int) (Math.cos(angle) * r);
            yPoints[i] = (int) (Math.sin(angle) * r);
        }

        Polygon octagon = new Polygon(xPoints, yPoints, SIDES);
        g2.setColor(customColor != null ? customColor : DEFAULT_COLOR);
        g2.fillPolygon(octagon);
        g2.setColor(Color.DARK_GRAY);
        g2.drawPolygon(octagon);
    }
}
