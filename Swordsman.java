import java.awt.*;
import java.util.ArrayList;

public class Swordsman extends Player {
    private boolean isAttacking;
    private boolean hasDoneMelee; // Track if melee damage has been done
    private boolean hasDoneRanged; // Track if projectile has been launched
    private ArrayList<Characters> attackTargets;
    private boolean isMeleeAtk; // Track if this attack is a melee attack
    private boolean isRangedAtk; // Track if this attack is a ranged attack
    private double projectileSpeed;

    public Swordsman(int x, int y, int playerNum) {
        super(x, y, playerNum, 100, 3.0, 10.0, new Color(139, 0, 0));
        this.name = "Swordsman";
        this.isAttacking = false;
        this.hasDoneMelee = false;
        this.hasDoneRanged = false;
        this.attackTargets = new ArrayList<>();
        this.isMeleeAtk = false;
        this.isRangedAtk = false;
        this.projectileSpeed = 6.0;
        loadSprites();
    }

    private void loadSprites() {
        String basePath = "Swordsman/PNG/";
        animationManager.loadAnimation("idle", basePath + "idle/", 8, 3);
        animationManager.loadAnimation("run", basePath + "run/", 8, 2);
        animationManager.loadAnimation("jump_up", basePath + "j_up/j_up_", 3, 2);
        animationManager.loadAnimation("jump_down", basePath + "j_down/j_down_", 3, 2);
        animationManager.loadAnimation("attack1", basePath + "1_atk/1_atk_", 8, 2);
        animationManager.loadAnimation("attack2", basePath + "2_atk/2_atk_", 6, 2);
        animationManager.loadAnimation("attack3", basePath + "3_atk/3_atk_", 8, 2);
        animationManager.loadAnimation("defend", basePath + "defend/", 4, 3);
        animationManager.loadAnimation("take_hit", basePath + "take_hit/take_hit_", 3, 2);
        animationManager.loadAnimation("death", basePath + "death/", 10, 3);
        animationManager.setAnimation("idle");
    }

    @Override
    protected void initializeAttacks() {
        ProjectileAttack ranged = new ProjectileAttack(15, 40, 6.0, Color.ORANGE);
        attackManager.setPrimaryRanged(ranged);
        MeleeAttack melee = new MeleeAttack(25, 30, 50, 15, new Color(255, 215, 0));
        attackManager.setPrimaryMelee(melee);
        rangedAttack = ranged;
        meleeAttack = melee;
    }

    @Override
    public void performMeleeAttack(ArrayList<Characters> targets) {
        if (meleeAttack != null && meleeAttack.canUse() && !stunned && !isAttacking) {
            // Start attack animation (attack1) - melee damage on frame 1
            isAttacking = true;
            isMeleeAtk = true;
            isRangedAtk = false;
            hasDoneMelee = false;
            hasDoneRanged = false;
            attackTargets.clear();
            attackTargets.addAll(targets);
            meleeAttack.startCooldown();
            animationManager.setAnimationForced("attack1");
        }
    }

    @Override
    public void performRangedAttack(ArrayList<Characters> targets) {
        if (rangedAttack != null && rangedAttack.canUse() && !stunned && !isAttacking) {
            // Start attack animation (attack1) - projectile on frame 1
            isAttacking = true;
            isMeleeAtk = false;
            isRangedAtk = true;
            hasDoneMelee = false;
            hasDoneRanged = false;
            attackTargets.clear();
            attackTargets.addAll(targets);
            rangedAttack.startCooldown();
            animationManager.setAnimationForced("attack1");
        }
    }

    @Override
    protected void updateAnimationState() {
        if (isAttacking) {
            // Keep attack1 animation playing
            currentState = "attack1";
            animationManager.setAnimation(currentState);

            // Deal melee damage AND launch projectile on frame 1 (index 0)
            int currentFrame = animationManager.getCurrentFrameNumber();
            if (currentFrame == 0) {
                if (isMeleeAtk && !hasDoneMelee) {
                    dealMeleeDamage();
                    hasDoneMelee = true;
                }
                if (isRangedAtk && !hasDoneRanged) {
                    launchRangedProjectile();
                    hasDoneRanged = true;
                }
            }

            // Check if animation is complete
            if (animationManager.isAnimationComplete()) {
                isAttacking = false;
                isMeleeAtk = false;
                isRangedAtk = false;
                hasDoneMelee = false;
                hasDoneRanged = false;
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
        isAttacking = false;
        isMeleeAtk = false;
        isRangedAtk = false;
        hasDoneMelee = false;
        hasDoneRanged = false;
        attackTargets.clear();
    }
}
