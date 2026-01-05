import java.awt.*;
import java.util.ArrayList;

/**
 * FireBoss - Aggressive fire-themed boss with homing projectiles
 * Demonstrates proper OOP: inheritance, composition (AttackManager), and polymorphism
 */
public class FireBoss extends Boss {
    private ProjectileAttack homingAttack;

    public FireBoss(int x, int y) {
        super(x, y, 200, 5.0, "Fire Boss", new Color(255, 69, 0));
        // Use aggressive AI behavior - suits fire element
        setAIBehavior(new AggressiveAIBehavior(500, 300, 100));
        loadSprites();
        initializeAttacks();
        initializeSpecialAttack();
    }
    
    /**
     * Load all Fire Boss sprite animations
     */
    private void loadSprites() {
        String basePath = "FireBoss/individual sprites/";
        
        // Load idle animation (6 frames)
        animationManager.loadAnimation("idle", basePath + "01_demon_idle/demon_idle_", 6, 4);
        
        // Load walk animation (12 frames)
        animationManager.loadAnimation("run", basePath + "02_demon_walk/demon_walk_", 12, 3);
        
        // Load attack animation (15 frames)
        animationManager.loadAnimation("attack1", basePath + "03_demon_cleave/demon_cleave_", 15, 2);
        
        // Load take hit animation (5 frames)
        animationManager.loadAnimation("take_hit", basePath + "04_demon_take_hit/demon_take_hit_", 5, 3);
        
        // Load death animation (22 frames)
        animationManager.loadAnimation("death", basePath + "05_demon_death/demon_death_", 22, 4);
        
        // Set default animation
        animationManager.setAnimation("idle");
    }

    @Override
    protected void drawCharacter(Graphics2D g) {
        String animToUse = currentState;
        if (!animationManager.hasAnimation(currentState)) {
            animToUse = "idle";
        }
        if (animationManager.hasAnimation(animToUse)) {
            animationManager.setAnimation(animToUse);
            // Fire boss sprites are facing opposite direction, so invert facingRight
            animationManager.drawWithRatio(g, x, y, width, height, !facingRight, 12.0/36.0, 0.6);
        } else if (sprite != null) {
            if (!facingRight) {
                g.drawImage(sprite, x, y, width, height, null);
            } else {
                g.drawImage(sprite, x + width, y, -width, height, null);
            }
        } else {
            g.setColor(characterColor);
            g.fillRect(x, y, width, height);
            g.setColor(Color.BLACK);
            int eyeY = y + height / 4;
            int eyeSize = Math.max(width / 5, 6);
            if (!facingRight) {
                g.fillOval(x + width - eyeSize - 5, eyeY, eyeSize, eyeSize);
            } else {
                g.fillOval(x + 5, eyeY, eyeSize, eyeSize);
            }
        }
    }

    @Override
    protected void initializeAttacks() {
        // Use the new AttackManager for professional OOP design
        ProjectileAttack ranged = new ProjectileAttack(15, 50, 7.0, Color.ORANGE);
        attackManager.setPrimaryRanged(ranged);

        MeleeAttack melee = new MeleeAttack(25, 40, 55, 18, new Color(255, 100, 0));
        attackManager.setPrimaryMelee(melee);

        homingAttack = new ProjectileAttack(20, 200, 10.0, Color.RED);
        attackManager.registerAttack("homing", homingAttack);

        // Keep backwards compatibility
        rangedAttack = ranged;
        meleeAttack = melee;
    }

    @Override
    protected void initializeSpecialAttack() {
        specialAttack = new SpecialAttack(20, 200, 1) {
            @Override
            public void execute(ArrayList<Characters> targets) {
                if (!canUse() || targets.isEmpty()) return;
                activate();

                Characters t = targets.get(0);
                double dx = t.getX() - x;
                double dy = t.getY() - y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist > 0) {
                    dx = (dx / dist) * 10;
                    dy = (dy / dist) * 10;
                    homingAttack.spawnProjectile(x + width / 2, y + height / 2, dx, dy);
                }
            }

            @Override
            public void draw(Graphics2D g) {
                homingAttack.draw(g);
            }
        };
        specialAttack.setOwner(this);
        specialCooldown = 200;
    }

    @Override
    protected void updateSpecialAttack() {
        super.updateSpecialAttack();
        homingAttack.update();
    }

    @Override
    public void checkAttackCollisions(ArrayList<Characters> targets) {
        super.checkAttackCollisions(targets);
        homingAttack.checkCollisions(targets);
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);
        homingAttack.draw(g);
    }
}
