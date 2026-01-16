import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel implements ActionListener {

    // DEV MODE: Set to true to enable round 0 for testing all enemy types
    private static final boolean DEV_MODE = false;

    public static final int MAP_WIDTH = 2000;
    public static final int MAP_HEIGHT = 2000;

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

    // Game state
    private enum GameState {
        MAIN_MENU, HOW_TO_PLAY, PLAYING, GAME_OVER
    }

    private GameState gameState = GameState.MAIN_MENU;
    private int score = 0;
    private int currency = 0;
    private int highScore = 0;
    private boolean showingUpgradeShop = false;
    private long lastShotTime;
    
    // Ultimate ability system
    private final BeamAbility beamAbility;
    private long lastUltimateTime = 0;
    private static final long ULTIMATE_COOLDOWN_MS = 10_000; // 10 seconds
    private boolean isGamePaused = false;

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
        if (showingUpgradeShop) {
            handleShopClick(x, y);
        } else if (gameState == GameState.MAIN_MENU) {
            handleMainMenuClick(x, y);
        } else if (gameState == GameState.GAME_OVER) {
            handleGameOverClick(x, y);
        } else if (gameState == GameState.HOW_TO_PLAY) {
            handleHowToPlayClick(x, y);
        }
    }
    
    private void handleRightClick(int screenX, int screenY) {
        if (gameState != GameState.PLAYING || showingUpgradeShop || isGamePaused) {
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
            if (!enemy.isAlive()) continue;
            
            double dx = worldX - enemy.getX();
            double dy = worldY - enemy.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            
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
        isGamePaused = true;
        
        // Activate beam (pass particleManager for explosions)
        beamAbility.activate(path, targetEnemies, particleManager);
    }

    private void startNewGame() {
        score = 0;
        currency = 0;
        showingUpgradeShop = false;
        gameState = GameState.PLAYING;

        player = new Character(MAP_WIDTH / 2.0, MAP_HEIGHT / 2.0);
        camera = new Camera(screenWidth, screenHeight);
        uiOverlay = new UIOverlay(player);

        bullets.clear();
        enemies.clear();
        particleManager.clear();
        beamAbility.deactivate();
        isGamePaused = false;
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

        if (gameState == GameState.MAIN_MENU) {
            menuRenderer.drawMainMenu(g2, highScore);
        } else if (gameState == GameState.HOW_TO_PLAY) {
            menuRenderer.drawHowToPlay(g2);
        } else if (gameState == GameState.GAME_OVER) {
            menuRenderer.drawGameOver(g2, score, waveManager.getWaveNumber(), highScore);
        } else if (gameState == GameState.PLAYING) {
            g2.translate(-camera.getX(), -camera.getY());
            drawGameWorld(g2);
            g2.translate(camera.getX(), camera.getY());

            if (showingUpgradeShop) {
                menuRenderer.drawUpgradeShop(g2, player, currency, score);
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
        // Space background - dark blue/purple gradient
        g2.setColor(new Color(10, 10, 30));
        g2.fillRect(0, 0, MAP_WIDTH, MAP_HEIGHT);

        // Add stars (small white dots)
        g2.setColor(new Color(255, 255, 255, 200));
        java.util.Random rand = new java.util.Random(12345); // Fixed seed for consistent star positions
        for (int i = 0; i < 300; i++) {
            int x = rand.nextInt(MAP_WIDTH);
            int y = rand.nextInt(MAP_HEIGHT);
            int size = rand.nextInt(3) + 1;
            g2.fillOval(x, y, size, size);
        }

        // Add some larger, brighter stars
        g2.setColor(new Color(255, 255, 255, 255));
        rand = new java.util.Random(54321);
        for (int i = 0; i < 50; i++) {
            int x = rand.nextInt(MAP_WIDTH);
            int y = rand.nextInt(MAP_HEIGHT);
            g2.fillOval(x, y, 2, 2);
        }

        // Corner nebula effects (purple/blue haze)
        g2.setColor(new Color(80, 60, 140, 40));
        g2.fillRect(0, 0, 400, 400);
        g2.setColor(new Color(60, 80, 160, 40));
        g2.fillRect(MAP_WIDTH - 400, 0, 400, 400);
        g2.setColor(new Color(100, 60, 120, 40));
        g2.fillRect(0, MAP_HEIGHT - 400, 400, 400);
        g2.setColor(new Color(70, 70, 150, 40));
        g2.fillRect(MAP_WIDTH - 400, MAP_HEIGHT - 400, 400, 400);

        // Border
        g2.setStroke(new java.awt.BasicStroke(8));
        g2.setColor(new Color(60, 60, 100));
        g2.drawRect(0, 0, MAP_WIDTH, MAP_HEIGHT);
        g2.setStroke(new java.awt.BasicStroke(1));
    }

    private void drawHUD(Graphics2D g2) {
        long now = System.currentTimeMillis();
        int enemiesRemaining = waveManager.getEnemiesRemaining(enemies.size());
        String status = waveManager.getWaveStatusText();
        uiOverlay.draw(g2, screenWidth, screenHeight, waveManager.getWaveNumber(),
                waveManager.getWaveStartTime(), now, enemiesRemaining, status);
        drawScore(g2);
        drawBeamCooldown(g2, now);
    }
    
    private void drawBeamCooldown(Graphics2D g2, long currentTime) {
        long timeSinceLastUltimate = currentTime - lastUltimateTime;
        double cooldownProgress = Math.min(1.0, (double)timeSinceLastUltimate / ULTIMATE_COOLDOWN_MS);
        
        int barWidth = 200;
        int barHeight = 20;
        int x = screenWidth - barWidth - 20;
        int y = screenHeight - barHeight - 20;
        
        // Background
        g2.setColor(new Color(40, 40, 40, 220));
        g2.fillRoundRect(x - 3, y - 3, barWidth + 6, barHeight + 6, 8, 8);
        
        // Cooldown background
        g2.setColor(new Color(60, 0, 0));
        g2.fillRoundRect(x, y, barWidth, barHeight, 6, 6);
        
        // Progress fill
        int filledWidth = (int)(barWidth * cooldownProgress);
        if (filledWidth > 0) {
            Color fillColor = cooldownProgress >= 1.0 ? 
                new Color(0, 200, 0) : new Color(255, 150, 0);
            g2.setColor(fillColor);
            g2.fillRoundRect(x, y, filledWidth, barHeight, 6, 6);
        }
        
        // Border
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(x, y, barWidth, barHeight, 6, 6);
        
        // Text
        String text = cooldownProgress >= 1.0 ? "BEAM READY" : 
                      String.format("BEAM: %.1fs", (ULTIMATE_COOLDOWN_MS - timeSinceLastUltimate) / 1000.0);
        g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 12f));
        java.awt.FontMetrics fm = g2.getFontMetrics();
        int textX = x + (barWidth - fm.stringWidth(text)) / 2;
        int textY = y + (barHeight + fm.getAscent()) / 2 - 2;
        
        g2.setColor(Color.BLACK);
        g2.drawString(text, textX + 1, textY + 1);
        g2.setColor(Color.WHITE);
        g2.drawString(text, textX, textY);
    }

    private void drawScore(Graphics2D g2) {
        g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        java.awt.FontMetrics fm = g2.getFontMetrics();
        String scoreText = "Score: " + score;
        int x = 20, y = 70;

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(x - 10, y - fm.getAscent() - 5, fm.stringWidth(scoreText) + 20, fm.getHeight() + 10, 10, 10);
        g2.setColor(Color.WHITE);
        g2.drawString(scoreText, x, y);

        int statsY = y + 40;
        String[] stats = {
                "Currency: " + currency + " points",
                "Max Health: " + (int) player.getMaxHealth() + " (Lv " + player.getMaxHealthLevel() + ")",
                "Bullet Speed: Lv " + player.getBulletSpeedLevel(),
                "Fire Rate: Lv " + player.getFireRateLevel(),
                "Movement Speed: Lv " + player.getMovementSpeedLevel(),
                "Bullet Damage: Lv " + player.getBulletDamageLevel()
        };

        g2.setFont(g2.getFont().deriveFont(java.awt.Font.PLAIN, 12f));
        fm = g2.getFontMetrics();
        for (int i = 0; i < stats.length; i++) {
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRoundRect(x - 10, statsY + i * 28 - fm.getAscent() - 2, fm.stringWidth(stats[i]) + 20,
                    fm.getHeight() + 4, 5, 5);
            g2.setColor(new Color(200, 200, 200));
            g2.drawString(stats[i], x, statsY + i * 28);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Update hover state for menus
        boolean isMenu = gameState == GameState.MAIN_MENU || gameState == GameState.GAME_OVER
                || gameState == GameState.HOW_TO_PLAY;
        menuRenderer.updateHover(inputHandler.getMouseX(), inputHandler.getMouseY(), isMenu, showingUpgradeShop);

        if (gameState == GameState.PLAYING) {
            updateGame();
            if (player.getHealthLeft() <= 0)
                gameOver();
        }
        repaint();
    }

    private void updateGame() {
        double deltaSeconds = 16 / 1000.0;
        
        // Update beam ability (always update, even when paused)
        boolean beamWasActive = beamAbility.isActive();
        boolean beamStillActive = beamAbility.update(deltaSeconds);
        
        // If beam just finished, unpause game and process dead enemies
        if (beamWasActive && !beamStillActive) {
            isGamePaused = false;
            // Process any enemies that were killed by the beam
            updateEnemies(0.0); // Small update to clean up dead enemies
        }
        
        // Pause game updates during beam (but still update beam animation)
        if (isGamePaused && beamAbility.isActive()) {
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
        if (showingUpgradeShop)
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
        if (showingUpgradeShop)
            return;

        long now = System.currentTimeMillis();
        long fireInterval = (long) (1000 / player.getFireRate());

        if (now - lastShotTime >= fireInterval) {
            double originX = player.getX();
            double originY = player.getY();
            int targetX = inputHandler.getMouseX() + camera.getX();
            int targetY = inputHandler.getMouseY() + camera.getY();

            double dx = targetX - originX;
            double dy = targetY - originY;
            double len = Math.sqrt(dx * dx + dy * dy);
            if (len == 0) {
                dx = 1;
                dy = 0;
                len = 1;
            }

            double vx = (dx / len) * player.getBulletSpeed();
            double vy = (dy / len) * player.getBulletSpeed();

            bullets.add(new Bullet(originX, originY, vx, vy, player.getBulletSpeed(), player.getBulletDamage(), true));
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
                awardScoreForEnemy(enemy);
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
                awardScoreForEnemy(enemy);
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
            if (enemies.isEmpty() && !showingUpgradeShop) {
                currency += 10;
                showingUpgradeShop = true;
            }
            return;
        }

        if (!waveManager.isSpawningComplete())
            return;

        if (enemies.isEmpty() && !showingUpgradeShop) {
            currency += waveManager.getWaveNumber() * 2;
            showingUpgradeShop = true;
        }
    }

    private void awardScoreForEnemy(Enemy enemy) {
        if (enemy instanceof TriangleEnemy)
            score += 10;
        else if (enemy instanceof CircleEnemy || enemy instanceof SquareEnemy)
            score += 20;
        else if (enemy instanceof ShooterEnemy)
            score += 30;
        else if (enemy instanceof SpawnerEnemy)
            score += 40;
        else if (enemy instanceof OctagonEnemy)
            score += 50;
    }

    private void spawnHexSplit(HexagonEnemy hex, List<Enemy> collector) {
        double centerX = hex.getX(), centerY = hex.getY();
        double spawnDistance = hex.getRadius();
        for (int i = 0; i < 6; i++) {
            double angle = i * (2 * Math.PI / 6.0);
            double x = centerX + Math.cos(angle) * spawnDistance;
            double y = centerY + Math.sin(angle) * spawnDistance;
            collector.add(new TriangleEnemy(x, y, 14, 40, 5, 280));
        }
    }

    private void gameOver() {
        gameState = GameState.GAME_OVER;
        if (score > highScore)
            highScore = score;
    }

    // Click handlers
    private void handleMainMenuClick(int x, int y) {
        int btn = menuRenderer.getClickedMenuButton(x, y);
        if (btn == 0)
            startNewGame();
        else if (btn == 1)
            gameState = GameState.HOW_TO_PLAY;
        else if (btn == 2)
            System.exit(0);
    }

    private void handleGameOverClick(int x, int y) {
        int btn = menuRenderer.getClickedMenuButton(x, y);
        if (btn == 0)
            gameState = GameState.MAIN_MENU;
        else if (btn == 1)
            startNewGame();
    }

    private void handleHowToPlayClick(int x, int y) {
        if (menuRenderer.getClickedMenuButton(x, y) == 0)
            gameState = GameState.MAIN_MENU;
    }

    private void handleShopClick(int x, int y) {
        int btn = menuRenderer.getClickedShopButton(x, y);
        if (btn == -1)
            return;

        if (btn == 7) { // Continue button
            showingUpgradeShop = false;
            int nextWave = (waveManager.getWaveNumber() == 0) ? 1 : waveManager.getWaveNumber() + 1;
            waveManager.startNewWave(nextWave, enemies, bullets);
            return;
        }

        if (currency <= 0)
            return;

        if (btn == 0 && player.getMaxHealthLevel() < 10) {
            player.upgradeMaxHealth();
            currency--;
        } else if (btn == 1 && player.getBulletSpeedLevel() < 10) {
            player.upgradeBulletSpeed();
            currency--;
        } else if (btn == 2 && player.getFireRateLevel() < 10) {
            player.upgradeFireRate();
            currency--;
        } else if (btn == 3 && player.getMovementSpeedLevel() < 10) {
            player.upgradeMovementSpeed();
            currency--;
        } else if (btn == 4 && player.getBulletDamageLevel() < 10) {
            player.upgradeBulletDamage();
            currency--;
        } else if (btn == 5) {
            player.buyHealth();
            currency--;
        } else if (btn == 6) {
            score += 10;
            currency--;
        }
    }
}
