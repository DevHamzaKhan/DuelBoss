import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public abstract class Boss extends Characters {
    protected SpecialAttack specialAttack;
    protected int specialCooldown;
    protected int specialTimer;
    protected Random random;
    protected Characters target;
    protected ArrayList<Characters> targetList;

    // New: AI Behavior Strategy Pattern for professional OOP design
    protected AIBehavior aiBehavior;
    
    // Deprecated: Old AI parameters (keeping for backwards compatibility)
    @Deprecated
    protected double aggroRange;
    @Deprecated
    protected double attackRange;
    @Deprecated
    protected double preferredDistance;

    public Boss(int x, int y, int maxHealth, double baseSpeed, String name, Color color) {
        super(x, y, 159, 192, maxHealth);
        this.name = name;
        this.characterColor = color;
        this.baseSpeed = baseSpeed;
        this.jumpStrength = 12.0;
        this.specialCooldown = 300;
        this.specialTimer = specialCooldown;
        this.random = new Random();
        this.targetList = new ArrayList<>();

        // Default values for backwards compatibility
        this.aggroRange = 500;
        this.attackRange = 300;
        this.preferredDistance = 150;
        
        // Default to aggressive AI behavior
        this.aiBehavior = new AggressiveAIBehavior();
    }
    
    /**
     * Set custom AI behavior using Strategy pattern
     */
    public void setAIBehavior(AIBehavior behavior) {
        this.aiBehavior = behavior;
    }

    @Override
    protected abstract void initializeAttacks();

    protected abstract void initializeSpecialAttack();

    public void setTarget(Characters target) {
        this.target = target;
        this.targetList.clear();
        this.targetList.add(target);
    }

    public void updateAI(Platform[] platforms, double gravityMod, double speedMod) {
        if (stunned || target == null) {
            super.update(platforms, gravityMod, speedMod);
            return;
        }

        updateSpecialAttack();
        
        // Use the new AI Behavior Strategy pattern for professional OOP design
        // IMPORTANT: Set velocities BEFORE applying physics
        if (aiBehavior != null) {
            aiBehavior.update(this, target, targetList, platforms, speedMod);
        } else {
            // Fallback to old behavior for backwards compatibility
            updateMovement(speedMod);
            updateCombat();
        }
        
        // Apply physics and movement AFTER AI decisions
        super.update(platforms, gravityMod, speedMod);
    }

    protected void updateSpecialAttack() {
        specialTimer--;
        if (specialTimer <= 0) {
            executeSpecialAttack();
            specialTimer = specialCooldown;
        }

        if (specialAttack != null) {
            specialAttack.update();
        }
    }

    protected void executeSpecialAttack() {
        if (specialAttack != null && specialAttack.canUse()) {
            specialAttack.execute(targetList);
        }
    }

    /**
     * @deprecated Use AIBehavior instead for better OOP design
     */
    @Deprecated
    protected void updateMovement(double speedMod) {
        if (target == null) return;

        int distX = target.getX() - x;
        int distY = target.getY() - y;

        if (Math.abs(distX) > preferredDistance) {
            if (distX > 0) {
                moveRight(speedMod);
            } else {
                moveLeft(speedMod);
            }
        } else {
            stopMoving();
            facingRight = distX > 0;
        }

        if (distY < -50 && onGround && random.nextInt(100) < 5) {
            jump();
        }

        if (distY > 100 && onGround && random.nextInt(100) < 3) {
            dropThroughPlatform();
        }
    }

    /**
     * @deprecated Use AIBehavior instead for better OOP design
     */
    @Deprecated
    protected void updateCombat() {
        if (target == null) return;

        int distX = Math.abs(target.getX() - x);
        int distY = Math.abs(target.getY() - y);

        if (distX < attackRange && distY < 100) {
            if (rangedAttack != null && rangedAttack.canUse()) {
                performRangedAttack(targetList);
            }
        }

        if (distX < 100 && distY < 80) {
            if (meleeAttack != null && meleeAttack.canUse()) {
                performMeleeAttack(targetList);
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(name);
        g.drawString(name, x + (width - textWidth) / 2, y - 20);

        if (specialAttack != null) {
            specialAttack.draw(g);
        }
    }
    
    @Override
    protected void drawCharacter(Graphics2D g) {
        String animToUse = currentState;
        if (!animationManager.hasAnimation(currentState)) {
            animToUse = "idle";
        }
        if (animationManager.hasAnimation(animToUse)) {
            animationManager.setAnimation(animToUse);
            // Boss sprites are 12/36 width and 60% height of frame (vs player 4/36 and 5/16)
            animationManager.drawWithRatio(g, x, y, width, height, facingRight, 12.0/36.0, 0.6);
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

    @Override
    protected void drawHealthBar(Graphics2D g) {
        int barWidth = width;
        int barHeight = 8;
        int barY = y - 15;

        g.setColor(Color.DARK_GRAY);
        g.fillRect(x - 1, barY - 1, barWidth + 2, barHeight + 2);

        g.setColor(Color.RED);
        g.fillRect(x, barY, barWidth, barHeight);

        g.setColor(Color.GREEN);
        int healthWidth = (int)(barWidth * ((double)health / maxHealth));
        g.fillRect(x, barY, healthWidth, barHeight);
    }

    @Override
    public void reset(int newX, int newY) {
        super.reset(newX, newY);
        specialTimer = specialCooldown;
        target = null;
        targetList.clear();
    }

    public SpecialAttack getSpecialAttack() {
        return specialAttack;
    }
}
