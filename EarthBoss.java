import java.awt.*;
import java.util.ArrayList;

/**
 * EarthBoss - Defensive tank with powerful melee area attacks
 * Demonstrates proper OOP: uses DefensiveAIBehavior, composition, and custom
 * attacks
 */
public class EarthBoss extends Boss {
    private boolean isAttacking;
    private boolean hasDealtDamageFrame1; // Track if damage dealt on frame 5
    private boolean hasDealtDamageFrame2; // Track if damage dealt on frame 11
    private ArrayList<Characters> attackTargets;

    public EarthBoss(int x, int y) {
        super(x, y, 400, 2.0, "Earth Boss", new Color(139, 90, 43));
        // Use custom AI behavior that attacks when target is within 200-pixel area attack radius
        setAIBehavior(new EarthBossAIBehavior());
        this.isAttacking = false;
        this.hasDealtDamageFrame1 = false;
        this.hasDealtDamageFrame2 = false;
        this.attackTargets = new ArrayList<>();
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
            animationManager.drawWithRatio(g, x, y, width, height, !facingRight, 12.0 / 36.0, 0.6, 0.75);
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
                if (!canUse() || owner == null)
                    return;

                int projX = owner.isFacingRight() ? owner.getX() + owner.getWidth() : owner.getX() - 20;
                int projY = owner.getY() + owner.getHeight() / 2;
                double velX = owner.isFacingRight() ? 5.0 : -5.0;
                spawnProjectile(projX, projY, velX, -2);
                startCooldown();
            }
        };
        attackManager.setPrimaryRanged(ranged);

        // Large area melee attack (replaces earthquake special attack)
        // 200 radius circular area damage (same as old earthquake)
        MeleeAttack melee = new MeleeAttack(30, 60, 200, 25, new Color(101, 67, 33));
        attackManager.setPrimaryMelee(melee);

        // Keep backwards compatibility
        rangedAttack = ranged;
        meleeAttack = melee;
    }

    @Override
    protected void initializeSpecialAttack() {
        // No special attack - Earth Boss relies on powerful melee attacks
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
            hasDealtDamageFrame1 = false;
            hasDealtDamageFrame2 = false;
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

            // Get current frame (0-indexed, so frame 5 is index 4, frame 11 is index 10)
            int currentFrame = animationManager.getCurrentFrameNumber();

            // Deal damage on frame 5 (index 4)
            if (currentFrame == 4 && !hasDealtDamageFrame1) {
                dealMeleeDamage();
                hasDealtDamageFrame1 = true;
            }

            // Deal damage on frame 11 (index 10)
            if (currentFrame == 10 && !hasDealtDamageFrame2) {
                dealMeleeDamage();
                hasDealtDamageFrame2 = true;
            }

            // Check if animation is complete
            if (animationManager.isAnimationComplete()) {
                isAttacking = false;
                hasDealtDamageFrame1 = false;
                hasDealtDamageFrame2 = false;
                attackTargets.clear();
            }
        } else {
            // Default animation state logic
            super.updateAnimationState();
        }
    }

    /**
     * Deal melee damage to all targets in range
     * Circular area attack identical to the old earthquake
     */
    private void dealMeleeDamage() {
        if (meleeAttack == null || attackTargets.isEmpty()) return;

        SoundManager.playAttack();

        // Calculate center of boss
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int radius = 200; // Same radius as old earthquake

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
        hasDealtDamageFrame1 = false;
        hasDealtDamageFrame2 = false;
        attackTargets.clear();
    }
}
