/*
Name: DeathParticle.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Recursive particle effect for enemy deaths. Each particle spawns two children at smaller scale and velocity, creating cascading explosion. Uses true recursive function where parent calls spawnChildren on children during creation.
*/

package particle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

public class DeathParticle {
    double x, y, vx, vy, life = 0.25; // 0.25 second lifespan per particle
    Color color;
    int depth; // recursion depth (0 = parent, 1 = child, 2 = grandchild)
    boolean spawned;

    public DeathParticle(double x, double y, double angle, double speed, Color color, int depth) {
        this.x = x; this.y = y;
        // convert polar coordinates (angle, speed) to cartesian velocity
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
        this.color = color;
        this.depth = depth;
    }

    // true recursive function: spawns children which immediately spawn their own children
    // creates entire particle tree in one call (not iterative spawning over time)
    public void spawnChildren(List<DeathParticle> out, int maxDepth) {
        if (depth >= maxDepth) return; // base case: stop at max recursion depth
        
        // calculate child properties from parent velocity
        double angle = Math.atan2(vy, vx);
        double speed = Math.sqrt(vx*vx + vy*vy) * 0.6; // children move 60% as fast
        
        // spawn 2 children per particle, each with slightly randomized angle
        for (int i = 0; i < 2; i++) {
            DeathParticle child = new DeathParticle(x, y, angle + (Math.random()-0.5)*1.2, speed, color, depth + 1);
            out.add(child);
            child.spawnChildren(out, maxDepth); // recursion: child spawns its own children
        }
    }

    public void update(double dt) {
        x += vx * dt; y += vy * dt;
        life -= dt;
    }

    // renders particle as a short line segment showing motion trail
    public void draw(Graphics2D g2) {
        // fade out as life decreases (life/0.25 gives percentage remaining)
        int a = (int)(Math.max(0, life/0.25) * 255);
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), a));
        g2.setStroke(new BasicStroke(2));
        // line extends backward along velocity direction for motion blur effect
        g2.drawLine((int)(x - vx*0.02), (int)(y - vy*0.02), (int)x, (int)y);
    }

    public boolean isDead() { return life <= 0; }
}
