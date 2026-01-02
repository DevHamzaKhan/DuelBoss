import java.awt.*;
import java.util.ArrayList;

/**
 * FireBoss - Aggressive fire-themed boss with homing projectiles
 * Demonstrates proper OOP: inheritance, composition (AttackManager), and polymorphism
 */
public class FireBoss extends Boss {
    private ProjectileAttack homingAttack;

    public FireBoss(int x, int y) {
        super(x, y, 200, 5.0, "Fire Boss", new Color(255, 69, 0));
        // Use aggressive AI behavior - suits fire element
        setAIBehavior(new AggressiveAIBehavior(500, 300, 100));
        initializeAttacks();
        initializeSpecialAttack();
    }

    @Override
    protected void initializeAttacks() {
        // Use the new AttackManager for professional OOP design
        ProjectileAttack ranged = new ProjectileAttack(15, 50, 7.0, Color.ORANGE);
        attackManager.setPrimaryRanged(ranged);

        MeleeAttack melee = new MeleeAttack(25, 40, 55, 18, new Color(255, 100, 0));
        attackManager.setPrimaryMelee(melee);

        homingAttack = new ProjectileAttack(20, 200, 10.0, Color.RED);
        attackManager.registerAttack("homing", homingAttack);
        
        // Keep backwards compatibility
        rangedAttack = ranged;
        meleeAttack = melee;
    }

    @Override
    protected void initializeSpecialAttack() {
        specialAttack = new SpecialAttack(20, 200, 1) {
            @Override
            public void execute(ArrayList<Characters> targets) {
                if (!canUse() || targets.isEmpty()) return;
                activate();

                Characters t = targets.get(0);
                double dx = t.getX() - x;
                double dy = t.getY() - y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist > 0) {
                    dx = (dx / dist) * 10;
                    dy = (dy / dist) * 10;
                    homingAttack.spawnProjectile(x + width / 2, y + height / 2, dx, dy);
                }
            }

            @Override
            public void draw(Graphics2D g) {
                homingAttack.draw(g);
            }
        };
        specialAttack.setOwner(this);
        specialCooldown = 200;
    }

    @Override
    protected void updateSpecialAttack() {
        super.updateSpecialAttack();
        homingAttack.update();
    }

    @Override
    public void checkAttackCollisions(ArrayList<Characters> targets) {
        super.checkAttackCollisions(targets);
        homingAttack.checkCollisions(targets);
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);
        homingAttack.draw(g);
    }
}
