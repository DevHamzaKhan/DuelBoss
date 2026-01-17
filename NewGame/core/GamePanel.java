/*
Name: GamePanel.java
Authors: Hamza Khan & Alec Li  
Date: January 16, 2026
Description: Core game loop
*/

package core;

import entity.Character;
import entity.Bullet;
import enemy.Enemy;
import enemy.TriangleEnemy;
import enemy.HexagonEnemy;
import enemy.StarEnemy;
import manager.WaveManager;
import manager.CollisionManager;
import manager.ParticleManager;
import manager.ScoreManager;
import manager.ShopController;
import ui.Camera;
import ui.InputHandler;
import ui.HUDRenderer;
import ability.BeamAbility;
import ability.TSPSolver;
import util.Utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel implements ActionListener {

    private static final boolean DEV_MODE = true;
    public static final int MAP_WIDTH = 2000;
    public static final int MAP_HEIGHT = 2000;
    private static final int FRAME_DELAY_MS = 16;
    private static final double DELTA_SECONDS = FRAME_DELAY_MS / 1000.0;

    // starfield constants
    private static final int SMALL_STAR_COUNT = 300;
    private static final int LARGE_STAR_COUNT = 50;
    private static final int NEBULA_SIZE = 400;
    private static final int BORDER_WIDTH = 8;
    private static final Random STAR_RANDOM_1 = new Random(12345);
    private static final Random STAR_RANDOM_2 = new Random(54321);
    private static final Color BACKGROUND_COLOR = new Color(10, 10, 30);
    private static final Color STAR_COLOR_DIM = new Color(255, 255, 255, 200);
    private static final Color STAR_COLOR_BRIGHT = new Color(255, 255, 255, 255);
    private static final Color BORDER_COLOR = new Color(60, 60, 100);
    private static final Color NEBULA_PURPLE = new Color(80, 60, 140, 40);
    private static final Color NEBULA_BLUE = new Color(60, 80, 160, 40);
    private static final Color NEBULA_MAGENTA = new Color(100, 60, 120, 40);
    private static final Color NEBULA_INDIGO = new Color(70, 70, 150, 40);

    // hexagon split constants
    private static final int HEX_SPLIT_COUNT = 6;
    private static final double HEX_SPLIT_TRIANGLE_RADIUS = 14;
    private static final double HEX_SPLIT_TRIANGLE_HEALTH = 40;
    private static final double HEX_SPLIT_TRIANGLE_DAMAGE = 5;
    private static final double HEX_SPLIT_TRIANGLE_SPEED = 280;
    private static final long ULTIMATE_COOLDOWN_MS = 10_000;

    private final int screenWidth;
    private final int screenHeight;
    private final Timer gameTimer;

    // game entities
    private Character player;
    private final List<Bullet> bullets;
    private final List<Enemy> enemies;
    private Camera camera;

    // managers
    private final InputHandler inputHandler;
    private final WaveManager waveManager;
    private final CollisionManager collisionManager;
    private final ParticleManager particleManager;
    private final ScoreManager scoreManager;
    private final ShopController shopController;
    private final HUDRenderer hudRenderer;
    private final BeamAbility beamAbility;

    // state
    private long lastShotTime;
    private long lastUltimateTime = 0;
    private boolean showingShop = false;
    private boolean gamePaused = false;
    private boolean gameOver = false;
    private GameListener gameListener;

    // listener interface for communication with parent container
    public interface GameListener {
        void onShopOpen(Character player, int currency, int score);

        void onGameOver(int score, int waveNumber, int highScore);
    }

    // constructor initializes all game components and input handlers
    public GamePanel(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setBackground(Color.BLACK);
        setFocusable(true);

        // initialize entities
        player = new Character(MAP_WIDTH / 2.0, MAP_HEIGHT / 2.0);
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        camera = new Camera(screenWidth, screenHeight);

        // initialize managers
        inputHandler = new InputHandler();
        waveManager = new WaveManager(MAP_WIDTH, MAP_HEIGHT);
        collisionManager = new CollisionManager(MAP_WIDTH, MAP_HEIGHT);
        particleManager = new ParticleManager();
        scoreManager = new ScoreManager();
        shopController = new ShopController();
        hudRenderer = new HUDRenderer();
        beamAbility = new BeamAbility();

        // event handling setup
        addKeyListener(inputHandler);
        addMouseMotionListener(inputHandler);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    handleRightClick(e.getX(), e.getY());
                }
            }
        });

        lastShotTime = 0;
        gameTimer = new Timer(FRAME_DELAY_MS, this);
        // don't start timer until game begins
    }

    // registers game listener shop open and game over
    //
    public void setGameListener(GameListener listener) {
        this.gameListener = listener;
    }

    // stops the game timer to pause all updates
    public void pauseGame() {
        gameTimer.stop();
    }

    // restarts the game timer to resume updates
    public void resumeGame() {
        gameTimer.start();
    }

    // stops the game timer permanently (for game over)
    public void stopGame() {
        gameTimer.stop();
    }

    // resets all game state and starts a fresh game
    public void startNewGame() {
        scoreManager.reset();
        player = new Character(MAP_WIDTH / 2.0, MAP_HEIGHT / 2.0);
        camera = new Camera(screenWidth, screenHeight);
        bullets.clear();
        enemies.clear();
        particleManager.clear();
        beamAbility.deactivate();
        lastUltimateTime = 0;
        showingShop = false;
        gamePaused = false;
        gameOver = false;

        if (DEV_MODE) {
            waveManager.setupRoundZero(enemies, bullets);
        } else {
            waveManager.startNewWave(1, enemies, bullets);
        }

        // start timer when game begins
        gameTimer.start();
    }

    // returns to gameplay and starts the next wave after shop
    public void resumeFromShop() {
        showingShop = false;
        int nextWave = (waveManager.getWaveNumber() == 0) ? 1 : waveManager.getWaveNumber() + 1;
        waveManager.startNewWave(nextWave, enemies, bullets);
    }

    // processes a shop purchase and updates player stats
    public void handleShopPurchase(int buttonIndex) {
        ShopController.ShopPurchaseResult result = shopController.handlePurchase(buttonIndex, player, scoreManager);
        if (result.getType() == ShopController.ShopPurchaseType.SCORE_PURCHASED) {
            scoreManager.addScore(result.getScoreAwarded());
        }
    }

    // accessor methods for parent container
    public Character getPlayer() {
        return player;
    }

    public int getCurrency() {
        return scoreManager.getCurrency();
    }

    public int getScore() {
        return scoreManager.getScore();
    }

    public int getHighScore() {
        return scoreManager.getHighScore();
    }

    // handles right-click to activate beam ability on all enemies of the clicked
    // type
    private void handleRightClick(int screenX, int screenY) {
        if (showingShop || gamePaused)
            return;

        long now = System.currentTimeMillis();
        if (now - lastUltimateTime < ULTIMATE_COOLDOWN_MS)
            return;

        // convert screen to world coordinates
        double worldX = screenX + camera.getX();
        double worldY = screenY + camera.getY();

        Enemy clickedEnemy = findEnemyAt(worldX, worldY);
        if (clickedEnemy == null || !clickedEnemy.isAlive())
            return;

        // find all enemies of same type
        Class<?> enemyType = clickedEnemy.getClass();
        List<Enemy> enemiesOfType = new ArrayList<>();
        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && enemy.getClass() == enemyType) {
                enemiesOfType.add(enemy);
            }
        }

        if (!enemiesOfType.isEmpty()) {
            activateUltimateAbility(enemiesOfType);
            lastUltimateTime = now;
        }
    }

    // returns the enemy at the given world position, or null if none found
    private Enemy findEnemyAt(double worldX, double worldY) {
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                double distance = Utils.distance(worldX, worldY, enemy.getX(), enemy.getY());
                if (distance <= enemy.getRadius()) {
                    return enemy;
                }
            }
        }
        return null;
    }

    // activates beam ability using TSP solver to find optimal path through enemies
    private void activateUltimateAbility(List<Enemy> targetEnemies) {
        List<double[]> points = new ArrayList<>();
        for (Enemy enemy : targetEnemies) {
            if (enemy != null && enemy.isAlive()) {
                points.add(new double[] { enemy.getX(), enemy.getY() });
            }
        }
        if (points.isEmpty())
            return;

        // solve tsp for optimal beam path
        List<double[]> path = TSPSolver.solveTSP(player.getX(), player.getY(), points);
        if (path.isEmpty() || path.size() < 2)
            return;

        gamePaused = true;
        beamAbility.activate(path, targetEnemies, particleManager);
    }

    // renders game world with camera offset then hud on top
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.translate(-camera.getX(), -camera.getY());
        drawGameWorld(g2);
        g2.translate(camera.getX(), camera.getY());
        drawHUD(g2);
    }

    // draws background, player, bullets, enemies, particles in world space
    private void drawGameWorld(Graphics2D g2) {
        drawGridBackground(g2);
        player.draw(g2);
        // OPTIMIZATION: DO NOT RENDER OFF-SCREEN ENTITIES
        for (Bullet bullet : bullets)
            if (camera.isInView(bullet.getX(), bullet.getY(), 10))
                bullet.draw(g2);
        for (Enemy enemy : enemies)
            if (enemy.isAlive() && camera.isInView(enemy.getX(), enemy.getY(), enemy.getRadius()))
                enemy.draw(g2);
        particleManager.draw(g2);
        beamAbility.draw(g2);
    }

    // draws starfield background with nebula effects and border
    private void drawGridBackground(Graphics2D g2) {
        g2.setColor(BACKGROUND_COLOR);
        g2.fillRect(0, 0, MAP_WIDTH, MAP_HEIGHT);

        // draw small stars (using static random with fixed seed for consistency)
        STAR_RANDOM_1.setSeed(12345);
        g2.setColor(STAR_COLOR_DIM);
        for (int i = 0; i < SMALL_STAR_COUNT; i++) {
            int starX = STAR_RANDOM_1.nextInt(MAP_WIDTH);
            int starY = STAR_RANDOM_1.nextInt(MAP_HEIGHT);
            int size = STAR_RANDOM_1.nextInt(3) + 1;
            g2.fillOval(starX, starY, size, size);
        }

        // draw larger, brighter stars
        STAR_RANDOM_2.setSeed(54321);
        g2.setColor(STAR_COLOR_BRIGHT);
        for (int i = 0; i < LARGE_STAR_COUNT; i++) {
            int starX = STAR_RANDOM_2.nextInt(MAP_WIDTH);
            int starY = STAR_RANDOM_2.nextInt(MAP_HEIGHT);
            g2.fillOval(starX, starY, 2, 2);
        }

        // corner nebula effects
        g2.setColor(NEBULA_PURPLE);
        g2.fillRect(0, 0, NEBULA_SIZE, NEBULA_SIZE);
        g2.setColor(NEBULA_BLUE);
        g2.fillRect(MAP_WIDTH - NEBULA_SIZE, 0, NEBULA_SIZE, NEBULA_SIZE);
        g2.setColor(NEBULA_MAGENTA);
        g2.fillRect(0, MAP_HEIGHT - NEBULA_SIZE, NEBULA_SIZE, NEBULA_SIZE);
        g2.setColor(NEBULA_INDIGO);
        g2.fillRect(MAP_WIDTH - NEBULA_SIZE, MAP_HEIGHT - NEBULA_SIZE, NEBULA_SIZE, NEBULA_SIZE);

        // border
        g2.setStroke(new BasicStroke(BORDER_WIDTH));
        g2.setColor(BORDER_COLOR);
        g2.drawRect(0, 0, MAP_WIDTH, MAP_HEIGHT);
        g2.setStroke(new BasicStroke(1));
    }

    // draws score, health, wave info in screen space
    private void drawHUD(Graphics2D g2) {
        long now = System.currentTimeMillis();
        int enemiesRemaining = waveManager.getEnemiesRemaining(enemies.size());
        String status = waveManager.getWaveStatusText();
        hudRenderer.drawHUD(g2, screenWidth, screenHeight, scoreManager.getScore(),
                scoreManager.getCurrency(), player, lastUltimateTime, now,
                waveManager.getWaveNumber(), waveManager.getWaveStartTime(),
                enemiesRemaining, status);
    }

    // updates game state and triggers repaint on timer tick
    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
    }

    // main update loop
    private void updateGame() {
        double deltaSeconds = DELTA_SECONDS;

        // update beam ability (always update, even when paused)
        // we need to track if beam was active before and after update to detect when it
        // finishes
        boolean beamWasActive = beamAbility.isActive();
        boolean beamStillActive = beamAbility.update(deltaSeconds);

        // if beam just finished, unpause game and process dead enemies
        // the beam was active last frame but isn't anymore = animation complete
        if (beamWasActive && !beamStillActive) {
            gamePaused = false;
            updateEnemies(0.0);
        }

        // pause game updates during beam (but still update beam animation)
        // this freezes enemies, bullets, and player movement while beam travels
        if (gamePaused && beamAbility.isActive()) {
            // only update camera to follow player (visual only)
            updateCamera();
            return;
        }

        if (!showingShop) {
            updatePlayer(deltaSeconds);
            updateCamera();
            updateShooting();
            updateBullets(deltaSeconds);
            updateEnemies(deltaSeconds);
            waveManager.updateSpawning(enemies);
            updateWaveProgress();
            particleManager.update(deltaSeconds);

            if (player.getHealthLeft() <= 0 && !gameOver) {
                gameOver = true;
                scoreManager.updateHighScore();
                if (gameListener != null) {
                    gameListener.onGameOver(scoreManager.getScore(), waveManager.getWaveNumber(),
                            scoreManager.getHighScore());
                }
            }
        }
    }

    // moves player based on wasd input and rotates toward mouse
    private void updatePlayer(double deltaSeconds) {
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

    // keeps camera centered on player position
    private void updateCamera() {
        camera.centerOn(player.getX(), player.getY());
    }

    // fires bullets toward mouse at player's fire rate
    private void updateShooting() {
        long now = System.currentTimeMillis();
        long fireInterval = (long) (1000 / player.getFireRate());

        if (now - lastShotTime >= fireInterval) {
            double originX = player.getX();
            double originY = player.getY();
            int targetX = inputHandler.getMouseX() + camera.getX();
            int targetY = inputHandler.getMouseY() + camera.getY();

            double[] direction = Utils.normalizeWithDefault(targetX - originX, targetY - originY, 1, 0);
            double velocityX = direction[0] * player.getBulletSpeed();
            double velocityY = direction[1] * player.getBulletSpeed();

            bullets.add(new Bullet(originX, originY, velocityX, velocityY,
                    player.getBulletSpeed(), player.getBulletDamage(), true));
            lastShotTime = now;
        }
    }

    // moves bullets and checks for collisions with enemies or player
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

    // updates enemy ai, spawning, collisions, and death effects
    private void updateEnemies(double deltaSeconds) {
        // keep track of new enemies spawned during this update
        // we can't add directly to enemies list while iterating
        List<Enemy> spawnedFromDeaths = new ArrayList<>();
        List<Enemy> spawnedFromSpawners = new ArrayList<>();

        Iterator<Enemy> iterator = enemies.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            if (!enemy.isAlive()) {
                scoreManager.awardScoreForEnemy(enemy);
                particleManager.spawnDeathEffect(enemy);
                // hexagon enemies split into 6 triangles when they die
                if (enemy instanceof HexagonEnemy)
                    spawnHexSplit((HexagonEnemy) enemy, spawnedFromDeaths);
                iterator.remove();
                continue;
            }

            enemy.update(deltaSeconds, player, bullets, MAP_WIDTH, MAP_HEIGHT);

            // spawner enemies periodically create new enemies
            if (enemy instanceof StarEnemy) {
                ((StarEnemy) enemy).trySpawn(player, spawnedFromSpawners);
            }

            // push enemies apart if they're overlapping
            // index tells the collision manager which enemy we're checking against all
            // others
            collisionManager.resolveEnemyCollisions(enemy, enemies, index);

            // if enemy touches player, deal damage and remove the enemy
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

        // add all newly spawned enemies to the main list
        enemies.addAll(spawnedFromDeaths);
        enemies.addAll(spawnedFromSpawners);
    }

    // checks if wave is complete and opens shop when all enemies are defeated
    private void updateWaveProgress() {
        if (waveManager.getWaveNumber() == 0) {
            if (enemies.isEmpty() && !showingShop) {
                scoreManager.awardWaveCurrency(0);
                showingShop = true;
                if (gameListener != null) {
                    gameListener.onShopOpen(player, scoreManager.getCurrency(), scoreManager.getScore());
                }
            }
            return;
        }

        // wave completes when spawning is done and all enemies are dead
        if (waveManager.isSpawningComplete() && enemies.isEmpty() && !showingShop) {
            scoreManager.awardWaveCurrency(waveManager.getWaveNumber());
            showingShop = true;
            if (gameListener != null) {
                gameListener.onShopOpen(player, scoreManager.getCurrency(), scoreManager.getScore());
            }
        }
    }

    // spawns 6 triangle enemies in a circle when a hexagon enemy dies
    private void spawnHexSplit(HexagonEnemy hex, List<Enemy> collector) {
        double centerX = hex.getX();
        double centerY = hex.getY();
        double spawnDistance = hex.getRadius();

        // spawn 6 triangles in a circle
        for (int i = 0; i < HEX_SPLIT_COUNT; i++) {
            double angle = i * (2 * Math.PI / HEX_SPLIT_COUNT);
            double spawnX = centerX + Math.cos(angle) * spawnDistance;
            double spawnY = centerY + Math.sin(angle) * spawnDistance;
            collector.add(new TriangleEnemy(spawnX, spawnY,
                    HEX_SPLIT_TRIANGLE_RADIUS, HEX_SPLIT_TRIANGLE_HEALTH,
                    HEX_SPLIT_TRIANGLE_DAMAGE, HEX_SPLIT_TRIANGLE_SPEED));
        }
    }
}
