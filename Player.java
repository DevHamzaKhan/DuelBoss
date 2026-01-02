import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Abstract base class for player characters.
 * Uses OOP principles: encapsulation of input handling, polymorphism for character types.
 */
public abstract class Player extends Characters {
    protected int playerNum;
    protected HashSet<Integer> activeKeys;
    protected ArrayList<Characters> targets;

    protected int upKey, downKey, leftKey, rightKey;
    protected int rangedKey, meleeKey;

    public Player(int x, int y, int playerNum, int maxHealth, double baseSpeed,
                  double jumpStrength, Color color) {
        super(x, y, 53, 80, maxHealth);
        this.playerNum = playerNum;
        this.baseSpeed = baseSpeed;
        this.jumpStrength = jumpStrength;
        this.characterColor = color;
        this.activeKeys = new HashSet<>();
        this.targets = new ArrayList<>();
        this.facingRight = (playerNum == 1);

        setupControls();
        initializeAttacks();
    }

    /**
     * Configure control scheme based on player number
     */
    private void setupControls() {
        if (playerNum == 1) {
            upKey = KeyEvent.VK_W;
            downKey = KeyEvent.VK_S;
            leftKey = KeyEvent.VK_A;
            rightKey = KeyEvent.VK_D;
            rangedKey = KeyEvent.VK_Q;
            meleeKey = KeyEvent.VK_E;
        } else {
            upKey = KeyEvent.VK_I;
            downKey = KeyEvent.VK_K;
            leftKey = KeyEvent.VK_J;
            rightKey = KeyEvent.VK_L;
            rangedKey = KeyEvent.VK_U;
            meleeKey = KeyEvent.VK_O;
        }
    }

    public void setTargets(ArrayList<Characters> targets) {
        this.targets = targets;
    }

    /**
     * Handle key press events. 
     * Attacks are triggered immediately, movement is handled in processInput.
     * Down key allows dropping through platforms to reach lower levels.
     */
    public void handleKeyPress(int keyCode) {
        activeKeys.add(keyCode);

        if (keyCode == rangedKey) {
            performRangedAttack(targets);
        }
        if (keyCode == meleeKey) {
            performMeleeAttack(targets);
        }
        // Allow dropping through platforms to descend to lower levels
        if (keyCode == downKey && onGround) {
            dropThroughPlatform();
        }
    }

    public void handleKeyRelease(int keyCode) {
        activeKeys.remove(keyCode);
    }

    /**
     * Process continuous input for movement (separate from discrete button presses)
     */
    protected void processInput(double speedMod) {
        if (stunned) {
            stopMoving();
            return;
        }

        boolean movingLeft = activeKeys.contains(leftKey);
        boolean movingRight = activeKeys.contains(rightKey);
        boolean jumping = activeKeys.contains(upKey);

        if (movingLeft && !movingRight) {
            moveLeft(speedMod);
        } else if (movingRight && !movingLeft) {
            moveRight(speedMod);
        } else {
            stopMoving();
        }

        if (jumping) {
            jump();
        }
    }

    @Override
    public void update(Platform[] platforms, double gravityMod, double speedMod) {
        processInput(speedMod);
        super.update(platforms, gravityMod, speedMod);
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("P" + playerNum, x + width / 2 - 6, y - 18);
    }

    public boolean isMeleeActive() {
        // Use new AttackManager API
        return attackManager.isMeleeActive();
    }

    public int getPlayerNum() {
        return playerNum;
    }

    @Override
    public void reset(int newX, int newY) {
        super.reset(newX, newY);
        activeKeys.clear();
    }
}
