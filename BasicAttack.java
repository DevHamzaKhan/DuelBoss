import java.awt.*;
import java.util.ArrayList;

public abstract class BasicAttack extends Attack {
    protected int range;
    protected int width;
    protected int height;

    public BasicAttack(int damage, int cooldown, int range) {
        super(damage, cooldown);
        this.range = range;
    }

    public int getRange() {
        return range;
    }

    public abstract Rectangle getHitbox();
}
