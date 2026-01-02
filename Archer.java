import java.awt.*;

/**
 * Archer class - fast, ranged-focused character
 * Demonstrates proper OOP: specialization through inheritance
 */
public class Archer extends Player {
    public Archer(int x, int y, int playerNum) {
        super(x, y, playerNum, 100, 4.5, 10.0, new Color(34, 139, 34));
        this.name = "Archer";
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
}
