import java.awt.*;
import java.util.ArrayList;

public class PersistentAreaAttack extends SpecialAttack {
    private int radius;
    private int centerX, centerY;
    private Color color;
    private int tickDamage;
    private int tickInterval;
    private int tickTimer;
    private double pullStrength;

    public PersistentAreaAttack(int damage, int cooldown, int duration, int radius,
                                 int tickInterval, double pullStrength, Color color) {
        super(damage, cooldown, duration);
        this.radius = radius;
        this.tickDamage = damage;
        this.tickInterval = tickInterval;
        this.tickTimer = 0;
        this.pullStrength = pullStrength;
        this.color = color;
    }

    public void executeAt(int x, int y, ArrayList<Characters> targets) {
        if (!canUse()) return;

        activate();
        this.centerX = x;
        this.centerY = y;
    }

    @Override
    public void execute(ArrayList<Characters> targets) {
        if (owner != null) {
            executeAt(owner.getX() + owner.getWidth() / 2,
                     owner.getY() + owner.getHeight() / 2, targets);
        }
    }

    public void applyEffects(ArrayList<Characters> targets) {
        if (!active) return;

        tickTimer++;
        for (Characters target : targets) {
            if (target != owner) {
                int tx = target.getX() + target.getWidth() / 2;
                int ty = target.getY() + target.getHeight() / 2;
                double dist = Math.sqrt(Math.pow(tx - centerX, 2) + Math.pow(ty - centerY, 2));

                if (dist <= radius && dist > 0) {
                    double dx = (centerX - tx) / dist * pullStrength;
                    double dy = (centerY - ty) / dist * pullStrength;
                    target.pushBack(dx);
                    target.pushVertical(dy);

                    if (tickTimer >= tickInterval) {
                        target.takeDamage(tickDamage);
                    }
                }
            }
        }

        if (tickTimer >= tickInterval) {
            tickTimer = 0;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (active) {
            float alpha = (float) currentDuration / duration * 0.5f;
            g.setColor(new Color(color.getRed() / 255f, color.getGreen() / 255f,
                                  color.getBlue() / 255f, alpha));
            g.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

            g.setColor(color);
            g.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        }
    }
}
