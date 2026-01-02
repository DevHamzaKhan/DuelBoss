import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages all attacks for a character using professional OOP principles.
 * Provides a centralized way to handle multiple attack types, cooldowns, and execution.
 */
public class AttackManager {
    private Characters owner;
    private Map<String, Attack> attacks;
    private ProjectileAttack primaryRanged;
    private MeleeAttack primaryMelee;
    private SpecialAttack specialAttack;
    
    public AttackManager(Characters owner) {
        this.owner = owner;
        this.attacks = new HashMap<>();
    }
    
    /**
     * Register an attack with a unique name
     */
    public void registerAttack(String name, Attack attack) {
        attack.setOwner(owner);
        attacks.put(name, attack);
    }
    
    /**
     * Set the primary ranged attack (shortcut for common usage)
     */
    public void setPrimaryRanged(ProjectileAttack attack) {
        attack.setOwner(owner);
        this.primaryRanged = attack;
        registerAttack("primary_ranged", attack);
    }
    
    /**
     * Set the primary melee attack (shortcut for common usage)
     */
    public void setPrimaryMelee(MeleeAttack attack) {
        attack.setOwner(owner);
        this.primaryMelee = attack;
        registerAttack("primary_melee", attack);
    }
    
    /**
     * Set the special attack
     */
    public void setSpecialAttack(SpecialAttack attack) {
        attack.setOwner(owner);
        this.specialAttack = attack;
        registerAttack("special", attack);
    }
    
    /**
     * Execute an attack by name
     */
    public boolean executeAttack(String name, ArrayList<Characters> targets) {
        Attack attack = attacks.get(name);
        if (attack != null && attack.canUse()) {
            attack.execute(targets);
            return true;
        }
        return false;
    }
    
    /**
     * Execute primary ranged attack
     */
    public boolean executePrimaryRanged(ArrayList<Characters> targets) {
        if (primaryRanged != null && primaryRanged.canUse()) {
            primaryRanged.execute(targets);
            return true;
        }
        return false;
    }
    
    /**
     * Execute primary melee attack
     */
    public boolean executePrimaryMelee(ArrayList<Characters> targets) {
        if (primaryMelee != null && primaryMelee.canUse()) {
            primaryMelee.execute(targets);
            return true;
        }
        return false;
    }
    
    /**
     * Execute special attack
     */
    public boolean executeSpecial(ArrayList<Characters> targets) {
        if (specialAttack != null && specialAttack.canUse()) {
            specialAttack.execute(targets);
            return true;
        }
        return false;
    }
    
    /**
     * Update all attacks (cooldowns, animations, projectiles)
     */
    public void updateAll() {
        for (Attack attack : attacks.values()) {
            attack.update();
        }
    }
    
    /**
     * Draw all attacks
     */
    public void drawAll(Graphics2D g) {
        for (Attack attack : attacks.values()) {
            attack.draw(g);
        }
    }
    
    /**
     * Check projectile collisions for all projectile attacks
     */
    public void checkAllCollisions(ArrayList<Characters> targets) {
        for (Attack attack : attacks.values()) {
            if (attack instanceof ProjectileAttack) {
                ((ProjectileAttack) attack).checkCollisions(targets);
            }
        }
    }
    
    /**
     * Clear all projectiles (for reset)
     */
    public void clearAllProjectiles() {
        for (Attack attack : attacks.values()) {
            if (attack instanceof ProjectileAttack) {
                ((ProjectileAttack) attack).clearProjectiles();
            }
        }
    }
    
    // Getters
    public ProjectileAttack getPrimaryRanged() { return primaryRanged; }
    public MeleeAttack getPrimaryMelee() { return primaryMelee; }
    public SpecialAttack getSpecialAttack() { return specialAttack; }
    public Attack getAttack(String name) { return attacks.get(name); }
    
    public boolean isAnyAttackActive() {
        for (Attack attack : attacks.values()) {
            if (attack.isActive()) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isMeleeActive() {
        return primaryMelee != null && primaryMelee.isActive();
    }
}
