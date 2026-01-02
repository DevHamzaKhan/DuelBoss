import java.awt.*;
import java.util.ArrayList;

/**
 * LightningBoss - Fast, aggressive boss with area denial lightning strikes
 * Demonstrates proper OOP: composition with multiple attack types
 */
public class LightningBoss extends Boss {
    private ArrayList<TargetedStrikeAttack> lightningStrikes;

    public LightningBoss(int x, int y) {
        super(x, y, 150, 7.0, "Lightning Boss", new Color(255, 255, 0));
        lightningStrikes = new ArrayList<>();
        // Very aggressive AI - suits fast lightning element
        setAIBehavior(new AggressiveAIBehavior(600, 350, 120));
        loadSprites();
        initializeAttacks();
        initializeSpecialAttack();
    }
    
    /**
     * Load all Lightning Boss sprite animations (Crystal Mauler)
     */
    private void loadSprites() {
        String basePath = "LightningBoss/animations/PNG/";
        
        // Load idle animation (8 frames)
        animationManager.loadAnimation("idle", basePath + "idle/idle_", 8, 4);
        
        // Load run animation (8 frames)
        animationManager.loadAnimation("run", basePath + "run/run_", 8, 2);
        
        // Load jump up animation (3 frames)
        animationManager.loadAnimation("jump_up", basePath + "j_up/j_up_", 3, 2);
        
        // Load jump down animation (3 frames)
        animationManager.loadAnimation("jump_down", basePath + "j_down/j_down_", 3, 2);
        
        // Load attack animations
        animationManager.loadAnimation("attack1", basePath + "1_atk/1_atk_", 7, 2);
        animationManager.loadAnimation("attack2", basePath + "2_atk/2_atk_", 6, 2);
        animationManager.loadAnimation("attack3", basePath + "3_atk/3_atk_", 8, 2);
        
        // Load take hit animation (6 frames)
        animationManager.loadAnimation("take_hit", basePath + "take_hit/take_hit_", 6, 3);
        
        // Load death animation (15 frames)
        animationManager.loadAnimation("death", basePath + "death/death_", 15, 3);
        
        // Set default animation
        animationManager.setAnimation("idle");
    }

    @Override
    protected void initializeAttacks() {
        ProjectileAttack ranged = new ProjectileAttack(18, 30, 10.5, Color.YELLOW);
        attackManager.setPrimaryRanged(ranged);

        MeleeAttack melee = new MeleeAttack(12, 20, 45, 8, new Color(255, 255, 100));
        attackManager.setPrimaryMelee(melee);
        
        // Keep backwards compatibility
        rangedAttack = ranged;
        meleeAttack = melee;
    }

    @Override
    protected void initializeSpecialAttack() {
        specialCooldown = 180;
    }

    @Override
    protected void executeSpecialAttack() {
        lightningStrikes.clear();
        for (int i = 0; i < 5; i++) {
            int randX = random.nextInt(Main.WIDTH - 40) + 20;
            TargetedStrikeAttack strike = new TargetedStrikeAttack(15, 0, 30, 10, 40, Color.YELLOW);
            strike.setOwner(this);
            strike.executeAtPosition(randX);
            lightningStrikes.add(strike);
        }
    }

    @Override
    protected void updateSpecialAttack() {
        specialTimer--;
        if (specialTimer <= 0) {
            executeSpecialAttack();
            specialTimer = specialCooldown;
        }

        for (TargetedStrikeAttack strike : lightningStrikes) {
            strike.update();
            if (strike.getPhase() == TargetedStrikeAttack.PHASE_STRIKE) {
                strike.checkHits(targetList);
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);
        for (TargetedStrikeAttack strike : lightningStrikes) {
            strike.draw(g);
        }
    }
}
