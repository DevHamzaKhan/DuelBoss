import java.awt.*;
import java.util.ArrayList;

/**
 * WaterBoss - Defensive boss with area control via whirlpool
 * Demonstrates proper OOP: composition, custom attacks, and defensive AI
 */
public class WaterBoss extends Boss {
    private PersistentAreaAttack whirlpool;
    private WaterProjectileAttack waterRanged;

    public WaterBoss(int x, int y) {
        super(x, y, 200, 5.5, "Water Boss", new Color(0, 105, 148));
        // Use defensive AI - water boss prefers zoning
        setAIBehavior(new DefensiveAIBehavior(500, 350, 180, 230));
        initializeAttacks();
        initializeSpecialAttack();
    }

    @Override
    protected void initializeAttacks() {
        waterRanged = new WaterProjectileAttack(10, 45, 7.0, Color.BLUE);
        attackManager.setPrimaryRanged(waterRanged);

        MeleeAttack melee = new MeleeAttack(15, 35, 70, 15, new Color(0, 150, 200));
        attackManager.setPrimaryMelee(melee);
        
        // Keep backwards compatibility
        rangedAttack = waterRanged;
        meleeAttack = melee;
    }

    @Override
    protected void initializeSpecialAttack() {
        whirlpool = new PersistentAreaAttack(5, 250, 180, 100, 30, 2.0, new Color(0, 100, 200));
        attackManager.registerAttack("whirlpool", whirlpool);
        specialAttack = whirlpool;
        specialCooldown = 250;
    }

    @Override
    protected void executeSpecialAttack() {
        if (target != null && whirlpool.canUse()) {
            whirlpool.executeAt(target.getX(), target.getY(), targetList);
        }
    }

    @Override
    public void updateAI(Platform[] platforms, double gravityMod, double speedMod) {
        super.updateAI(platforms, gravityMod, speedMod);
        if (whirlpool.isActive()) {
            whirlpool.applyEffects(targetList);
        }
    }

    /**
     * Custom water projectile that pushes enemies back on hit
     * Demonstrates proper OOP: polymorphism through method overriding
     */
    private class WaterProjectileAttack extends ProjectileAttack {
        public WaterProjectileAttack(int damage, int cooldown, double speed, Color color) {
            super(damage, cooldown, speed, color);
        }

        @Override
        protected void onHit(Characters target, Projectile p) {
            target.pushBack(p.getVelocityX() > 0 ? 5 : -5);
        }
    }
}
