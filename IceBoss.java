import java.awt.*;
import java.util.ArrayList;

/**
 * IceBoss - Balanced boss with freezing/stunning area attacks
 * Demonstrates proper OOP: custom attack subclass with special behavior
 */
public class IceBoss extends Boss {
    private IceProjectileAttack iceRanged;
    private boolean isAttacking;
    private boolean hasDealtDamage; // Track if damage dealt on frame 7
    private ArrayList<Characters> attackTargets;

    public IceBoss(int x, int y) {
        super(x, y, 200, 4.0, "Ice Boss", new Color(135, 206, 250));
        // Use custom AI behavior that attacks when target is within 200-pixel area attack radius
        setAIBehavior(new IceBossAIBehavior());
        this.isAttacking = false;
        this.hasDealtDamage = false;
        this.attackTargets = new ArrayList<>();
        loadSprites();
        initializeAttacks();
        initializeSpecialAttack();
    }

    /**
     * Load all Ice Boss sprite animations (Frost Guardian)
     */
    private void loadSprites() {
        String basePath = "IceBoss/PNG files/";

        // Load idle animation (6 frames)
        animationManager.loadAnimation("idle", basePath + "idle/idle_", 6, 4);

        // Load walk animation (10 frames)
        animationManager.loadAnimation("run", basePath + "walk/walk_", 10, 3);

        // Load attack animation (14 frames)
        animationManager.loadAnimation("attack1", basePath + "1_atk/1_atk_", 14, 2);

        // Load take hit animation (7 frames)
        animationManager.loadAnimation("take_hit", basePath + "take_hit/take_hit_", 7, 3);

        // Load death animation (16 frames)
        animationManager.loadAnimation("death", basePath + "death/death_", 16, 3);

        // Set default animation
        animationManager.setAnimation("idle");
    }

    @Override
    protected void initializeAttacks() {
        iceRanged = new IceProjectileAttack(12, 60, 8.0, Color.CYAN);
        attackManager.setPrimaryRanged(iceRanged);

        // Large area melee attack with freezing effect
        // 200 radius circular area damage (same as Earth Boss)
        MeleeAttack melee = new MeleeAttack(25, 60, 200, 20, new Color(173, 216, 230));
        attackManager.setPrimaryMelee(melee);

        // Keep backwards compatibility
        rangedAttack = iceRanged;
        meleeAttack = melee;
    }

    @Override
    protected void initializeSpecialAttack() {
        // No special attack - Ice Boss relies on powerful freezing melee attacks
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
    protected void drawCharacter(Graphics2D g) {
        String animToUse = currentState;
        if (!animationManager.hasAnimation(currentState)) {
            animToUse = "idle";
        }
        if (animationManager.hasAnimation(animToUse)) {
            animationManager.setAnimation(animToUse);
            // Ice boss sprites are facing opposite direction, so invert facingRight
            animationManager.drawWithRatio(g, x, y, width, height, !facingRight, 12.0 / 36.0, 0.6, 0.6);
        } else if (sprite != null) {
            if (facingRight) {
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
            if (facingRight) {
                g.fillOval(x + width - eyeSize - 5, eyeY, eyeSize, eyeSize);
            } else {
                g.fillOval(x + 5, eyeY, eyeSize, eyeSize);
            }
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

            // Get current frame (0-indexed, so frame 7 is index 6)
            int currentFrame = animationManager.getCurrentFrameNumber();

            // Deal damage on frame 7 (index 6)
            if (currentFrame == 6 && !hasDealtDamage) {
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
     * Deal melee damage to all targets in range with freezing effect
     * Circular area attack identical to Earth Boss
     */
    private void dealMeleeDamage() {
        if (meleeAttack == null || attackTargets.isEmpty()) return;

        // Calculate center of boss
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int radius = 200; // Same radius as Earth Boss

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
                    // Add freezing effect - stun for a short duration
                    target.stun(30);
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

    /**
     * Custom ice projectile that stuns on hit
     * Demonstrates proper OOP: extension of base class with additional behavior
     */
    private class IceProjectileAttack extends ProjectileAttack {
        public IceProjectileAttack(int damage, int cooldown, double speed, Color color) {
            super(damage, cooldown, speed, color);
        }

        @Override
        protected void onHit(Characters target, Projectile p) {
            target.stun(30);
        }
    }
}
