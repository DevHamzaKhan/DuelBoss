package com.polygonwars.enemy;

import com.polygonwars.entity.Character;
import com.polygonwars.entity.Bullet;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

public class HexagonEnemy extends Enemy {

    // Hexagon gives no direct score (splits into triangles on death)
    private static final int SCORE_VALUE = 0;
    private static final int SIDES = 6;
    private static final Color BODY_COLOR = new Color(120, 200, 120);

    public HexagonEnemy(double x,
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

        for (int i = 0; i < SIDES; i++) {
            double angle = -Math.PI / 2 + i * 2 * Math.PI / SIDES;
            xPoints[i] = (int) (Math.cos(angle) * r);
            yPoints[i] = (int) (Math.sin(angle) * r);
        }

        Polygon hex = new Polygon(xPoints, yPoints, SIDES);
        g2.setColor(BODY_COLOR);
        g2.fillPolygon(hex);
        g2.setColor(Color.DARK_GRAY);
        g2.drawPolygon(hex);
    }
}
