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
        // Use defensive AI behavior - suits tanky earth element
        setAIBehavior(new DefensiveAIBehavior(500, 350, 200, 250));
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
