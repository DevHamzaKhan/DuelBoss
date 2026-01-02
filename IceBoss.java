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
