import java.awt.*;
import java.util.ArrayList;

public class TargetedStrikeAttack extends BasicAttack {
    private int strikeWidth;
    private int strikeHeight;
    private int windupFrames;
    private int activeFrames;
    private int currentFrame;
    private int phase;
    private int targetX, targetY;
    private Color color;
    private Characters targetChar;

    public static final int PHASE_NONE = 0;
    public static final int PHASE_WINDUP = 1;
    public static final int PHASE_STRIKE = 2;

    public TargetedStrikeAttack(int damage, int cooldown, int windupFrames,
                                 int activeFrames, int strikeWidth, Color color) {
        super(damage, cooldown, 0);
        this.windupFrames = windupFrames;
        this.activeFrames = activeFrames;
        this.strikeWidth = strikeWidth;
        this.strikeHeight = Main.HEIGHT;
        this.phase = PHASE_NONE;
        this.color = color;
    }

    @Override
    public void execute(ArrayList<Characters> targets) {
        if (!canUse() || targets.isEmpty()) return;

        Characters closest = null;
        double minDist = Double.MAX_VALUE;

        for (Characters target : targets) {
            if (target != owner) {
                double dist = owner != null ?
                    Math.abs(target.getX() - owner.getX()) : 0;
                if (dist < minDist) {
                    minDist = dist;
                    closest = target;
                }
            }
        }

        if (closest != null) {
            executeOnTarget(closest);
        }
    }

    public void executeOnTarget(Characters target) {
        if (!canUse()) return;

        targetChar = target;
        targetX = target.getX() + target.getWidth() / 2;
        targetY = 0;
        phase = PHASE_WINDUP;
        currentFrame = windupFrames;
        active = true;
        startCooldown();
    }

    public void executeAtPosition(int x) {
        if (!canUse()) return;

        targetX = x;
        targetY = 0;
        targetChar = null;
        phase = PHASE_WINDUP;
        currentFrame = windupFrames;
        active = true;
        startCooldown();
    }

    @Override
    public void update() {
        updateCooldown();

        if (!active) return;

        currentFrame--;

        if (phase == PHASE_WINDUP && currentFrame <= 0) {
            phase = PHASE_STRIKE;
            currentFrame = activeFrames;
        } else if (phase == PHASE_STRIKE && currentFrame <= 0) {
            phase = PHASE_NONE;
            active = false;
        }
    }

    public void checkHits(ArrayList<Characters> targets) {
        if (phase != PHASE_STRIKE) return;

        Rectangle hitbox = getHitbox();
        for (Characters target : targets) {
            if (target != owner && hitbox.intersects(target.getBounds())) {
                target.takeDamage(damage);
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (!active) return;

        if (phase == PHASE_WINDUP) {
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
            g.drawRect(targetX - strikeWidth / 2, 0, strikeWidth, Main.HEIGHT);
        } else if (phase == PHASE_STRIKE) {
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 200));
            g.fillRect(targetX - strikeWidth / 2, 0, strikeWidth, Main.HEIGHT);
        }
    }

    @Override
    public Rectangle getHitbox() {
        return new Rectangle(targetX - strikeWidth / 2, 0, strikeWidth, Main.HEIGHT);
    }

    public int getPhase() {
        return phase;
    }
}
