package com.polygonwars.particle;

import java.awt.*;
import java.util.List;

public class DeathParticle {
    double x, y, vx, vy, life = 0.25;
    Color color;
    int depth;
    boolean spawned;

    public DeathParticle(double x, double y, double angle, double speed, Color color, int depth) {
        this.x = x; this.y = y;
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
        this.color = color;
        this.depth = depth;
    }

    // TRUE RECURSIVE FUNCTION: spawns children which spawn grandchildren
    public void spawnChildren(List<DeathParticle> out, int maxDepth) {
        if (depth >= maxDepth) return;
        double angle = Math.atan2(vy, vx);
        double speed = Math.sqrt(vx*vx + vy*vy) * 0.6;
        for (int i = 0; i < 2; i++) {
            DeathParticle child = new DeathParticle(x, y, angle + (Math.random()-0.5)*1.2, speed, color, depth + 1);
            out.add(child);
            child.spawnChildren(out, maxDepth); // RECURSION
        }
    }

    public void update(double dt) {
        x += vx * dt; y += vy * dt;
        life -= dt;
    }

    public void draw(Graphics2D g2) {
        int a = (int)(Math.max(0, life/0.25) * 255);
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), a));
        g2.setStroke(new BasicStroke(2));
        g2.drawLine((int)(x - vx*0.02), (int)(y - vy*0.02), (int)x, (int)y);
    }

    public boolean isDead() { return life <= 0; }
}
