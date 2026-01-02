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
        initializeAttacks();
        initializeSpecialAttack();
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
