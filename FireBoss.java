import java.awt.*;
import java.util.ArrayList;

/**
 * FireBoss - Aggressive fire-themed boss with powerful area attacks
 * Demonstrates proper OOP: inheritance, composition (AttackManager), and polymorphism
 */
public class FireBoss extends Boss {
    private ProjectileAttack homingAttack;
    private boolean isAttacking;
    private boolean hasDealtDamage; // Track if damage dealt on frame 10
    private ArrayList<Characters> attackTargets;

    public FireBoss(int x, int y) {
        super(x, y, 200, 5.0, "Fire Boss", new Color(255, 69, 0));
        // Use custom AI behavior that attacks when target is within 200-pixel area attack radius
        setAIBehavior(new FireBossAIBehavior());
        this.isAttacking = false;
        this.hasDealtDamage = false;
        this.attackTargets = new ArrayList<>();
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

        // Large area melee attack with burning effect
        // 200 radius circular area damage (same as Earth/Ice Boss)
        MeleeAttack melee = new MeleeAttack(30, 60, 200, 20, new Color(255, 100, 0));
        attackManager.setPrimaryMelee(melee);

        homingAttack = new ProjectileAttack(20, 200, 10.0, Color.RED);
        attackManager.registerAttack("homing", homingAttack);

        // Keep backwards compatibility
        rangedAttack = ranged;
        meleeAttack = melee;
    }

    @Override
    protected void initializeSpecialAttack() {
        // No special attack - Fire Boss relies on powerful melee attacks
        specialCooldown = 0;
    }

    @Override
    protected void executeSpecialAttack() {
        // Perform melee attack instead of special attack
        if (meleeAttack != null && meleeAttack.canUse() && !stunned && !isAttacking) {
            performMeleeAttack(targetList);
        }
    }

    @Override
    protected void updateSpecialAttack() {
        // Use melee attack as special attack with longer cooldown
        specialTimer--;
        if (specialTimer <= 0) {
            executeSpecialAttack();
            specialTimer = 120; // Cooldown between special melee attacks
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

            // Get current frame (0-indexed, so frame 10 is index 9)
            int currentFrame = animationManager.getCurrentFrameNumber();

            // Deal damage on frame 10 (index 9)
            if (currentFrame == 9 && !hasDealtDamage) {
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
     * Circular area attack identical to Earth/Ice Boss
     */
    private void dealMeleeDamage() {
        if (meleeAttack == null || attackTargets.isEmpty()) return;

        SoundManager.playAttack();

        // Calculate center of boss
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int radius = 200; // Same radius as Earth/Ice Boss

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
