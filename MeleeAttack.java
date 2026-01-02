import java.awt.*;
import java.util.ArrayList;

public class MeleeAttack extends BasicAttack {
    private int activeFrames;
    private int currentFrame;
    private Rectangle hitbox;
    private Color color;

    public MeleeAttack(int damage, int cooldown, int range, int activeFrames, Color color) {
        super(damage, cooldown, range);
        this.activeFrames = activeFrames;
        this.currentFrame = 0;
        this.width = range;
        this.height = 60;
        this.color = color;
    }

    @Override
    public void execute(ArrayList<Characters> targets) {
        if (!canUse() || owner == null) return;

        active = true;
        currentFrame = activeFrames;
        startCooldown();

        updateHitbox();

        for (Characters target : targets) {
            if (target != owner && hitbox.intersects(target.getBounds())) {
                target.takeDamage(damage);
            }
        }
    }

    private void updateHitbox() {
        if (owner == null) return;

        int hx = owner.isFacingRight() ? owner.getX() + owner.getWidth() : owner.getX() - range;
        int hy = owner.getY();
        hitbox = new Rectangle(hx, hy, range, height);
    }

    @Override
    public void update() {
        updateCooldown();
        if (active) {
            currentFrame--;
            updateHitbox();
            if (currentFrame <= 0) {
                active = false;
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (active && hitbox != null) {
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
            g.fillRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
        }
    }

    @Override
    public Rectangle getHitbox() {
        return hitbox;
    }
}
