import java.awt.*;
import java.util.ArrayList;

/**
 * EarthBoss - Defensive tank with earthquake area attack
 * Demonstrates proper OOP: uses DefensiveAIBehavior, composition, and custom attacks
 */
public class EarthBoss extends Boss {
    private AreaAttack earthquake;

    public EarthBoss(int x, int y) {
        super(x, y, 400, 2.0, "Earth Boss", new Color(139, 90, 43));
        // Use aggressive AI behavior like other bosses
        setAIBehavior(new AggressiveAIBehavior(500, 300, 100));
        loadSprites();
        initializeAttacks();
        initializeSpecialAttack();
    }
    
    /**
     * Load all Earth Boss sprite animations (Minotaur)
     */
    private void loadSprites() {
        String basePath = "EarthBoss/animations/";
        
        // Load idle animation (16 frames)
        animationManager.loadAnimation("idle", basePath + "idle/idle_", 16, 4);
        
        // Load walk animation (12 frames)
        animationManager.loadAnimation("run", basePath + "walk/walk_", 12, 3);
        
        // Load attack animation (16 frames)
        animationManager.loadAnimation("attack1", basePath + "atk_1/atk_1_", 16, 2);
        
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
            // Earth boss sprites are facing opposite direction, so invert facingRight
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
        // Custom projectile attack with arc trajectory
        ProjectileAttack ranged = new ProjectileAttack(25, 90, 5.0, new Color(139, 90, 43)) {
            @Override
            public void execute(ArrayList<Characters> targets) {
                if (!canUse() || owner == null) return;

                int projX = owner.isFacingRight() ? owner.getX() + owner.getWidth() : owner.getX() - 20;
                int projY = owner.getY() + owner.getHeight() / 2;
                double velX = owner.isFacingRight() ? 5.0 : -5.0;
                spawnProjectile(projX, projY, velX, -2);
                startCooldown();
            }
        };
        attackManager.setPrimaryRanged(ranged);

        MeleeAttack melee = new MeleeAttack(35, 60, 70, 25, new Color(101, 67, 33));
        attackManager.setPrimaryMelee(melee);
        
        // Keep backwards compatibility
        rangedAttack = ranged;
        meleeAttack = melee;
    }

    @Override
    protected void initializeSpecialAttack() {
        earthquake = new AreaAttack(30, 350, 200, 40, new Color(139, 90, 43));
        attackManager.registerAttack("earthquake", earthquake);
        specialCooldown = 350;
    }

    @Override
    protected void executeSpecialAttack() {
        if (earthquake.canUse()) {
            earthquake.execute(targetList);
            for (Characters t : targetList) {
                t.stun(60);
            }
        }
    }

    @Override
    protected void updateSpecialAttack() {
        specialTimer--;
        if (specialTimer <= 0) {
            executeSpecialAttack();
            specialTimer = specialCooldown;
        }
        earthquake.update();
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);
        earthquake.draw(g);
    }
}
