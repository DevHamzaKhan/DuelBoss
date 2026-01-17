/*
Name: DeathParticle.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Recursive particle for enemy deaths. Each particle spawns two smaller children.
*/

package particle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

public class DeathParticle {

    private static final double LIFESPAN = 0.25; // seconds per particle
    private static final double CHILD_SPEED_RATIO = 0.6; // children move 60% as fast
    private static final double ANGLE_SPREAD = 1.2; // randomization range for child angles
    private static final double TRAIL_LENGTH = 0.02; // trail extends backward by velocity * this

    private double x, y, vx, vy;
    private double life = LIFESPAN;
    private Color color;
    private int depth; // recursion depth (0 = parent, 1 = child, 2 = grandchild)

    public DeathParticle(double x, double y, double angle, double speed, Color color, int depth) {
        this.x = x;
        this.y = y;
        // convert polar coordinates (angle, speed) to cartesian velocity
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
        this.color = color;
        this.depth = depth;
    }

    // spawns children which immediately spawn their own children
    public void spawnChildren(List<DeathParticle> out, int maxDepth) {
        if (depth >= maxDepth)
            return; // base case: stop at max recursion depth

        // calculate child properties from parent velocity
        double angle = Math.atan2(vy, vx);
        double speed = Math.sqrt(vx * vx + vy * vy) * CHILD_SPEED_RATIO;

        // spawn 2 children per particle, each with slightly randomized angle
        for (int i = 0; i < 2; i++) {
            DeathParticle child = new DeathParticle(x, y, angle + (Math.random() - 0.5) * ANGLE_SPREAD, speed, color,
                    depth + 1);
            out.add(child);
            child.spawnChildren(out, maxDepth); // recursion: child spawns its own children
        }
    }

    public void update(double dt) {
        x += vx * dt;
        y += vy * dt;
        life -= dt;
    }

    // renders particle as a short line segment showing motion trail
    public void draw(Graphics2D g2) {
        // fade out as life decreases
        int a = (int) (Math.max(0, life / LIFESPAN) * 255);
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), a));
        g2.setStroke(new BasicStroke(2));
        // line extends backward along velocity direction for motion blur effect
        g2.drawLine((int) (x - vx * TRAIL_LENGTH), (int) (y - vy * TRAIL_LENGTH), (int) x, (int) y);
    }

    public boolean isDead() {
        return life <= 0;
    }
}
