package enemy;

/*
Name: ShooterEnemy.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Ranged enemy that maintains distance and fires projectiles. Keeps away from player while shooting, creating need for aggressive positioning. Pentagon-shaped for visual distinction.
*/

import entity.Character;
import entity.Bullet;
import util.MathUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;

public class ShooterEnemy extends Enemy {

    private static final int SCORE_VALUE = 30;
    private static final double FIRE_INTERVAL_SECONDS = 1.5;
    private static final double BULLET_SPEED = 900; // fast projectiles
    private static final double BULLET_DAMAGE = 5;
    private static final double STOP_DISTANCE = 400; // stops moving when this close to player
    private static final double SHOOT_RANGE = 500; // only shoots within this range
    private static final int SIDES = 5; // pentagon shape
    private static final Color BODY_COLOR = new Color(180, 120, 255);

    private double timeSinceLastShot = 0; // tracks firing cooldown

    public ShooterEnemy(double x,
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
        faceTowards(player.getX(), player.getY()); // always aim at player

        // maintain distance - only approach if too far away
        if (distance > STOP_DISTANCE) {
            moveTowards(player.getX(), player.getY(), deltaSeconds, mapWidth, mapHeight);
        }

        // shoot at player if in range and cooldown expired
        if (distance <= SHOOT_RANGE) {
            timeSinceLastShot += deltaSeconds;
            if (timeSinceLastShot >= FIRE_INTERVAL_SECONDS) {
                shootAt(player, bullets);
                timeSinceLastShot = 0; // reset cooldown
            }
        }
    }

    private void shootAt(Character player, List<Bullet> bullets) {
        double[] direction = MathUtils.normalize(player.getX() - x, player.getY() - y);
        double velocityX = direction[0] * BULLET_SPEED;
        double velocityY = direction[1] * BULLET_SPEED;
        bullets.add(new Bullet(x, y, velocityX, velocityY, BULLET_SPEED, BULLET_DAMAGE, false));
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

        Polygon pentagon = new Polygon(xPoints, yPoints, SIDES);
        g2.setColor(BODY_COLOR);
        g2.fillPolygon(pentagon);
        g2.setColor(Color.DARK_GRAY);
        g2.drawPolygon(pentagon);
    }
}
