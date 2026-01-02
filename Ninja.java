import java.awt.*;

/**
 * Ninja class - fast, agile character with quick attacks
 * Demonstrates proper OOP: specialization through inheritance
 */
public class Ninja extends Player {
    public Ninja(int x, int y, int playerNum) {
        super(x, y, playerNum, 100, 6.0, 10.0, new Color(50, 50, 50));
        this.name = "Ninja";
    }

    @Override
    protected void initializeAttacks() {
        // Use the new AttackManager for professional OOP design
        ProjectileAttack ranged = new ProjectileAttack(10, 20, 8.0, Color.DARK_GRAY);
        attackManager.setPrimaryRanged(ranged);

        MeleeAttack melee = new MeleeAttack(15, 15, 40, 10, new Color(100, 100, 100));
        attackManager.setPrimaryMelee(melee);
        
        // Keep backwards compatibility
        rangedAttack = ranged;
        meleeAttack = melee;
    }
}
