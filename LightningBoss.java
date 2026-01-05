import java.awt.*;
import java.util.ArrayList;

/**
 * LightningBoss - Fast, aggressive boss with area denial lightning strikes
 * Demonstrates proper OOP: composition with multiple attack types
 */
public class LightningBoss extends Boss {
    private ArrayList<TargetedStrikeAttack> lightningStrikes;
    private boolean isAttacking;
    private boolean hasDealtDamage; // Track if damage dealt on frame 4
    private ArrayList<Characters> attackTargets;

    public LightningBoss(int x, int y) {
        super(x, y, 80, 96, 150, 7.0, "Lightning Boss", new Color(255, 255, 0));
        lightningStrikes = new ArrayList<>();
        this.isAttacking = false;
        this.hasDealtDamage = false;
        this.attackTargets = new ArrayList<>();
        // Use custom aggressive melee AI
        setAIBehavior(new LightningBossAIBehavior());
        loadSprites();
        initializeAttacks();
        initializeSpecialAttack();
    }
    
    /**
     * Load all Lightning Boss sprite animations (Crystal Mauler)
     */
    private void loadSprites() {
        String basePath = "LightningBoss/animations/PNG/";
        
        // Load idle animation (8 frames)
        animationManager.loadAnimation("idle", basePath + "idle/idle_", 8, 4);
        
        // Load run animation (8 frames)
        animationManager.loadAnimation("run", basePath + "run/run_", 8, 2);
        
        // Load jump up animation (3 frames)
        animationManager.loadAnimation("jump_up", basePath + "j_up/j_up_", 3, 2);
        
        // Load jump down animation (3 frames)
        animationManager.loadAnimation("jump_down", basePath + "j_down/j_down_", 3, 2);
        
        // Load attack animations
        animationManager.loadAnimation("attack1", basePath + "1_atk/1_atk_", 7, 2);
        animationManager.loadAnimation("attack2", basePath + "2_atk/2_atk_", 6, 2);
        animationManager.loadAnimation("attack3", basePath + "3_atk/3_atk_", 8, 2);
        
        // Load take hit animation (6 frames)
        animationManager.loadAnimation("take_hit", basePath + "take_hit/take_hit_", 6, 3);
        
        // Load death animation (15 frames)
        animationManager.loadAnimation("death", basePath + "death/death_", 15, 3);
        
        // Set default animation
        animationManager.setAnimation("idle");
    }

    @Override
    protected void initializeAttacks() {
        ProjectileAttack ranged = new ProjectileAttack(18, 30, 10.5, Color.YELLOW);
        attackManager.setPrimaryRanged(ranged);

        // Large area melee attack (200 radius circular area damage like Fire/Earth/Ice Boss)
        MeleeAttack melee = new MeleeAttack(25, 35, 200, 15, new Color(255, 255, 100));
        attackManager.setPrimaryMelee(melee);

        // Keep backwards compatibility
        rangedAttack = ranged;
        meleeAttack = melee;
    }

    @Override
    protected void initializeSpecialAttack() {
        specialCooldown = 180;
    }

    @Override
    protected void executeSpecialAttack() {
        lightningStrikes.clear();
        for (int i = 0; i < 5; i++) {
            int randX = random.nextInt(Main.WIDTH - 40) + 20;
            TargetedStrikeAttack strike = new TargetedStrikeAttack(15, 0, 30, 10, 40, Color.YELLOW);
            strike.setOwner(this);
            strike.executeAtPosition(randX);
            lightningStrikes.add(strike);
        }
    }

    @Override
    protected void updateSpecialAttack() {
        specialTimer--;
        if (specialTimer <= 0) {
            executeSpecialAttack();
            specialTimer = specialCooldown;
        }

        for (TargetedStrikeAttack strike : lightningStrikes) {
            strike.update();
            if (strike.getPhase() == TargetedStrikeAttack.PHASE_STRIKE) {
                strike.checkHits(targetList);
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);
        for (TargetedStrikeAttack strike : lightningStrikes) {
            strike.draw(g);
        }
    }

    @Override
    public void performMeleeAttack(ArrayList<Characters> targets) {
        if (meleeAttack != null && meleeAttack.canUse() && !stunned && !isAttacking) {
            // Start attack animation instead of dealing damage immediately
            isAttacking = true;
            hasDealtDamage = false;
            attackTargets.clear();
            attackTargets.addAll(targets);
            meleeAttack.startCooldown(); // Start cooldown when attack begins
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

            // Deal damage on frame 4 (index 3)
            if (currentFrame == 3 && !hasDealtDamage) {
                dealMeleeDamage();
                hasDealtDamage = true;
            }

            // Check if animation is complete
            if (animationManager.isAnimationComplete()) {
                isAttacking = false;
                hasDealtDamage = false;
                attackTargets.clear();
            }
        } else {
            // Default animation state logic
            super.updateAnimationState();
        }
    }

    /**
     * Deal melee damage to all targets in range
     * Circular area attack identical to Fire/Earth/Ice Boss
     */
    private void dealMeleeDamage() {
        if (meleeAttack == null || attackTargets.isEmpty()) return;

        // Calculate center of boss
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int radius = 200; // Same radius as other melee bosses

        // Deal damage to all targets within circular radius
        for (Characters target : attackTargets) {
            if (target != this) {
                // Calculate center of target
                int tx = target.getX() + target.getWidth() / 2;
                int ty = target.getY() + target.getHeight() / 2;

                // Calculate distance from boss center to target center
                double dist = Math.sqrt(Math.pow(tx - centerX, 2) + Math.pow(ty - centerY, 2));

                // Deal damage if within radius
                if (dist <= radius) {
                    target.takeDamage(meleeAttack.getDamage());
                }
            }
        }
    }

    @Override
    public void reset(int newX, int newY) {
        super.reset(newX, newY);
        isAttacking = false;
        hasDealtDamage = false;
        attackTargets.clear();
    }
}
