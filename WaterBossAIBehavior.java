import java.util.ArrayList;
import java.util.Random;

/**
 * Custom AI behavior for Water Boss - defensive zoner
 * Maintains maximum distance while trying to stay on same vertical level as player
 */
public class WaterBossAIBehavior implements AIBehavior {
    private Random random;

    public WaterBossAIBehavior() {
        this.random = new Random();
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

        // Horizontal movement - ALWAYS try to move away from player (maximize distance)
        // Move in opposite direction of target
        if (distX > 0) {
            boss.moveLeft(speedMod);  // Target is right, move left to maximize distance
        } else {
            boss.moveRight(speedMod); // Target is left, move right to maximize distance
        }

        // Always face the target even while retreating
        boss.setFacingDirection(distX > 0);

        // Vertical movement - aggressively try to match player's level
        if (distY < -50 && boss.isOnGround() && random.nextInt(100) < 15) {
            boss.jump(); // Jump more frequently to reach player level
        }

        if (distY > 80 && boss.isOnGround() && random.nextInt(100) < 12) {
            boss.dropThroughPlatform(); // Drop more frequently to reach player level
        }
    }

    private void updateCombat(Boss boss, Characters target, ArrayList<Characters> targets) {
        // Always shoot at the player - infinite range zoner
        boss.performRangedAttack(targets);

        // No melee attacks - Water Boss avoids close combat entirely
    }

    @Override
    public String getName() {
        return "Water Boss";
    }
}
