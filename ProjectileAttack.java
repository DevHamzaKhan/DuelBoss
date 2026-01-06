import java.awt.*;
import java.util.ArrayList;

public class ProjectileAttack extends BasicAttack {
    private double speed;
    private ArrayList<Projectile> projectiles;
    private Color color;

    public ProjectileAttack(int damage, int cooldown, double speed, Color color) {
        super(damage, cooldown, 0);
        this.speed = speed;
        this.projectiles = new ArrayList<>();
        this.color = color;
    }

    @Override
    public void execute(ArrayList<Characters> targets) {
        if (!canUse() || owner == null) return;

        int projX = owner.isFacingRight() ? owner.getX() + owner.getWidth() : owner.getX() - 10;
        int projY = owner.getY() + owner.getHeight() / 2;
        double velX = owner.isFacingRight() ? speed : -speed;

        projectiles.add(new Projectile(projX, projY, velX, 0, damage, color));
        startCooldown();

        SoundManager.playShot();
    }

    public void executeDirectional(double velX, double velY) {
        if (!canUse() || owner == null) return;

        int projX = owner.getX() + owner.getWidth() / 2;
        int projY = owner.getY() + owner.getHeight() / 2;

        projectiles.add(new Projectile(projX, projY, velX, velY, damage, color));
        startCooldown();

        SoundManager.playShot();
    }

    public void spawnProjectile(int x, int y, double velX, double velY) {
        projectiles.add(new Projectile(x, y, velX, velY, damage, color));
    }

    @Override
    public void update() {
        updateCooldown();
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.update();
            if (p.isOffScreen()) {
                projectiles.remove(i);
            }
        }
    }

    public void checkCollisions(ArrayList<Characters> targets) {
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            for (Characters target : targets) {
                if (target != owner && p.getBounds().intersects(target.getBounds())) {
                    target.takeDamage(p.getDamage());
                    onHit(target, p);
                    projectiles.remove(i);
                    break;
                }
            }
        }
    }

    protected void onHit(Characters target, Projectile p) {
    }

    @Override
    public void draw(Graphics2D g) {
        for (Projectile p : projectiles) {
            p.draw(g);
        }
    }

    @Override
    public Rectangle getHitbox() {
        return null;
    }

    public ArrayList<Projectile> getProjectiles() {
        return projectiles;
    }

    public void clearProjectiles() {
        projectiles.clear();
    }
}
