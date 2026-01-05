import java.util.ArrayList;
import java.util.Random;

/**
 * Custom AI behavior for Ice Boss with large area attack range
 */
public class IceBossAIBehavior implements AIBehavior {
    private Random random;
    private double aggroRange;
    private double attackRange;
    private double preferredDistance;

    public IceBossAIBehavior() {
        this.random = new Random();
        this.aggroRange = 500;
        this.attackRange = 300;
        this.preferredDistance = 50; // Get very close to ensure within attack range
    }

    @Override
    public void update(Boss boss, Characters target, ArrayList<Characters> targets,
                      Platform[] platforms, double speedMod) {
        if (boss.isStunned() || target == null) return;

        updateMovement(boss, target, speedMod);
        updateCombat(boss, target, targets);
    }

    private void updateMovement(Boss boss, Characters target, double speedMod) {
        int distX = target.getX() - boss.getX();
        int distY = target.getY() - boss.getY();

        // Horizontal movement - maintain preferred distance
        if (Math.abs(distX) > preferredDistance) {
            if (distX > 0) {
                boss.moveRight(speedMod);
            } else {
                boss.moveLeft(speedMod);
            }
        } else {
            boss.stopMoving();
            boss.setFacingDirection(distX > 0);
        }

        // Vertical movement - jump to reach higher targets
        if (distY < -50 && boss.isOnGround() && random.nextInt(100) < 8) {
            boss.jump();
        }

        // Drop down to reach lower targets
        if (distY > 100 && boss.isOnGround() && random.nextInt(100) < 5) {
            boss.dropThroughPlatform();
        }
    }

    private void updateCombat(Boss boss, Characters target, ArrayList<Characters> targets) {
        // Calculate actual distance from boss center to target center
        int centerX = boss.getX() + boss.getWidth() / 2;
        int centerY = boss.getY() + boss.getHeight() / 2;
        int tx = target.getX() + target.getWidth() / 2;
        int ty = target.getY() + target.getHeight() / 2;
        double actualDist = Math.sqrt(Math.pow(tx - centerX, 2) + Math.pow(ty - centerY, 2));

        // Melee attack when target is within 200-pixel radius (area attack range)
        if (actualDist <= 200) {
            boss.performMeleeAttack(targets);
        }

        // Ranged attack disabled (Ice Boss uses melee only)
        // if (distX < attackRange && distY < 100 && actualDist > 200) {
        //     boss.performRangedAttack(targets);
        // }
    }

    @Override
    public String getName() {
        return "Ice Boss";
    }
}
