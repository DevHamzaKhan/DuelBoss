import java.awt.*;
import java.util.ArrayList;

/**
 * Archer class - fast, ranged-focused character
 * Demonstrates proper OOP: specialization through inheritance
 */
public class Archer extends Player {
    private boolean isMeleeAttacking;
    private boolean isRangedAttacking;
    private boolean hasDoneAction; // Track if action (damage/projectile) has been done
    private ArrayList<Characters> attackTargets;
    private double projectileSpeed;

    public Archer(int x, int y, int playerNum) {
        super(x, y, playerNum, 100, 4.5, 10.0, new Color(34, 139, 34));
        this.name = "Archer";
        this.isMeleeAttacking = false;
        this.isRangedAttacking = false;
        this.hasDoneAction = false;
        this.attackTargets = new ArrayList<>();
        this.projectileSpeed = 12.0;
        loadSprites();
    }
    
    /**
     * Load all Archer sprite animations
     */
    private void loadSprites() {
        String basePath = "Archer/animations/PNG/";
        animationManager.loadAnimation("idle", basePath + "idle/", 12, 3);
        animationManager.loadAnimation("run", basePath + "run/", 10, 2);
        animationManager.loadAnimation("jump_up", basePath + "jump_up/jump_up_", 3, 2);
        animationManager.loadAnimation("jump_down", basePath + "jump_down/jump_down_", 3, 2);
        animationManager.loadAnimation("attack1", basePath + "1_atk/1_atk_", 10, 2);
        animationManager.loadAnimation("attack2", basePath + "2_atk/2_atk_", 15, 2);
        animationManager.loadAnimation("defend", basePath + "defend/", 4, 3);
        animationManager.loadAnimation("take_hit", basePath + "take_hit/take_hit_", 4, 2);
        animationManager.loadAnimation("death", basePath + "death/", 19, 3);
        animationManager.setAnimation("idle");
    }

    @Override
    protected void initializeAttacks() {
        // Use the new AttackManager for professional OOP design
        ProjectileAttack ranged = new ProjectileAttack(20, 50, 12.0, new Color(139, 69, 19));
        attackManager.setPrimaryRanged(ranged);

        MeleeAttack melee = new MeleeAttack(10, 40, 35, 12, new Color(34, 139, 34, 150));
        attackManager.setPrimaryMelee(melee);

        // Keep backwards compatibility
        rangedAttack = ranged;
        meleeAttack = melee;
    }

    @Override
    public void performMeleeAttack(ArrayList<Characters> targets) {
        if (meleeAttack != null && meleeAttack.canUse() && !stunned && !isMeleeAttacking && !isRangedAttacking) {
            // Start melee attack animation (attack1)
            isMeleeAttacking = true;
            hasDoneAction = false;
            attackTargets.clear();
            attackTargets.addAll(targets);
            meleeAttack.startCooldown();
            animationManager.setAnimationForced("attack1");
        }
    }

    @Override
    public void performRangedAttack(ArrayList<Characters> targets) {
        if (rangedAttack != null && rangedAttack.canUse() && !stunned && !isMeleeAttacking && !isRangedAttacking) {
            // Start ranged attack animation (attack2)
            isRangedAttacking = true;
            hasDoneAction = false;
            attackTargets.clear();
            attackTargets.addAll(targets);
            rangedAttack.startCooldown();
            animationManager.setAnimationForced("attack2");
        }
    }

    @Override
    protected void updateAnimationState() {
        if (isMeleeAttacking) {
            // Keep attack1 animation playing
            currentState = "attack1";
            animationManager.setAnimation(currentState);

            // Deal melee damage on frame 6 (index 5)
            int currentFrame = animationManager.getCurrentFrameNumber();
            if (currentFrame == 5 && !hasDoneAction) {
                dealMeleeDamage();
                hasDoneAction = true;
            }

            // Check if animation is complete
            if (animationManager.isAnimationComplete()) {
                isMeleeAttacking = false;
                hasDoneAction = false;
                attackTargets.clear();
            }
        } else if (isRangedAttacking) {
            // Keep attack2 animation playing
            currentState = "attack2";
            animationManager.setAnimation(currentState);

            // Launch projectile on frame 9 (index 8)
            int currentFrame = animationManager.getCurrentFrameNumber();
            if (currentFrame == 8 && !hasDoneAction) {
                launchRangedProjectile();
                hasDoneAction = true;
            }

            // Check if animation is complete
            if (animationManager.isAnimationComplete()) {
                isRangedAttacking = false;
                hasDoneAction = false;
                attackTargets.clear();
            }
        } else {
            // Default animation state logic
            super.updateAnimationState();
        }
    }

    /**
     * Deal melee damage to all targets in range
     */
    private void dealMeleeDamage() {
        if (meleeAttack == null || attackTargets.isEmpty()) return;

        SoundManager.playAttack();

        for (Characters target : attackTargets) {
            if (target != this) {
                int distX = Math.abs(target.getX() - x);
                int distY = Math.abs(target.getY() - y);

                if (distX < meleeAttack.getRange() && distY < 60) {
                    target.takeDamage(meleeAttack.getDamage());
                }
            }
        }
    }

    /**
     * Launch projectile at targets
     */
    private void launchRangedProjectile() {
        if (rangedAttack == null || attackTargets.isEmpty()) return;

        SoundManager.playShot();

        // Launch projectile in facing direction
        int projX = facingRight ? x + width : x - 20;
        int projY = y + height / 2;
        double velX = facingRight ? projectileSpeed : -projectileSpeed;

        ((ProjectileAttack)rangedAttack).spawnProjectile(projX, projY, velX, 0);
    }

    @Override
    public void reset(int newX, int newY) {
        super.reset(newX, newY);
        isMeleeAttacking = false;
        isRangedAttacking = false;
        hasDoneAction = false;
        attackTargets.clear();
    }
}
