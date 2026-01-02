import java.awt.*;
import java.util.ArrayList;

public abstract class Attack {
    protected int damage;
    protected int cooldown;
    protected int currentCooldown;
    protected boolean active;
    protected Characters owner;

    public Attack(int damage, int cooldown) {
        this.damage = damage;
        this.cooldown = cooldown;
        this.currentCooldown = 0;
        this.active = false;
    }

    public void setOwner(Characters owner) {
        this.owner = owner;
    }

    public boolean canUse() {
        return currentCooldown <= 0;
    }

    public void updateCooldown() {
        if (currentCooldown > 0) {
            currentCooldown--;
        }
    }

    public abstract void execute(ArrayList<Characters> targets);

    public abstract void update();

    public abstract void draw(Graphics2D g);

    public boolean isActive() {
        return active;
    }

    public int getDamage() {
        return damage;
    }

    protected void startCooldown() {
        currentCooldown = cooldown;
    }
}
