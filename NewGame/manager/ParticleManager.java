/*
Name: ParticleManager.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Manages particle effects for enemy deaths. Uses true recursion to generate multi-level particle explosions (parent spawns children immediately). Handles particle lifecycle (update, render, cleanup).
*/

package manager;

import enemy.Enemy;
import particle.DeathParticle;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class ParticleManager {
    private final List<DeathParticle> particles = new ArrayList<>();

    // generates recursive particle explosion on enemy death
    // spawns 6 parent particles, each spawning 2 children in true recursion
    public void spawnDeathEffect(Enemy e) {
        Color c = e.getCustomColor() != null ? e.getCustomColor() : Color.WHITE;
        for (int i = 0; i < 6; i++) {
            // randomize angle slightly for natural spread
            double angle = i * Math.PI / 3 + Math.random() * 0.3;
            DeathParticle p = new DeathParticle(e.getX(), e.getY(), angle, 300 + Math.random() * 100, c, 0);
            particles.add(p);
            p.spawnChildren(particles, 2); // recursively spawns all child particles immediately
        }
    }

    public void update(double dt) {
        particles.removeIf(p -> { p.update(dt); return p.isDead(); });
    }

    public void draw(Graphics2D g2) {
        for (DeathParticle p : particles) p.draw(g2);
    }

    public void clear() { particles.clear(); }
}
