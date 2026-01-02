import java.awt.*;

/**
 * Archer class - fast, ranged-focused character
 * Demonstrates proper OOP: specialization through inheritance
 */
public class Archer extends Player {
    public Archer(int x, int y, int playerNum) {
        super(x, y, playerNum, 100, 4.5, 10.0, new Color(34, 139, 34));
        this.name = "Archer";
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
