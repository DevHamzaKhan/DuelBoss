import java.awt.*;
import java.util.ArrayList;

public class AreaAttack extends BasicAttack {
    private int radius;
    private int activeFrames;
    private int currentFrame;
    private int centerX, centerY;
    private Color color;

    public AreaAttack(int damage, int cooldown, int radius, int activeFrames, Color color) {
        super(damage, cooldown, radius);
        this.radius = radius;
        this.activeFrames = activeFrames;
        this.currentFrame = 0;
        this.color = color;
    }

    @Override
    public void execute(ArrayList<Characters> targets) {
        if (!canUse() || owner == null) return;

        active = true;
        currentFrame = activeFrames;
        centerX = owner.getX() + owner.getWidth() / 2;
        centerY = owner.getY() + owner.getHeight() / 2;
        startCooldown();

        for (Characters target : targets) {
            if (target != owner) {
                int tx = target.getX() + target.getWidth() / 2;
                int ty = target.getY() + target.getHeight() / 2;
                double dist = Math.sqrt(Math.pow(tx - centerX, 2) + Math.pow(ty - centerY, 2));
                if (dist <= radius) {
                    target.takeDamage(damage);
                }
            }
        }
    }

    @Override
    public void update() {
        updateCooldown();
        if (active) {
            currentFrame--;
            if (currentFrame <= 0) {
                active = false;
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (active) {
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
            g.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        }
    }

    @Override
    public Rectangle getHitbox() {
        return new Rectangle(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }
}
