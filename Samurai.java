import java.awt.*;

/**
 * Samurai class - balanced melee-focused character
 * Demonstrates proper OOP: specialization through inheritance
 */
public class Samurai extends Player {
    public Samurai(int x, int y, int playerNum) {
        super(x, y, playerNum, 100, 3.0, 10.0, new Color(139, 0, 0));
        this.name = "Samurai";
    }

    @Override
    protected void initializeAttacks() {
        // Use the new AttackManager for professional OOP design
        ProjectileAttack ranged = new ProjectileAttack(15, 40, 6.0, Color.ORANGE);
        attackManager.setPrimaryRanged(ranged);

        MeleeAttack melee = new MeleeAttack(25, 30, 50, 15, new Color(255, 215, 0));
        attackManager.setPrimaryMelee(melee);
        
        // Keep backwards compatibility
        rangedAttack = ranged;
        meleeAttack = melee;
    }
}
