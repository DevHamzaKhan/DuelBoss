import java.awt.*;

public class Swordsman extends Player {
    public Swordsman(int x, int y, int playerNum) {
        super(x, y, playerNum, 100, 3.0, 10.0, new Color(139, 0, 0));
        this.name = "Swordsman";
        loadSprites();
    }

    private void loadSprites() {
        String basePath = "Swordsman/PNG/";
        animationManager.loadAnimation("idle", basePath + "idle/", 8, 3);
        animationManager.loadAnimation("run", basePath + "run/", 8, 2);
        animationManager.loadAnimation("jump_up", basePath + "j_up/j_up_", 3, 2);
        animationManager.loadAnimation("jump_down", basePath + "j_down/j_down_", 3, 2);
        animationManager.loadAnimation("attack1", basePath + "1_atk/1_atk_", 8, 2);
        animationManager.loadAnimation("attack2", basePath + "2_atk/2_atk_", 6, 2);
        animationManager.loadAnimation("attack3", basePath + "3_atk/3_atk_", 8, 2);
        animationManager.loadAnimation("defend", basePath + "defend/", 4, 3);
        animationManager.loadAnimation("take_hit", basePath + "take_hit/take_hit_", 3, 2);
        animationManager.loadAnimation("death", basePath + "death/", 10, 3);
        animationManager.setAnimation("idle");
    }

    @Override
    protected void initializeAttacks() {
        ProjectileAttack ranged = new ProjectileAttack(15, 40, 6.0, Color.ORANGE);
        attackManager.setPrimaryRanged(ranged);
        MeleeAttack melee = new MeleeAttack(25, 30, 50, 15, new Color(255, 215, 0));
        attackManager.setPrimaryMelee(melee);
        rangedAttack = ranged;
        meleeAttack = melee;
    }
}
