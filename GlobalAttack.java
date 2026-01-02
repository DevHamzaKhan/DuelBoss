import java.awt.*;
import java.util.ArrayList;

public class GlobalAttack extends SpecialAttack {
    private Color flashColor;
    private int effectType;

    public static final int EFFECT_STUN = 0;
    public static final int EFFECT_DAMAGE = 1;
    public static final int EFFECT_SLOW = 2;

    public GlobalAttack(int damage, int cooldown, int duration, int effectType, Color color) {
        super(damage, cooldown, duration);
        this.effectType = effectType;
        this.flashColor = color;
    }

    @Override
    public void execute(ArrayList<Characters> targets) {
        if (!canUse()) return;

        activate();

        for (Characters target : targets) {
            if (target != owner) {
                switch (effectType) {
                    case EFFECT_STUN:
                        target.stun(duration);
                        break;
                    case EFFECT_DAMAGE:
                        target.takeDamage(damage);
                        break;
                    case EFFECT_SLOW:
                        target.takeDamage(damage);
                        target.stun(duration / 2);
                        break;
                }
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (active) {
            g.setColor(new Color(flashColor.getRed(), flashColor.getGreen(), flashColor.getBlue(), 50));
            g.fillRect(0, 0, Main.WIDTH, Main.HEIGHT);
        }
    }
}
