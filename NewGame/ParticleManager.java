import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class ParticleManager {
    private final List<DeathParticle> particles = new ArrayList<>();

    public void spawnDeathEffect(Enemy e) {
        Color c = e.customColor != null ? e.customColor : Color.WHITE;
        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3 + Math.random() * 0.3;
            DeathParticle p = new DeathParticle(e.getX(), e.getY(), angle, 300 + Math.random() * 100, c, 0);
            particles.add(p);
            p.spawnChildren(particles, 2); // TRUE RECURSION - spawns all layers immediately
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
