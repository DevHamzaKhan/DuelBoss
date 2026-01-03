import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.ArrayList;

public abstract class Characters {
    protected int x, y;
    protected int width, height;
    protected int health, maxHealth;
    protected double velocityX, velocityY;
    protected double baseSpeed;
    protected double jumpStrength;
    protected boolean onGround;
    protected boolean facingRight;
    protected boolean stunned;
    protected int stunTimer;
    protected boolean droppingThrough;
    protected int dropTimer;

    // New: Centralized attack management using proper OOP principles
    protected AttackManager attackManager;

    // Deprecated: Keep for backwards compatibility temporarily
    @Deprecated
    protected ProjectileAttack rangedAttack;
    @Deprecated
    protected MeleeAttack meleeAttack;

    protected BufferedImage sprite;
    protected Color characterColor;
    protected String name;
    
    // New: Professional animation system
    protected AnimationManager animationManager;
    protected String currentState; // Track character state for animations

    public Characters(int x, int y, int width, int height, int maxHealth) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.velocityX = 0;
        this.velocityY = 0;
        this.onGround = false;
        this.facingRight = true;
        this.stunned = false;
        this.stunTimer = 0;
        this.droppingThrough = false;
        this.dropTimer = 0;
        this.characterColor = Color.GRAY;
        this.name = "Character";
        
        // Initialize attack manager with proper OOP design
        this.attackManager = new AttackManager(this);
        
        // Initialize animation system
        this.animationManager = new AnimationManager();
        this.currentState = "idle";
    }

    protected abstract void initializeAttacks();

    public void update(Platform[] platforms, double gravityMod, double speedMod) {
        updateStun();
        updateDropThrough();
        applyPhysics(gravityMod, speedMod);
        handlePlatformCollisions(platforms);
        handleBoundaries();
        updateAttacks();
    }

    protected void updateStun() {
        if (stunned) {
            stunTimer--;
            if (stunTimer <= 0) {
                stunned = false;
            }
            velocityX = 0;
        }
    }

    protected void updateDropThrough() {
        if (droppingThrough) {
            dropTimer--;
            if (dropTimer <= 0) {
                droppingThrough = false;
            }
        }
    }

    protected void applyPhysics(double gravityMod, double speedMod) {
        velocityY += 0.5 * gravityMod;
        x += (int)(velocityX * speedMod);
        y += (int)velocityY;
    }

    protected void handlePlatformCollisions(Platform[] platforms) {
        onGround = false;
        for (Platform p : platforms) {
            if (p.checkCollision(x, y, width, height, velocityY, droppingThrough)) {
                y = p.getSurfaceY() - height;
                velocityY = 0;
                onGround = true;
                break;
            }
        }
    }

    protected void handleBoundaries() {
        if (x < 0) x = 0;
        if (x + width > Main.WIDTH) x = Main.WIDTH - width;
        if (y + height > Main.HEIGHT) {
            y = Main.HEIGHT - height;
            velocityY = 0;
            onGround = true;
        }
        if (y < 0) y = 0;
    }

    protected void updateAttacks() {
        // Use the new AttackManager for better encapsulation
        attackManager.updateAll();
        
        // Update animations
        updateAnimationState();
        animationManager.update();
        
        // Backwards compatibility
        if (rangedAttack != null) rangedAttack.update();
        if (meleeAttack != null) meleeAttack.update();
    }
    
    /**
     * Update animation state based on character actions
     * Can be overridden by subclasses for custom behavior
     */
    protected void updateAnimationState() {
        if (isDead()) {
            currentState = "death";
        } else if (!onGround) {
            if (velocityY < 0) {
                currentState = "jump_up";
            } else {
                currentState = "jump_down";
            }
        } else if (Math.abs(velocityX) > 0.1) {
            currentState = "run";
        } else {
            currentState = "idle";
        }
        
        animationManager.setAnimation(currentState);
    }

    public void draw(Graphics2D g) {
        drawCharacter(g);
        drawHealthBar(g);
        drawAttacks(g);
    }

    protected void drawCharacter(Graphics2D g) {
        String animToUse = currentState;
        if (!animationManager.hasAnimation(currentState)) {
            animToUse = "idle";
        }
        if (animationManager.hasAnimation(animToUse)) {
            animationManager.setAnimation(animToUse);
            animationManager.draw(g, x, y, width, height, facingRight);
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

    protected void drawHealthBar(Graphics2D g) {
        int barWidth = width;
        int barHeight = 5;
        int barY = y - 12;

        g.setColor(Color.DARK_GRAY);
        g.fillRect(x - 1, barY - 1, barWidth + 2, barHeight + 2);

        g.setColor(Color.RED);
        g.fillRect(x, barY, barWidth, barHeight);

        g.setColor(Color.GREEN);
        int healthWidth = (int)(barWidth * ((double)health / maxHealth));
        g.fillRect(x, barY, healthWidth, barHeight);
    }

    protected void drawAttacks(Graphics2D g) {
        // Use the new AttackManager for better encapsulation
        attackManager.drawAll(g);
        
        // Backwards compatibility
        if (rangedAttack != null) rangedAttack.draw(g);
        if (meleeAttack != null) meleeAttack.draw(g);
    }

    public void performRangedAttack(ArrayList<Characters> targets) {
        // Try new AttackManager first
        if (attackManager.executePrimaryRanged(targets)) {
            return;
        }
        
        // Fallback to legacy system
        if (rangedAttack != null && rangedAttack.canUse() && !stunned) {
            rangedAttack.execute(targets);
        }
    }

    public void performMeleeAttack(ArrayList<Characters> targets) {
        // Try new AttackManager first
        if (attackManager.executePrimaryMelee(targets)) {
            return;
        }
        
        // Fallback to legacy system
        if (meleeAttack != null && meleeAttack.canUse() && !stunned) {
            meleeAttack.execute(targets);
        }
    }

    public void checkAttackCollisions(ArrayList<Characters> targets) {
        // Use new AttackManager for better collision management
        attackManager.checkAllCollisions(targets);
        
        // Backwards compatibility
        if (rangedAttack != null) {
            rangedAttack.checkCollisions(targets);
        }
    }

    /**
     * Allows character to drop through a platform (not ground) by pressing down.
     * This enables movement to lower platforms when not on the bottom-most platform.
     * Characters will temporarily ignore platform collision for a short time.
     */
    public void dropThroughPlatform() {
        if (onGround && !droppingThrough) {
            droppingThrough = true;
            dropTimer = 15; // Frames to ignore platforms
            y += 5; // Small downward push to start falling
            onGround = false;
        }
    }

    public void moveLeft(double speedMod) {
        if (!stunned) {
            velocityX = -baseSpeed;
            facingRight = false;
        }
    }

    public void moveRight(double speedMod) {
        if (!stunned) {
            velocityX = baseSpeed;
            facingRight = true;
        }
    }

    public void stopMoving() {
        velocityX = 0;
    }

    public void jump() {
        if (onGround && !stunned) {
            velocityY = -jumpStrength;
            onGround = false;
        }
    }
    
    /**
     * Set facing direction directly (useful for AI)
     */
    public void setFacingDirection(boolean facingRight) {
        this.facingRight = facingRight;
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) health = 0;
    }

    public void heal(int amount) {
        health += amount;
        if (health > maxHealth) health = maxHealth;
    }

    public void stun(int duration) {
        stunned = true;
        stunTimer = Math.max(stunTimer, duration);
    }

    public void pushBack(double force) {
        velocityX += force;
    }

    public void pushVertical(double force) {
        velocityY += force;
    }

    public boolean isDead() {
        return health <= 0;
    }

    public boolean isStunned() {
        return stunned;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public boolean isFacingRight() { return facingRight; }
    public boolean isOnGround() { return onGround; }
    public String getName() { return name; }

    public ProjectileAttack getRangedAttack() { return rangedAttack; }
    public MeleeAttack getMeleeAttack() { return meleeAttack; }
    public AttackManager getAttackManager() { return attackManager; }

    public void reset(int newX, int newY) {
        x = newX;
        y = newY;
        health = maxHealth;
        velocityX = 0;
        velocityY = 0;
        stunned = false;
        stunTimer = 0;
        droppingThrough = false;
        
        // Reset using AttackManager
        attackManager.clearAllProjectiles();
        
        // Backwards compatibility
        if (rangedAttack != null) rangedAttack.clearProjectiles();
    }

    protected void loadSprite(String path) {
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("Images/" + path));
        } catch (Exception e) {
            sprite = null;
        }
    }
}
