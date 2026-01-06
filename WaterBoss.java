import java.awt.*;
import java.util.ArrayList;

/**
 * WaterBoss - Defensive zoner with ranged attacks
 * Demonstrates proper OOP: composition, custom attacks, and defensive AI
 */
public class WaterBoss extends Boss {
    private PersistentAreaAttack whirlpool;
    private WaterProjectileAttack waterRanged;
    private boolean isAttacking;
    private boolean hasLaunchedProjectile; // Track if projectile launched on frame 4
    private ArrayList<Characters> attackTargets;

    public WaterBoss(int x, int y) {
        super(x, y, 80, 96, 200, 5.5, "Water Boss", new Color(0, 105, 148));
        // Use custom defensive AI - water boss maintains distance and uses ranged attacks
        setAIBehavior(new WaterBossAIBehavior());
        this.isAttacking = false;
        this.hasLaunchedProjectile = false;
        this.attackTargets = new ArrayList<>();
        loadSprites();
        initializeAttacks();
        initializeSpecialAttack();
    }
    
    /**
     * Load all Water Boss sprite animations (Water Priestess)
     */
    private void loadSprites() {
        String basePath = "WaterBoss/png/";
        
        // Load idle animation (8 frames)
        animationManager.loadAnimation("idle", basePath + "01_idle/idle_", 8, 4);
        
        // Load walk animation (10 frames)
        animationManager.loadAnimation("run", basePath + "02_walk/walk_", 10, 3);
        
        // Load jump up animation (3 frames)
        animationManager.loadAnimation("jump_up", basePath + "04_j_up/j_up_", 3, 2);
        
        // Load jump down animation (3 frames)
        animationManager.loadAnimation("jump_down", basePath + "05_j_down/j_down_", 3, 2);
        
        // Load attack animations
        animationManager.loadAnimation("attack1", basePath + "07_1_atk/1_atk_", 7, 2);
        animationManager.loadAnimation("attack2", basePath + "08_2_atk/2_atk_", 6, 2);
        animationManager.loadAnimation("attack3", basePath + "09_3_atk/3_atk_", 10, 2);
        
        // Load take hit animation (7 frames)
        animationManager.loadAnimation("take_hit", basePath + "13_take_hit/take_hit_", 7, 3);
        
        // Load death animation (15 frames) - count from 14_death folder
        animationManager.loadAnimation("death", basePath + "14_death/death_", 15, 3);
        
        // Set default animation
        animationManager.setAnimation("idle");
    }

    @Override
    protected void initializeAttacks() {
        waterRanged = new WaterProjectileAttack(10, 45, 7.0, Color.BLUE);
        attackManager.setPrimaryRanged(waterRanged);

        MeleeAttack melee = new MeleeAttack(15, 35, 70, 15, new Color(0, 150, 200));
        attackManager.setPrimaryMelee(melee);
        
        // Keep backwards compatibility
        rangedAttack = waterRanged;
        meleeAttack = melee;
    }

    @Override
    protected void initializeSpecialAttack() {
        whirlpool = new PersistentAreaAttack(5, 250, 180, 100, 30, 2.0, new Color(0, 100, 200));
        attackManager.registerAttack("whirlpool", whirlpool);
        specialAttack = whirlpool;
        specialCooldown = 250;
    }

    @Override
    protected void executeSpecialAttack() {
        if (target != null && whirlpool.canUse()) {
            whirlpool.executeAt(target.getX(), target.getY(), targetList);
        }
    }

    @Override
    public void updateAI(Platform[] platforms, double gravityMod, double speedMod) {
        super.updateAI(platforms, gravityMod, speedMod);
        if (whirlpool.isActive()) {
            whirlpool.applyEffects(targetList);
        }
    }

    @Override
    public void performRangedAttack(ArrayList<Characters> targets) {
        if (rangedAttack != null && rangedAttack.canUse() && !stunned && !isAttacking) {
            // Start attack animation instead of launching projectile immediately
            isAttacking = true;
            hasLaunchedProjectile = false;
            attackTargets.clear();
            attackTargets.addAll(targets);
            rangedAttack.startCooldown(); // Start cooldown when attack begins
            animationManager.setAnimationForced("attack1"); // Force reset to restart animation
        }
    }

    @Override
    protected void updateAnimationState() {
        if (isAttacking) {
            // Keep attack animation playing
            currentState = "attack1";
            animationManager.setAnimation(currentState);

            // Get current frame (0-indexed, so frame 4 is index 3)
            int currentFrame = animationManager.getCurrentFrameNumber();

            // Launch projectile on frame 4 (index 3)
            if (currentFrame == 3 && !hasLaunchedProjectile) {
                launchRangedProjectile();
                hasLaunchedProjectile = true;
            }

            // Check if animation is complete
            if (animationManager.isAnimationComplete()) {
                isAttacking = false;
                hasLaunchedProjectile = false;
                attackTargets.clear();
            }
        } else {
            // Default animation state logic
            super.updateAnimationState();
        }
    }

    /**
     * Launch water projectile at targets
     */
    private void launchRangedProjectile() {
        if (waterRanged == null || attackTargets.isEmpty()) return;

        SoundManager.playShot();

        // Launch projectile in facing direction
        int projX = facingRight ? x + width : x - 20;
        int projY = y + height / 2;
        double velX = facingRight ? waterRanged.getSpeed() : -waterRanged.getSpeed();

        waterRanged.spawnProjectile(projX, projY, velX, 0);
    }

    @Override
    public void reset(int newX, int newY) {
        super.reset(newX, newY);
        isAttacking = false;
        hasLaunchedProjectile = false;
        attackTargets.clear();
    }

    /**
     * Custom water projectile that pushes enemies back on hit
     * Demonstrates proper OOP: polymorphism through method overriding
     */
    private class WaterProjectileAttack extends ProjectileAttack {
        private double projectileSpeed;

        public WaterProjectileAttack(int damage, int cooldown, double speed, Color color) {
            super(damage, cooldown, speed, color);
            this.projectileSpeed = speed;
        }

        @Override
        protected void onHit(Characters target, Projectile p) {
            target.pushBack(p.getVelocityX() > 0 ? 5 : -5);
        }

        public double getSpeed() {
            return projectileSpeed;
        }
    }
}
