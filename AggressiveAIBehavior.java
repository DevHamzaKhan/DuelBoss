import java.util.ArrayList;
import java.util.Random;

/**
 * Aggressive AI that pursues the target and attacks frequently
 */
public class AggressiveAIBehavior implements AIBehavior {
    private Random random;
    private double aggroRange;
    private double attackRange;
    private double preferredDistance;
    
    public AggressiveAIBehavior() {
        this(500, 300, 100);
    }
    
    public AggressiveAIBehavior(double aggroRange, double attackRange, double preferredDistance) {
        this.random = new Random();
        this.aggroRange = aggroRange;
        this.attackRange = attackRange;
        this.preferredDistance = preferredDistance;
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
        
        // Horizontal movement - chase aggressively
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
        int distX = Math.abs(target.getX() - boss.getX());
        int distY = Math.abs(target.getY() - boss.getY());
        
        // Ranged attack when at medium distance
        if (distX < attackRange && distY < 100) {
            boss.performRangedAttack(targets);
        }
        
        // Melee attack when close
        if (distX < 100 && distY < 80) {
            boss.performMeleeAttack(targets);
        }
    }
    
    @Override
    public String getName() {
        return "Aggressive";
    }
}
