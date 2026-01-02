import java.awt.*;
import java.util.ArrayList;

public abstract class SpecialAttack extends Attack {
    protected int duration;
    protected int currentDuration;

    public SpecialAttack(int damage, int cooldown, int duration) {
        super(damage, cooldown);
        this.duration = duration;
        this.currentDuration = 0;
    }

    @Override
    public void update() {
        updateCooldown();
        if (active) {
            currentDuration--;
            if (currentDuration <= 0) {
                active = false;
            }
        }
    }

    protected void activate() {
        active = true;
        currentDuration = duration;
        startCooldown();
    }
}
