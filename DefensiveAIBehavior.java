import java.util.ArrayList;
import java.util.Random;

/**
 * Defensive AI that maintains distance and prefers ranged combat
 */
public class DefensiveAIBehavior implements AIBehavior {
    private Random random;
    private double aggroRange;
    private double attackRange;
    private double minDistance;
    private double maxDistance;
    
    public DefensiveAIBehavior() {
        this(500, 350, 200, 250);
    }
    
    public DefensiveAIBehavior(double aggroRange, double attackRange, 
                              double minDistance, double maxDistance) {
        this.random = new Random();
        this.aggroRange = aggroRange;
        this.attackRange = attackRange;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
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
        double distance = Math.sqrt(distX * distX + distY * distY);
        
        // Maintain optimal distance
        if (distance < minDistance) {
            // Too close, back away (move in SAME direction as distX to increase distance)
            if (distX > 0) {
                boss.moveRight(speedMod);  // Target is right, move right to back away
            } else {
                boss.moveLeft(speedMod);   // Target is left, move left to back away
            }
        } else if (distance > maxDistance) {
            // Too far, move closer (move in OPPOSITE direction to decrease distance)
            if (distX > 0) {
                boss.moveRight(speedMod);  // Target is right, move right to get closer
            } else {
                boss.moveLeft(speedMod);   // Target is left, move left to get closer
            }
        } else {
            // Good distance, stop and face target
            boss.stopMoving();
            boss.setFacingDirection(distX > 0);
        }
        
        // Vertical movement - less aggressive
        if (distY < -80 && boss.isOnGround() && random.nextInt(100) < 3) {
            boss.jump();
        }
        
        if (distY > 120 && boss.isOnGround() && random.nextInt(100) < 2) {
            boss.dropThroughPlatform();
        }
    }
    
    private void updateCombat(Boss boss, Characters target, ArrayList<Characters> targets) {
        int distX = Math.abs(target.getX() - boss.getX());
        int distY = Math.abs(target.getY() - boss.getY());
        
        // Prefer ranged attacks
        if (distX < attackRange && distY < 120) {
            boss.performRangedAttack(targets);
        }
        
        // Only melee if target gets very close
        if (distX < 80 && distY < 60) {
            boss.performMeleeAttack(targets);
        }
    }
    
    @Override
    public String getName() {
        return "Defensive";
    }
}
