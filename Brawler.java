import java.awt.*;

public class Brawler extends Player {
    public Brawler(int x, int y, int playerNum) {
        super(x, y, playerNum, 100, 6.0, 10.0, new Color(50, 50, 50));
        this.name = "Brawler";
        loadSprites();
    }

    private void loadSprites() {
        String basePath = "Brawler/png/";
        animationManager.loadAnimation("idle", basePath + "idle/", 6, 3);
        animationManager.loadAnimation("run", basePath + "run/", 8, 2);
        animationManager.loadAnimation("jump_up", basePath + "j_up/j_up_", 3, 2);
        animationManager.loadAnimation("jump_down", basePath + "j_down/j_down_", 3, 2);
        animationManager.loadAnimation("attack1", basePath + "1_atk/1_atk_", 6, 2);
        animationManager.loadAnimation("attack2", basePath + "2_atk/2_atk_", 4, 2);
        animationManager.loadAnimation("attack3", basePath + "3_atk/3_atk_", 6, 2);
        animationManager.loadAnimation("defend", basePath + "defend/", 4, 3);
        animationManager.loadAnimation("take_hit", basePath + "take_hit/take_hit_", 3, 2);
        animationManager.loadAnimation("death", basePath + "death/", 10, 3);
        animationManager.loadAnimation("meditate", basePath + "meditate/", 6, 4);
        animationManager.setAnimation("idle");
    }

    @Override
    protected void initializeAttacks() {
        ProjectileAttack ranged = new ProjectileAttack(10, 20, 8.0, Color.DARK_GRAY);
        attackManager.setPrimaryRanged(ranged);
        MeleeAttack melee = new MeleeAttack(15, 15, 40, 10, new Color(100, 100, 100));
        attackManager.setPrimaryMelee(melee);
        rangedAttack = ranged;
        meleeAttack = melee;
    }

    @Override
    protected void drawCharacter(Graphics2D g) {
        String animToUse = currentState;
        if (!animationManager.hasAnimation(currentState)) {
            animToUse = "idle";
        }
        if (animationManager.hasAnimation(animToUse)) {
            animationManager.setAnimation(animToUse);
            // Brawler sprites are positioned higher in frame (not at bottom)
            animationManager.drawWithRatio(g, x, y, width, height, facingRight, 4.0 / 36.0, 5.0 / 16.0, 0.95);
        } else if (sprite != null) {
            if (facingRight) {
                g.drawImage(sprite, x, y, width, height, null);
            } else {
                g.drawImage(sprite, x + width, y, -width, height, null);
            }
        } else {
            g.setColor(characterColor);
            g.fillRect(x, y, width, height);
            g.setColor(Color.BLACK);
            int eyeY = y + height / 4;
            int eyeSize = Math.max(width / 5, 6);
            if (facingRight) {
                g.fillOval(x + width - eyeSize - 5, eyeY, eyeSize, eyeSize);
            } else {
                g.fillOval(x + 5, eyeY, eyeSize, eyeSize);
            }
        }
    }
}
