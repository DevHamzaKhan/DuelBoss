import java.awt.*;
import java.util.ArrayList;

/**
 * IceBoss - Balanced boss with freezing/stunning attacks
 * Demonstrates proper OOP: custom attack subclass with special behavior
 */
public class IceBoss extends Boss {
    private IceProjectileAttack iceRanged;

    public IceBoss(int x, int y) {
        super(x, y, 200, 4.0, "Ice Boss", new Color(135, 206, 250));
        // Use aggressive AI - ice boss is moderately aggressive
        setAIBehavior(new AggressiveAIBehavior(500, 300, 150));
        initializeAttacks();
        initializeSpecialAttack();
    }

    @Override
    protected void initializeAttacks() {
        iceRanged = new IceProjectileAttack(12, 60, 8.0, Color.CYAN);
        attackManager.setPrimaryRanged(iceRanged);

        MeleeAttack melee = new MeleeAttack(18, 50, 60, 20, new Color(173, 216, 230));
        attackManager.setPrimaryMelee(melee);
        
        // Keep backwards compatibility
        rangedAttack = iceRanged;
        meleeAttack = melee;
    }

    @Override
    protected void initializeSpecialAttack() {
        specialAttack = new GlobalAttack(0, 300, 180, GlobalAttack.EFFECT_STUN, Color.CYAN);
        attackManager.setSpecialAttack(specialAttack);
        specialCooldown = 300;
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
