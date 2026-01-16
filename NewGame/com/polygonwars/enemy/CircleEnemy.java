package com.polygonwars.enemy;

import com.polygonwars.entity.Character;
import com.polygonwars.entity.Bullet;
import com.polygonwars.util.MathUtils;

import java.awt.Color;
import java.awt.Graphics2D;

public class CircleEnemy extends Enemy {

    private static final int SCORE_VALUE = 20;
    private static final Color FORCE_FIELD_FILL = new Color(150, 150, 255, 40);
    private static final Color FORCE_FIELD_BORDER = new Color(120, 120, 255, 120);
    private static final Color BODY_COLOR = new Color(120, 120, 255);

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
    public int getScoreValue() {
        return SCORE_VALUE;
    }

    @Override
    public void update(double deltaSeconds,
            Character player,
            java.util.List<Bullet> bullets,
            int mapWidth,
            int mapHeight) {
        double dx = player.getX() - x;
        double dy = player.getY() - y;
        double distanceSq = MathUtils.distanceSquared(x, y, player.getX(), player.getY());
        double triggerRadius = forceFieldRadius + player.getRadius();

        if (distanceSq <= triggerRadius * triggerRadius) {
            player.takeDamage(bodyDamage);
            healthLeft = 0;
            return;
        }

        faceTowards(player.getX(), player.getY());
        moveWithDirection(dx, dy, deltaSeconds, mapWidth, mapHeight);
    }

    @Override
    public void draw(Graphics2D g2) {
        int centerX = (int) x;
        int centerY = (int) y;
        int bodyRadius = (int) radius;
        int fieldRadius = (int) forceFieldRadius;

        // Draw force field
        g2.setColor(FORCE_FIELD_FILL);
        g2.fillOval(centerX - fieldRadius, centerY - fieldRadius, fieldRadius * 2, fieldRadius * 2);
        g2.setColor(FORCE_FIELD_BORDER);
        g2.drawOval(centerX - fieldRadius, centerY - fieldRadius, fieldRadius * 2, fieldRadius * 2);

        // Draw main body
        g2.setColor(BODY_COLOR);
        g2.fillOval(centerX - bodyRadius, centerY - bodyRadius, bodyRadius * 2, bodyRadius * 2);
        g2.setColor(Color.WHITE);
        g2.drawOval(centerX - bodyRadius, centerY - bodyRadius, bodyRadius * 2, bodyRadius * 2);

        drawHealthBar(g2);
    }

    @Override
    protected void drawBody(Graphics2D g2) {
        // Not used - CircleEnemy overrides draw() completely
    }
}
