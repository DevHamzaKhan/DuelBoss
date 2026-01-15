import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseMotionListener {

    public static final int MAP_WIDTH = 4000;
    public static final int MAP_HEIGHT = 4000;

    private final int screenWidth;
    private final int screenHeight;

    private final Timer gameTimer;

    private final Character player;
    private final List<Bullet> bullets;
    private final Camera camera;
    private final UIOverlay uiOverlay;

    private boolean upPressed;
    private boolean downPressed;
    private boolean leftPressed;
    private boolean rightPressed;

    private int mouseX;
    private int mouseY;

    private long lastShotTime;
    private int waveNumber = 1;
    private long waveStartTime;

    public GamePanel(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setBackground(Color.BLACK);
        setFocusable(true);

        player = new Character(MAP_WIDTH / 2.0, MAP_HEIGHT / 2.0);
        bullets = new ArrayList<>();
        camera = new Camera(0, 0, screenWidth, screenHeight, MAP_WIDTH, MAP_HEIGHT);
        uiOverlay = new UIOverlay(player);

        addKeyListener(this);
        addMouseMotionListener(this);

        waveStartTime = System.currentTimeMillis();
        lastShotTime = 0;

        // ~60 FPS game loop
        gameTimer = new Timer(16, this);
        gameTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Apply camera offset
        g2.translate(-camera.getX(), -camera.getY());

        drawGridBackground(g2);
        drawPlayer(g2);
        drawBullets(g2);

        // Reset transform for UI overlay
        g2.translate(camera.getX(), camera.getY());

        long now = System.currentTimeMillis();
        uiOverlay.draw(g2, screenWidth, screenHeight, waveNumber, waveStartTime, now);
    }

    private void drawGridBackground(Graphics2D g2) {
        g2.setColor(new Color(25, 25, 25));
        g2.fillRect(0, 0, MAP_WIDTH, MAP_HEIGHT);

        g2.setColor(new Color(45, 45, 45));
        int gridSize = 50;
        for (int x = 0; x <= MAP_WIDTH; x += gridSize) {
            g2.drawLine(x, 0, x, MAP_HEIGHT);
        }
        for (int y = 0; y <= MAP_HEIGHT; y += gridSize) {
            g2.drawLine(0, y, MAP_WIDTH, y);
        }

        // Map border
        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(0, 0, MAP_WIDTH - 1, MAP_HEIGHT - 1);
    }

    private void drawPlayer(Graphics2D g2) {
        player.draw(g2);
    }

    private void drawBullets(Graphics2D g2) {
        for (Bullet bullet : bullets) {
            bullet.draw(g2);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
    }

    private void updateGame() {
        double deltaSeconds = 16 / 1000.0; // Approximate for now

        updatePlayer(deltaSeconds);
        updateCamera();
        updateShooting();
        updateBullets(deltaSeconds);
    }

    private void updatePlayer(double deltaSeconds) {
        double dx = 0;
        double dy = 0;

        if (upPressed) dy -= 1;
        if (downPressed) dy += 1;
        if (leftPressed) dx -= 1;
        if (rightPressed) dx += 1;

        player.update(dx, dy, deltaSeconds, MAP_WIDTH, MAP_HEIGHT);
    }

    private void updateCamera() {
        int margin = 100; // distance from screen edge before camera starts moving

        double camX = camera.getX();
        double camY = camera.getY();

        double playerScreenX = player.getX() - camX;
        double playerScreenY = player.getY() - camY;

        // Adjust camera so player stays within [margin, screenWidth - margin]
        if (playerScreenX < margin) {
            camX = player.getX() - margin;
        } else if (playerScreenX > screenWidth - margin) {
            camX = player.getX() - (screenWidth - margin);
        }

        if (playerScreenY < margin) {
            camY = player.getY() - margin;
        } else if (playerScreenY > screenHeight - margin) {
            camY = player.getY() - (screenHeight - margin);
        }

        // Apply and clamp to map bounds; if we hit map border, player can then reach screen edge
        camera.setPosition(camX, camY);
    }

    private void updateShooting() {
        long now = System.currentTimeMillis();
        long fireInterval = (long) (1000 / player.getFireRate()); // ms between shots

        if (now - lastShotTime >= fireInterval) {
            // Shoot towards mouse position
            double originX = player.getX();
            double originY = player.getY();

            int targetScreenX = mouseX + camera.getX();
            int targetScreenY = mouseY + camera.getY();

            double dx = targetScreenX - originX;
            double dy = targetScreenY - originY;
            double len = Math.sqrt(dx * dx + dy * dy);
            if (len == 0) {
                dx = 1;
                dy = 0;
                len = 1;
            }

            double vx = (dx / len) * player.getBulletSpeed();
            double vy = (dy / len) * player.getBulletSpeed();

            Bullet bullet = new Bullet(originX, originY, vx, vy,
                    player.getBulletSpeed(), player.getBulletDamage());
            bullets.add(bullet);
            lastShotTime = now;
        }
    }

    private void updateBullets(double deltaSeconds) {
        Iterator<Bullet> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.update(deltaSeconds);
            if (bullet.isOutOfBounds(0, 0, MAP_WIDTH, MAP_HEIGHT)) {
                iterator.remove();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W) upPressed = true;
        if (code == KeyEvent.VK_S) downPressed = true;
        if (code == KeyEvent.VK_A) leftPressed = true;
        if (code == KeyEvent.VK_D) rightPressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W) upPressed = false;
        if (code == KeyEvent.VK_S) downPressed = false;
        if (code == KeyEvent.VK_A) leftPressed = false;
        if (code == KeyEvent.VK_D) rightPressed = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        this.mouseX = e.getX();
        this.mouseY = e.getY();
    }
}


