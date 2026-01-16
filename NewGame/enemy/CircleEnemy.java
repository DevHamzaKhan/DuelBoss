package enemy;

/*
Name: CircleEnemy.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Suicide bomber enemy with visible force field radius. Explodes when player enters force field.
*/

import entity.Character;
import entity.Bullet;
import util.MathUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

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
            List<Bullet> bullets,
            int mapWidth,
            int mapHeight) {
        double dx = player.getX() - x;
        double dy = player.getY() - y;
        double distanceSq = MathUtils.distanceSquared(x, y, player.getX(), player.getY());
        // calculate trigger distance accounting for both force field and player radius
        // this ensures explosion triggers when player's edge touches field edge, not
        // just center
        double triggerRadius = forceFieldRadius + player.getRadius();

        // check if player has entered the force field - if so, explode
        // we use squared distance comparison to avoid expensive sqrt calculation
        if (distanceSq <= triggerRadius * triggerRadius) {
            player.takeDamage(bodyDamage); // deal explosion damage
            healthLeft = 0; // destroy self
            return;
        }

        // chase player if not in explosion range
        faceTowards(player.getX(), player.getY());
        moveWithDirection(dx, dy, deltaSeconds, mapWidth, mapHeight);
    }

    // overrides default draw to render force field before body
    // force field provides visual warning to player about danger zone
    @Override
    public void draw(Graphics2D g2) {
        int centerX = (int) x;
        int centerY = (int) y;
        int bodyRadius = (int) radius;
        int fieldRadius = (int) forceFieldRadius;

        // draw transparent force field first (background layer)
        g2.setColor(FORCE_FIELD_FILL);
        g2.fillOval(centerX - fieldRadius, centerY - fieldRadius, fieldRadius * 2, fieldRadius * 2);
        g2.setColor(FORCE_FIELD_BORDER);
        g2.drawOval(centerX - fieldRadius, centerY - fieldRadius, fieldRadius * 2, fieldRadius * 2);

        // draw main body on top
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
