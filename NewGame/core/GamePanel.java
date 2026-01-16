/*
Name: GamePanel.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Core game loop and rendering engine for Polygon Wars. Manages game states (menu, playing, shop, paused, game over), orchestrates updates for all entities/managers, and renders starfield background with game elements. Uses delegation pattern with managers for collision, waves, particles, score, state, and shop functionality.
*/

package core;

import entity.Character;
import entity.Bullet;
import enemy.Enemy;
import enemy.TriangleEnemy;
import enemy.CircleEnemy;
import enemy.SquareEnemy;
import enemy.ShooterEnemy;
import enemy.HexagonEnemy;
import enemy.OctagonEnemy;
import enemy.SpawnerEnemy;
import manager.WaveManager;
import manager.CollisionManager;
import manager.ParticleManager;
import manager.ScoreManager;
import manager.GameStateManager;
import manager.GameStateManager.GameState;
import manager.ShopController;
import ui.Camera;
import ui.UIOverlay;
import ui.MenuRenderer;
import ui.InputHandler;
import ui.HUDRenderer;
import ability.BeamAbility;
import ability.TSPSolver;
import util.MathUtils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel implements ActionListener {

    // dev mode spawns all enemy types in round 0 for quick testing
    private static final boolean DEV_MODE = false;

    public static final int MAP_WIDTH = 2000;
    public static final int MAP_HEIGHT = 2000;

    // 60 fps game loop (16ms per frame)
    private static final int FRAME_DELAY_MS = 16;
    private static final double DELTA_SECONDS = FRAME_DELAY_MS / 1000.0;

    // background rendering constants for starfield effect
    private static final int SMALL_STAR_COUNT = 300;
    private static final int LARGE_STAR_COUNT = 50;
    private static final int NEBULA_SIZE = 400;
    private static final int BORDER_WIDTH = 8;
    // deterministic random for consistent star positions across frames
    private static final Random STAR_RANDOM_1 = new Random(12345);
    private static final Random STAR_RANDOM_2 = new Random(54321);
    private static final Color BACKGROUND_COLOR = new Color(10, 10, 30);
    private static final Color STAR_COLOR_DIM = new Color(255, 255, 255, 200);
    private static final Color STAR_COLOR_BRIGHT = new Color(255, 255, 255, 255);
    private static final Color BORDER_COLOR = new Color(60, 60, 100);

    // Beam cooldown bar constants
    private static final int BEAM_BAR_WIDTH = 200;
    private static final int BEAM_BAR_HEIGHT = 20;
    private static final int BEAM_BAR_MARGIN = 20;

    // Score panel constants
    private static final int SCORE_X = 20;
    private static final int SCORE_Y = 70;
    private static final int STATS_LINE_HEIGHT = 28;

    // when hexagon enemy is killed, it spawns 6 triangles in a circle
    private static final int HEX_SPLIT_COUNT = 6;
    private static final double HEX_SPLIT_TRIANGLE_RADIUS = 14;
    private static final double HEX_SPLIT_TRIANGLE_HEALTH = 40;
    private static final double HEX_SPLIT_TRIANGLE_DAMAGE = 5;
    private static final double HEX_SPLIT_TRIANGLE_SPEED = 280;

    private final int screenWidth;
    private final int screenHeight;
    private final Timer gameTimer;

    // Game entities
    private Character player;
    private final List<Bullet> bullets;
    private final List<Enemy> enemies;
    private Camera camera;
    private UIOverlay uiOverlay;

    // Extracted managers
    private final InputHandler inputHandler;
    private final WaveManager waveManager;
    private final CollisionManager collisionManager;
    private final MenuRenderer menuRenderer;
    private final ParticleManager particleManager;
    private final ScoreManager scoreManager;
    private final GameStateManager stateManager;
    private final ShopController shopController;
    private final HUDRenderer hudRenderer;

    // Game state
    private long lastShotTime;
    
    // Ultimate ability system
    private final BeamAbility beamAbility;
    private long lastUltimateTime = 0;
    private static final long ULTIMATE_COOLDOWN_MS = 10_000; // 10 seconds

    public GamePanel(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setBackground(Color.BLACK);
        setFocusable(true);

        // Initialize entities
        player = new Character(MAP_WIDTH / 2.0, MAP_HEIGHT / 2.0);
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        camera = new Camera(screenWidth, screenHeight);
        uiOverlay = new UIOverlay(player);

        // Initialize managers
        inputHandler = new InputHandler();
        waveManager = new WaveManager(MAP_WIDTH, MAP_HEIGHT);
        collisionManager = new CollisionManager(MAP_WIDTH, MAP_HEIGHT);
        menuRenderer = new MenuRenderer(screenWidth, screenHeight);
        particleManager = new ParticleManager();
        scoreManager = new ScoreManager();
        stateManager = new GameStateManager();
        shopController = new ShopController();
        hudRenderer = new HUDRenderer();
        beamAbility = new BeamAbility();

        // Register input listeners
        addKeyListener(inputHandler);
        addMouseMotionListener(inputHandler);
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (e.getButton() == java.awt.event.MouseEvent.BUTTON3) {
                    // Right click - ultimate ability
                    handleRightClick(e.getX(), e.getY());
                } else {
                    handleClick(e.getX(), e.getY());
                }
            }
        });

        lastShotTime = 0;

        gameTimer = new Timer(16, this);
        gameTimer.start();
    }

    private void handleClick(int x, int y) {
        if (stateManager.isShowingUpgradeShop()) {
            handleShopClick(x, y);
        } else if (stateManager.getCurrentState() == GameState.MAIN_MENU) {
            handleMainMenuClick(x, y);
        } else if (stateManager.getCurrentState() == GameState.GAME_OVER) {
            handleGameOverClick(x, y);
        } else if (stateManager.getCurrentState() == GameState.HOW_TO_PLAY) {
            handleHowToPlayClick(x, y);
        }
    }
    
    private void handleRightClick(int screenX, int screenY) {
        if (stateManager.getCurrentState() != GameState.PLAYING || stateManager.isShowingUpgradeShop() || stateManager.isGamePaused()) {
            return;
        }
        
        // Check cooldown
        long now = System.currentTimeMillis();
        if (now - lastUltimateTime < ULTIMATE_COOLDOWN_MS) {
            return; // Still on cooldown
        }
        
        // Convert screen coordinates to world coordinates
        double worldX = screenX + camera.getX();
        double worldY = screenY + camera.getY();
        
        // Find enemy at click position
        Enemy clickedEnemy = findEnemyAt(worldX, worldY);
        if (clickedEnemy == null || !clickedEnemy.isAlive()) {
            return;
        }
        
        // Find all enemies of the same type
        Class<?> enemyType = clickedEnemy.getClass();
        List<Enemy> enemiesOfType = new ArrayList<>();
        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && enemy.getClass() == enemyType) {
                enemiesOfType.add(enemy);
            }
        }
        
        if (enemiesOfType.isEmpty()) {
            return;
        }
        
        // Activate ultimate ability
        activateUltimateAbility(enemiesOfType);
        lastUltimateTime = now;
    }
    
    private Enemy findEnemyAt(double worldX, double worldY) {
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) {
                continue;
            }
            double distance = MathUtils.distance(worldX, worldY, enemy.getX(), enemy.getY());
            if (distance <= enemy.getRadius()) {
                return enemy;
            }
        }
        return null;
    }
    
    private void activateUltimateAbility(List<Enemy> targetEnemies) {
        if (targetEnemies.isEmpty()) return;
        
        // Get player position as start
        double startX = player.getX();
        double startY = player.getY();
        
        // Build list of points to visit
        List<double[]> points = new ArrayList<>();
        for (Enemy enemy : targetEnemies) {
            if (enemy != null && enemy.isAlive()) {
                points.add(new double[]{enemy.getX(), enemy.getY()});
            }
        }
        
        if (points.isEmpty()) return;
        
        // Solve TSP to find shortest path
        List<double[]> path = TSPSolver.solveTSP(startX, startY, points);
        
        if (path.isEmpty() || path.size() < 2) return;
        
        // Pause the game
        stateManager.setGamePaused(true);
        
        // Activate beam (pass particleManager for explosions)
        beamAbility.activate(path, targetEnemies, particleManager);
    }

    private void startNewGame() {
        scoreManager.reset();
        stateManager.startNewGame();

        player = new Character(MAP_WIDTH / 2.0, MAP_HEIGHT / 2.0);
        camera = new Camera(screenWidth, screenHeight);
        uiOverlay = new UIOverlay(player);

        bullets.clear();
        enemies.clear();
        particleManager.clear();
        beamAbility.deactivate();
        lastUltimateTime = 0;

        if (DEV_MODE) {
            waveManager.setupRoundZero(enemies, bullets);
        } else {
            waveManager.startNewWave(1, enemies, bullets);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (stateManager.getCurrentState() == GameState.MAIN_MENU) {
            menuRenderer.drawMainMenu(g2, scoreManager.getHighScore());
        } else if (stateManager.getCurrentState() == GameState.HOW_TO_PLAY) {
            menuRenderer.drawHowToPlay(g2);
        } else if (stateManager.getCurrentState() == GameState.GAME_OVER) {
            menuRenderer.drawGameOver(g2, scoreManager.getScore(), waveManager.getWaveNumber(), scoreManager.getHighScore());
        } else if (stateManager.getCurrentState() == GameState.PLAYING) {
            g2.translate(-camera.getX(), -camera.getY());
            drawGameWorld(g2);
            g2.translate(camera.getX(), camera.getY());

            if (stateManager.isShowingUpgradeShop()) {
                menuRenderer.drawUpgradeShop(g2, player, scoreManager.getCurrency(), scoreManager.getScore());
            } else {
                drawHUD(g2);
            }
        }
    }

    private void drawGameWorld(Graphics2D g2) {
        drawGridBackground(g2);
        player.draw(g2);
        for (Bullet bullet : bullets)
            bullet.draw(g2);
        for (Enemy enemy : enemies)
            if (enemy.isAlive())
                enemy.draw(g2);
        particleManager.draw(g2);
        beamAbility.draw(g2);
    }

    private void drawGridBackground(Graphics2D g2) {
        g2.setColor(BACKGROUND_COLOR);
        g2.fillRect(0, 0, MAP_WIDTH, MAP_HEIGHT);

        // Draw small stars (using static Random with fixed seed for consistency)
        STAR_RANDOM_1.setSeed(12345);
        g2.setColor(STAR_COLOR_DIM);
        for (int i = 0; i < SMALL_STAR_COUNT; i++) {
            int starX = STAR_RANDOM_1.nextInt(MAP_WIDTH);
            int starY = STAR_RANDOM_1.nextInt(MAP_HEIGHT);
            int size = STAR_RANDOM_1.nextInt(3) + 1;
            g2.fillOval(starX, starY, size, size);
        }

        // Draw larger, brighter stars
        STAR_RANDOM_2.setSeed(54321);
        g2.setColor(STAR_COLOR_BRIGHT);
        for (int i = 0; i < LARGE_STAR_COUNT; i++) {
            int starX = STAR_RANDOM_2.nextInt(MAP_WIDTH);
            int starY = STAR_RANDOM_2.nextInt(MAP_HEIGHT);
            g2.fillOval(starX, starY, 2, 2);
        }

        // Corner nebula effects
        g2.setColor(new Color(80, 60, 140, 40));
        g2.fillRect(0, 0, NEBULA_SIZE, NEBULA_SIZE);
        g2.setColor(new Color(60, 80, 160, 40));
        g2.fillRect(MAP_WIDTH - NEBULA_SIZE, 0, NEBULA_SIZE, NEBULA_SIZE);
        g2.setColor(new Color(100, 60, 120, 40));
        g2.fillRect(0, MAP_HEIGHT - NEBULA_SIZE, NEBULA_SIZE, NEBULA_SIZE);
        g2.setColor(new Color(70, 70, 150, 40));
        g2.fillRect(MAP_WIDTH - NEBULA_SIZE, MAP_HEIGHT - NEBULA_SIZE, NEBULA_SIZE, NEBULA_SIZE);

        // Border
        g2.setStroke(new BasicStroke(BORDER_WIDTH));
        g2.setColor(BORDER_COLOR);
        g2.drawRect(0, 0, MAP_WIDTH, MAP_HEIGHT);
        g2.setStroke(new BasicStroke(1));
    }

    private void drawHUD(Graphics2D g2) {
        long now = System.currentTimeMillis();
        int enemiesRemaining = waveManager.getEnemiesRemaining(enemies.size());
        String status = waveManager.getWaveStatusText();
        uiOverlay.draw(g2, screenWidth, screenHeight, waveManager.getWaveNumber(),
                waveManager.getWaveStartTime(), now, enemiesRemaining, status);
        hudRenderer.drawHUD(g2, screenWidth, screenHeight, scoreManager.getScore(), 
                scoreManager.getCurrency(), player, lastUltimateTime, now);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Update hover state for menus
        boolean isMenu = stateManager.getCurrentState() == GameState.MAIN_MENU || stateManager.getCurrentState() == GameState.GAME_OVER
                || stateManager.getCurrentState() == GameState.HOW_TO_PLAY;
        menuRenderer.updateHover(inputHandler.getMouseX(), inputHandler.getMouseY(), isMenu, stateManager.isShowingUpgradeShop());

        if (stateManager.getCurrentState() == GameState.PLAYING) {
            updateGame();
            if (player.getHealthLeft() <= 0)
                gameOver();
        }
        repaint();
    }

    private void updateGame() {
        double deltaSeconds = DELTA_SECONDS;
        
        // Update beam ability (always update, even when paused)
        boolean beamWasActive = beamAbility.isActive();
        boolean beamStillActive = beamAbility.update(deltaSeconds);
        
        // If beam just finished, unpause game and process dead enemies
        if (beamWasActive && !beamStillActive) {
            stateManager.setGamePaused(false);
            // Process any enemies that were killed by the beam
            updateEnemies(0.0); // Small update to clean up dead enemies
        }
        
        // Pause game updates during beam (but still update beam animation)
        if (stateManager.isGamePaused() && beamAbility.isActive()) {
            // Only update camera to follow player (visual only)
            updateCamera();
            return;
        }

        updatePlayer(deltaSeconds);
        updateCamera();
        updateShooting();
        updateBullets(deltaSeconds);
        updateEnemies(deltaSeconds);
        waveManager.updateSpawning(enemies);
        updateWaveProgress();
        particleManager.update(deltaSeconds);
    }

    private void updatePlayer(double deltaSeconds) {
        if (stateManager.isShowingUpgradeShop())
            return;

        double dx = 0, dy = 0;
        if (inputHandler.isUpPressed())
            dy -= 1;
        if (inputHandler.isDownPressed())
            dy += 1;
        if (inputHandler.isLeftPressed())
            dx -= 1;
        if (inputHandler.isRightPressed())
            dx += 1;

        player.update(dx, dy, deltaSeconds, MAP_WIDTH, MAP_HEIGHT);

        double targetX = inputHandler.getMouseX() + camera.getX();
        double targetY = inputHandler.getMouseY() + camera.getY();
        player.setAngleToward(targetX, targetY);
    }

    private void updateCamera() {
        camera.centerOn(player.getX(), player.getY());
    }

    private void updateShooting() {
        if (stateManager.isShowingUpgradeShop()) {
            return;
        }

        long now = System.currentTimeMillis();
        long fireInterval = (long) (1000 / player.getFireRate());

        if (now - lastShotTime >= fireInterval) {
            double originX = player.getX();
            double originY = player.getY();
            int targetX = inputHandler.getMouseX() + camera.getX();
            int targetY = inputHandler.getMouseY() + camera.getY();

            double[] direction = MathUtils.normalizeWithDefault(targetX - originX, targetY - originY, 1, 0);
            double velocityX = direction[0] * player.getBulletSpeed();
            double velocityY = direction[1] * player.getBulletSpeed();

            bullets.add(new Bullet(originX, originY, velocityX, velocityY,
                    player.getBulletSpeed(), player.getBulletDamage(), true));
            lastShotTime = now;
        }
    }

    private void updateBullets(double deltaSeconds) {
        Iterator<Bullet> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.update(deltaSeconds);
            boolean remove = false;

            if (bullet.isOutOfBounds(0, 0, MAP_WIDTH, MAP_HEIGHT)) {
                remove = true;
            } else if (bullet.isFromPlayer()) {
                for (Enemy enemy : enemies) {
                    if (enemy.isAlive() && collisionManager.bulletHitsEnemy(bullet, enemy)) {
                        enemy.takeDamage(bullet.getDamage());
                        remove = true;
                        break;
                    }
                }
            } else if (collisionManager.bulletHitsPlayer(bullet, player)) {
                player.takeDamage(bullet.getDamage());
                remove = true;
            }

            if (remove)
                iterator.remove();
        }
    }

    private void updateEnemies(double deltaSeconds) {
        List<Enemy> spawnedFromDeaths = new ArrayList<>();
        List<Enemy> spawnedFromSpawners = new ArrayList<>();

        Iterator<Enemy> iterator = enemies.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            if (!enemy.isAlive()) {
                scoreManager.awardScoreForEnemy(enemy);
                particleManager.spawnDeathEffect(enemy);
                if (enemy instanceof HexagonEnemy)
                    spawnHexSplit((HexagonEnemy) enemy, spawnedFromDeaths);
                iterator.remove();
                continue;
            }

            enemy.update(deltaSeconds, player, bullets, MAP_WIDTH, MAP_HEIGHT);

            if (enemy instanceof SpawnerEnemy) {
                ((SpawnerEnemy) enemy).trySpawn(player, spawnedFromSpawners);
            }

            collisionManager.resolveEnemyCollisions(enemy, enemies, index);

            if (enemy.collidesWith(player)) {
                enemy.onCollideWithPlayer(player);
                scoreManager.awardScoreForEnemy(enemy);
                particleManager.spawnDeathEffect(enemy);
                if (enemy instanceof HexagonEnemy)
                    spawnHexSplit((HexagonEnemy) enemy, spawnedFromDeaths);
                iterator.remove();
            }
            index++;
        }

        enemies.addAll(spawnedFromDeaths);
        enemies.addAll(spawnedFromSpawners);
    }

    private void updateWaveProgress() {
        if (waveManager.getWaveNumber() == 0) {
            if (enemies.isEmpty() && !stateManager.isShowingUpgradeShop()) {
                scoreManager.awardWaveCurrency(0);
                stateManager.setShowingUpgradeShop(true);
            }
            return;
        }

        if (!waveManager.isSpawningComplete())
            return;

        if (enemies.isEmpty() && !stateManager.isShowingUpgradeShop()) {
            scoreManager.awardWaveCurrency(waveManager.getWaveNumber());
            stateManager.setShowingUpgradeShop(true);
        }
    }

    private void spawnHexSplit(HexagonEnemy hex, List<Enemy> collector) {
        double centerX = hex.getX();
        double centerY = hex.getY();
        double spawnDistance = hex.getRadius();

        for (int i = 0; i < HEX_SPLIT_COUNT; i++) {
            double angle = i * (2 * Math.PI / HEX_SPLIT_COUNT);
            double spawnX = centerX + Math.cos(angle) * spawnDistance;
            double spawnY = centerY + Math.sin(angle) * spawnDistance;
            collector.add(new TriangleEnemy(spawnX, spawnY,
                    HEX_SPLIT_TRIANGLE_RADIUS, HEX_SPLIT_TRIANGLE_HEALTH,
                    HEX_SPLIT_TRIANGLE_DAMAGE, HEX_SPLIT_TRIANGLE_SPEED));
        }
    }

    private void gameOver() {
        stateManager.endGame();
        scoreManager.updateHighScore();
    }

    // Click handlers
    private void handleMainMenuClick(int x, int y) {
        int btn = menuRenderer.getClickedMenuButton(x, y);
        if (btn == 0)
            startNewGame();
        else if (btn == 1)
            stateManager.setState(GameState.HOW_TO_PLAY);
        else if (btn == 2)
            System.exit(0);
    }

    private void handleGameOverClick(int x, int y) {
        int btn = menuRenderer.getClickedMenuButton(x, y);
        if (btn == 0)
            stateManager.returnToMenu();
        else if (btn == 1)
            startNewGame();
    }

    private void handleHowToPlayClick(int x, int y) {
        if (menuRenderer.getClickedMenuButton(x, y) == 0)
            stateManager.returnToMenu();
    }

    private void handleShopClick(int x, int y) {
        int btn = menuRenderer.getClickedShopButton(x, y);
        if (btn == -1) return;
        
        ShopController.ShopPurchaseResult result = shopController.handlePurchase(btn, player, scoreManager);
        
        switch (result.getType()) {
            case CONTINUE:
                stateManager.setShowingUpgradeShop(false);
                int nextWave = (waveManager.getWaveNumber() == 0) ? 1 : waveManager.getWaveNumber() + 1;
                waveManager.startNewWave(nextWave, enemies, bullets);
                break;
            case SCORE_PURCHASED:
                scoreManager.addScore(result.getScoreAwarded());
                break;
            case SUCCESS:
            case INSUFFICIENT_CURRENCY:
            case MAX_LEVEL_REACHED:
            case INVALID_BUTTON:
                // No additional action needed
                break;
        }
    }
}
