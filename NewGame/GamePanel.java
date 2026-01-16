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

    private Character player;
    private final List<Bullet> bullets;
    private final List<Enemy> enemies;
    private Camera camera;
    private UIOverlay uiOverlay;

    private boolean upPressed;
    private boolean downPressed;
    private boolean leftPressed;
    private boolean rightPressed;

    private int mouseX;
    private int mouseY;
    private int hoveredButtonIndex = -1; // For menu buttons
    private int hoveredShopButtonIndex = -1; // For shop buttons

    private long lastShotTime;
    private long lastSpawnTime;
    private int waveNumber = 0; // special round 0 first
    private long waveStartTime;

    // Wave system
    private static final long WAVE_DURATION_MS = 60_000; // 60 seconds per wave
    private int enemiesToSpawnThisWave;
    private int enemiesSpawnedThisWave;
    private int nextCornerIndex = 0;
    
    // Score and currency system
    private int score = 0;
    private int currency = 0;
    private boolean showingUpgradeShop = false;
    private List<java.awt.Rectangle> shopButtonRects = new ArrayList<>();
    
    // Game state management
    private enum GameState {
        MAIN_MENU, HOW_TO_PLAY, PLAYING, GAME_OVER
    }
    private GameState gameState = GameState.MAIN_MENU;
    private int highScore = 0;
    private List<java.awt.Rectangle> menuButtonRects = new ArrayList<>();

    public GamePanel(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setBackground(Color.BLACK);
        setFocusable(true);

        player = new Character(MAP_WIDTH / 2.0, MAP_HEIGHT / 2.0);
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        camera = new Camera(screenWidth, screenHeight);
        uiOverlay = new UIOverlay(player);

        addKeyListener(this);
        addMouseMotionListener(this);
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                // Use mousePressed instead of mouseClicked for immediate response
                if (showingUpgradeShop) {
                    handleShopClick(e.getX(), e.getY());
                } else if (gameState == GameState.MAIN_MENU) {
                    handleMainMenuClick(e.getX(), e.getY());
                } else if (gameState == GameState.GAME_OVER) {
                    handleGameOverClick(e.getX(), e.getY());
                } else if (gameState == GameState.HOW_TO_PLAY) {
                    handleHowToPlayClick(e.getX(), e.getY());
                }
            }
        });

        lastShotTime = 0;

        // Start in main menu, don't setup game yet
        // setupRoundZero();

        // ~60 FPS game loop
        gameTimer = new Timer(16, this);
        gameTimer.start();
    }
    
    private void startNewGame() {
        // Reset game state
        score = 0;
        currency = 0;
        showingUpgradeShop = false;
        gameState = GameState.PLAYING;
        
        // Reset player
        player = new Character(MAP_WIDTH / 2.0, MAP_HEIGHT / 2.0);
        camera = new Camera(screenWidth, screenHeight);
        uiOverlay = new UIOverlay(player);
        
        // Clear all entities
        bullets.clear();
        enemies.clear();
        
        setupRoundZero();
    }

    private void setupRoundZero() {
        waveNumber = 0;
        waveStartTime = System.currentTimeMillis();
        lastSpawnTime = waveStartTime;

        enemies.clear();
        bullets.clear();

        enemiesSpawnedThisWave = 0;
        enemiesToSpawnThisWave = 0; // no timed spawns in round 0
        nextCornerIndex = 0;

        spawnRoundZeroEnemies();
    }

    private void spawnRoundZeroEnemies() {
        // One of each enemy type from each corner
        for (int corner = 0; corner < 4; corner++) {
            double padding = 30;
            double[] pos = positionInCorner(corner, padding);

            // Triangle (half damage: 10 -> 5)
            enemies.add(new TriangleEnemy(pos[0], pos[1], 24, 50, 5, 260));
            // Square (half damage: 15 -> 7.5)
            enemies.add(new SquareEnemy(pos[0], pos[1], 22, 60, 7.5, 320, 150));
            // Circle (half damage: 40 -> 20)
            enemies.add(new CircleEnemy(pos[0], pos[1], 28, 80, 20, 160, 180));
            // Shooter (half damage: 10 -> 5)
            enemies.add(new ShooterEnemy(pos[0], pos[1], 26, 70, 5, 220));
            // Hexagon (half damage: 20 -> 10)
            enemies.add(new HexagonEnemy(pos[0], pos[1], 30, 140, 10, 220));
            // Spawner (Star enemy)
            enemies.add(new SpawnerEnemy(pos[0], pos[1], 28, 100, 5, 200));
            // Octagon (very tanky, slow) - half damage: 15 -> 7.5
            enemies.add(new OctagonEnemy(pos[0], pos[1], 32, 200, 7.5, 150));
        }
    }

    private void startNewWave(int newWaveNumber) {
        waveNumber = newWaveNumber;
        waveStartTime = System.currentTimeMillis();
        lastSpawnTime = waveStartTime;

        enemies.clear();
        bullets.clear();

        enemiesSpawnedThisWave = 0;
        enemiesToSpawnThisWave = calculateEnemiesForWave(waveNumber);
        nextCornerIndex = 0;
        showingUpgradeShop = false;
    }

    private int calculateEnemiesForWave(int wave) {
        // Simple scaling: base 6, then +4 per wave, grows a bit faster after wave 3
        int base = 6 + (wave - 1) * 4;
        if (wave > 3) {
            base += (wave - 3) * 2;
        }
        return base;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Handle different game states
        if (gameState == GameState.MAIN_MENU) {
            drawMainMenu(g2);
        } else if (gameState == GameState.HOW_TO_PLAY) {
            drawHowToPlay(g2);
        } else if (gameState == GameState.GAME_OVER) {
            drawGameOver(g2);
        } else if (gameState == GameState.PLAYING) {
            // Apply camera offset
            g2.translate(-camera.getX(), -camera.getY());

            drawGridBackground(g2);
            drawPlayer(g2);
            drawBullets(g2);
            drawEnemies(g2);

            // Reset transform for UI overlay
            g2.translate(camera.getX(), camera.getY());

            long now = System.currentTimeMillis();
            
            // Show upgrade shop between waves
            if (showingUpgradeShop) {
                drawUpgradeShop(g2);
            } else {
                int enemiesRemaining = getEnemiesRemainingThisWave();
                String status = getWaveStatusText(now);
                uiOverlay.draw(g2, screenWidth, screenHeight, waveNumber, waveStartTime, now,
                        enemiesRemaining, status);
                // Show score
                drawScore(g2);
            }
        }
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

        // Corner spawn zones (400x400 each) tinted so you can see them
        g2.setColor(new Color(80, 80, 160, 60));
        // top-left
        g2.fillRect(0, 0, 400, 400);
        // top-right
        g2.fillRect(MAP_WIDTH - 400, 0, 400, 400);
        // bottom-left
        g2.fillRect(0, MAP_HEIGHT - 400, 400, 400);
        // bottom-right
        g2.fillRect(MAP_WIDTH - 400, MAP_HEIGHT - 400, 400, 400);

        // Map border - strong black border around the entire map
        g2.setStroke(new java.awt.BasicStroke(8));
        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, MAP_WIDTH, MAP_HEIGHT);
        g2.setStroke(new java.awt.BasicStroke(1));
    }

    private void drawPlayer(Graphics2D g2) {
        player.draw(g2);
    }

    private void drawBullets(Graphics2D g2) {
        for (Bullet bullet : bullets) {
            bullet.draw(g2);
        }
    }

    private void drawEnemies(Graphics2D g2) {
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                enemy.draw(g2);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Only update game if playing
        if (gameState == GameState.PLAYING) {
            updateGame();
            // Check for player death
            if (player.getHealthLeft() <= 0) {
                gameOver();
            }
        }
        repaint();
    }

    private void updateGame() {
        double deltaSeconds = 16 / 1000.0; // Approximate for now

        updatePlayer(deltaSeconds);
        updateCamera();
        updateShooting();
        updateBullets(deltaSeconds);
        updateEnemies(deltaSeconds);
        updateSpawning();
        updateWaveProgress();
    }
    
    private void gameOver() {
        gameState = GameState.GAME_OVER;
        // Check for new high score
        if (score > highScore) {
            highScore = score;
        }
    }

    private void updateSpawning() {
        long now = System.currentTimeMillis();
        long elapsed = now - waveStartTime;

        if (elapsed > WAVE_DURATION_MS) {
            return; // spawn window over
        }

        if (enemiesSpawnedThisWave >= enemiesToSpawnThisWave) {
            return; // already spawned full wave
        }

        long spawnIntervalMs = 2000; // spawn every 2 seconds within the wave window
        if (now - lastSpawnTime < spawnIntervalMs) {
            return;
        }
        lastSpawnTime = now;

        spawnEnemyForCurrentWave();
    }

    private void spawnEnemyForCurrentWave() {
        // Wave 1: only triangles
        if (waveNumber == 1) {
            spawnRandomTriangleEnemy();
            return;
        }

        // Wave 2: triangles and squares
        if (waveNumber == 2) {
            if (Math.random() < 0.6) {
                spawnRandomTriangleEnemy();
            } else {
                spawnRandomSquareEnemy();
            }
            return;
        }

        // Wave 3+: all enemy types with some weighting (including shooters)
        double roll = Math.random();
        if (roll < 0.4) {
            spawnRandomTriangleEnemy();
        } else if (roll < 0.7) {
            spawnRandomSquareEnemy();
        } else if (roll < 0.9) {
            spawnRandomCircleEnemy();
        } else {
            spawnRandomShooterEnemy();
        }
    }

    private void updateWaveProgress() {
        long now = System.currentTimeMillis();
        long elapsed = now - waveStartTime;

        // Special round 0: no timed spawns, just kill all enemies to go to wave 1
        if (waveNumber == 0) {
            if (enemies.isEmpty() && !showingUpgradeShop) {
                // Round 0 gives special 10 points
                currency += 10;
                showingUpgradeShop = true;
            }
            return;
        }

        boolean spawnWindowOver = elapsed > WAVE_DURATION_MS
                || enemiesSpawnedThisWave >= enemiesToSpawnThisWave;

        if (!spawnWindowOver) {
            return; // still in spawning phase for this wave
        }

        // When all enemies from this wave are dead, award currency and show upgrade shop
        if (enemies.isEmpty() && !showingUpgradeShop) {
            // Award currency based on wave number (wave 1 = 2 points, wave 2 = 4 points, etc.)
            currency += waveNumber * 2;
            showingUpgradeShop = true;
        }
    }

    private void spawnRandomTriangleEnemy() {
        double r = 24;
        double[] pos = randomEdgePosition(r);
        enemies.add(new TriangleEnemy(pos[0], pos[1], r, 50, 5, 260)); // half damage: 10 -> 5
        enemiesSpawnedThisWave++;
    }

    private void spawnRandomCircleEnemy() {
        double r = 28;
        double ffRadius = 180;
        double[] pos = randomEdgePosition(r);
        enemies.add(new CircleEnemy(pos[0], pos[1], r, 80, 20, 160, ffRadius)); // half damage: 40 -> 20
        enemiesSpawnedThisWave++;
    }

    private void spawnRandomSquareEnemy() {
        double halfSize = 22;
        double dodgeRadius = 150;
        double[] pos = randomEdgePosition(halfSize);
        enemies.add(new SquareEnemy(pos[0], pos[1], halfSize, 60, 7.5, 320, dodgeRadius)); // half damage: 15 -> 7.5
        enemiesSpawnedThisWave++;
    }

    private void spawnRandomShooterEnemy() {
        double r = 26;
        double[] pos = randomEdgePosition(r);
        enemies.add(new ShooterEnemy(pos[0], pos[1], r, 70, 5, 220)); // half damage: 10 -> 5
        enemiesSpawnedThisWave++;
    }

    private double[] randomEdgePosition(double padding) {
        // Spawn inside one of four 400x400 corner squares, with padding so enemies are fully inside.
        // Corners are used in a round-robin fashion so enemies are spread evenly.
        int corner = nextCornerIndex;
        nextCornerIndex = (nextCornerIndex + 1) % 4;
        return positionInCorner(corner, padding);
    }

    private double[] positionInCorner(int corner, double padding) {
        double x = MAP_WIDTH / 2.0;
        double y = MAP_HEIGHT / 2.0;

        switch (corner) {
            case 0: // top-left
                x = padding + Math.random() * (400 - 2 * padding);
                y = padding + Math.random() * (400 - 2 * padding);
                break;
            case 1: // top-right
                x = MAP_WIDTH - 400 + padding + Math.random() * (400 - 2 * padding);
                y = padding + Math.random() * (400 - 2 * padding);
                break;
            case 2: // bottom-left
                x = padding + Math.random() * (400 - 2 * padding);
                y = MAP_HEIGHT - 400 + padding + Math.random() * (400 - 2 * padding);
                break;
            case 3: // bottom-right
                x = MAP_WIDTH - 400 + padding + Math.random() * (400 - 2 * padding);
                y = MAP_HEIGHT - 400 + padding + Math.random() * (400 - 2 * padding);
                break;
            default:
                break;
        }

        return new double[]{x, y};
    }

    private int getEnemiesRemainingThisWave() {
        int alive = enemies.size();
        int remainingToSpawn = Math.max(0, enemiesToSpawnThisWave - enemiesSpawnedThisWave);
        return alive + remainingToSpawn;
    }

    private String getWaveStatusText(long now) {
        long elapsed = now - waveStartTime;
        boolean spawningPhase = elapsed <= WAVE_DURATION_MS
                && enemiesSpawnedThisWave < enemiesToSpawnThisWave;

        if (spawningPhase) {
            return "Spawning";
        }

        if (!enemies.isEmpty()) {
            return "Kill enemies to go to next wave";
        }

        return "Preparing next wave";
    }

    private void updatePlayer(double deltaSeconds) {
        // Don't allow player movement when in upgrade shop
        if (showingUpgradeShop) {
            return;
        }
        
        double dx = 0;
        double dy = 0;

        if (upPressed) dy -= 1;
        if (downPressed) dy += 1;
        if (leftPressed) dx -= 1;
        if (rightPressed) dx += 1;

        player.update(dx, dy, deltaSeconds, MAP_WIDTH, MAP_HEIGHT);

        // Update player angle to face mouse
        double targetX = mouseX + camera.getX();
        double targetY = mouseY + camera.getY();
        player.setAngleToward(targetX, targetY);
    }

    private void updateCamera() {
        camera.centerOn(player.getX(), player.getY());
    }

    private void updateShooting() {
        // Don't allow shooting when in upgrade shop
        if (showingUpgradeShop) {
            return;
        }
        
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
                    player.getBulletSpeed(), player.getBulletDamage(), true);
            bullets.add(bullet);
            lastShotTime = now;
        }
    }

    private void updateBullets(double deltaSeconds) {
        Iterator<Bullet> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.update(deltaSeconds);
            boolean removeBullet = false;

            // Remove if it leaves the map
            if (bullet.isOutOfBounds(0, 0, MAP_WIDTH, MAP_HEIGHT)) {
                removeBullet = true;
            } else if (bullet.isFromPlayer()) {
                // Player bullets damage enemies only
                for (Enemy enemy : enemies) {
                    if (!enemy.isAlive()) {
                        continue;
                    }
                    if (bulletHitsEnemy(bullet, enemy)) {
                        enemy.takeDamage(bullet.getDamage());
                        removeBullet = true;
                        break;
                    }
                }
            } else {
                // Enemy bullets damage player only
                if (bulletHitsPlayer(bullet, player)) {
                    player.takeDamage(bullet.getDamage());
                    removeBullet = true;
                }
            }

            if (removeBullet) {
                iterator.remove();
            }
        }

    }

    private void updateEnemies(double deltaSeconds) {
        // Collect new enemies spawned from death explosions and spawner enemies
        List<Enemy> spawnedFromDeaths = new ArrayList<>();
        List<Enemy> spawnedFromSpawners = new ArrayList<>();

        Iterator<Enemy> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            if (!enemy.isAlive()) {
                // Award score based on enemy type (hexagon gives 0 since triangles give 10 each)
                awardScoreForEnemy(enemy);
                // If a hexagon dies, spawn 6 triangle enemies from its center
                if (enemy instanceof HexagonEnemy) {
                    spawnHexSplit((HexagonEnemy) enemy, spawnedFromDeaths);
                }
                iterator.remove();
                continue;
            }

            enemy.update(deltaSeconds, player, bullets, MAP_WIDTH, MAP_HEIGHT);
            
            // Handle spawner enemy spawning
            if (enemy instanceof SpawnerEnemy) {
                SpawnerEnemy spawner = (SpawnerEnemy) enemy;
                spawner.trySpawn(player, spawnedFromSpawners);
            }
            
            // Check enemy-to-enemy collisions (prevent overlapping)
            resolveEnemyCollisions(enemy);

            // If enemy touches player: deal damage and remove enemy
            if (enemy.collidesWith(player)) {
                enemy.onCollideWithPlayer(player);
                awardScoreForEnemy(enemy);
                if (enemy instanceof HexagonEnemy) {
                    spawnHexSplit((HexagonEnemy) enemy, spawnedFromDeaths);
                }
                iterator.remove();
            }
        }

        if (!spawnedFromDeaths.isEmpty()) {
            enemies.addAll(spawnedFromDeaths);
        }
        if (!spawnedFromSpawners.isEmpty()) {
            enemies.addAll(spawnedFromSpawners);
        }
    }

    private void resolveEnemyCollisions(Enemy currentEnemy) {
        // Check collision with all other enemies and push them apart
        // Use indices to avoid double-processing pairs
        int currentIndex = enemies.indexOf(currentEnemy);
        if (currentIndex == -1) return;
        
        for (int i = currentIndex + 1; i < enemies.size(); i++) {
            Enemy other = enemies.get(i);
            if (!other.isAlive()) {
                continue;
            }
            
            double dx = currentEnemy.getX() - other.getX();
            double dy = currentEnemy.getY() - other.getY();
            double distanceSq = dx * dx + dy * dy;
            double minDistance = currentEnemy.getRadius() + other.getRadius();
            double minDistanceSq = minDistance * minDistance;
            
            // If enemies are overlapping, push them apart
            if (distanceSq < minDistanceSq && distanceSq > 0) {
                double distance = Math.sqrt(distanceSq);
                // Normalize direction
                double nx = dx / distance;
                double ny = dy / distance;
                
                // Calculate overlap amount
                double overlap = minDistance - distance;
                
                // Push both enemies apart (each moves half the overlap distance)
                double pushX = nx * overlap * 0.5;
                double pushY = ny * overlap * 0.5;
                
                // Update positions (but clamp to map bounds)
                double newX1 = currentEnemy.getX() + pushX;
                double newY1 = currentEnemy.getY() + pushY;
                double newX2 = other.getX() - pushX;
                double newY2 = other.getY() - pushY;
                
                // Clamp to map bounds for current enemy
                double minX1 = currentEnemy.getRadius();
                double maxX1 = MAP_WIDTH - currentEnemy.getRadius();
                double minY1 = currentEnemy.getRadius();
                double maxY1 = MAP_HEIGHT - currentEnemy.getRadius();
                
                if (newX1 < minX1) newX1 = minX1;
                if (newX1 > maxX1) newX1 = maxX1;
                if (newY1 < minY1) newY1 = minY1;
                if (newY1 > maxY1) newY1 = maxY1;
                
                // Clamp to map bounds for other enemy
                double minX2 = other.getRadius();
                double maxX2 = MAP_WIDTH - other.getRadius();
                double minY2 = other.getRadius();
                double maxY2 = MAP_HEIGHT - other.getRadius();
                
                if (newX2 < minX2) newX2 = minX2;
                if (newX2 > maxX2) newX2 = maxX2;
                if (newY2 < minY2) newY2 = minY2;
                if (newY2 > maxY2) newY2 = maxY2;
                
                // Apply positions using setter
                currentEnemy.setPosition(newX1, newY1);
                other.setPosition(newX2, newY2);
            }
        }
    }
    
    private void awardScore(int amount) {
        score += amount;
    }
    
    private void awardScoreForEnemy(Enemy enemy) {
        if (enemy instanceof TriangleEnemy) {
            score += 10;
        } else if (enemy instanceof CircleEnemy || enemy instanceof SquareEnemy) {
            score += 20;
        } else if (enemy instanceof ShooterEnemy) {
            score += 30;
        } else if (enemy instanceof HexagonEnemy) {
            score += 0; // Hexagon gives 0, triangles give 10 each
        } else if (enemy instanceof SpawnerEnemy) {
            score += 40; // Spawner enemy worth more points
        } else if (enemy instanceof OctagonEnemy) {
            score += 50; // Octagon enemy worth even more points (very tanky)
        }
    }
    
    private void spawnHexSplit(HexagonEnemy hex, List<Enemy> collector) {
        // Simple split: spawn 6 triangle enemies positioned around the hexagon's center.
        // All positions are computed relative to the hexagon's CENTER (x, y).
        double centerX = hex.getX();
        double centerY = hex.getY();
        double parentRadius = hex.getRadius();
        double childRadius = 14; // Smaller than regular triangles (regular = 24)
        double spawnDistance = parentRadius; // how far from center to place the triangles

        for (int i = 0; i < 6; i++) {
            double angle = i * (2 * Math.PI / 6.0);
            double x = centerX + Math.cos(angle) * spawnDistance;
            double y = centerY + Math.sin(angle) * spawnDistance;

            // Each spawned triangle behaves like a normal triangle enemy (no special explosion phase).
            collector.add(new TriangleEnemy(x, y, childRadius, 40, 5, 280)); // half damage: 10 -> 5
        }
    }

    private boolean bulletHitsEnemy(Bullet bullet, Enemy enemy) {
        double dx = bullet.getX() - enemy.getX();
        double dy = bullet.getY() - enemy.getY();
        double distanceSq = dx * dx + dy * dy;
        double radiusSum = bullet.getRadius() + enemy.getRadius();
        return distanceSq <= radiusSum * radiusSum;
    }

    private boolean bulletHitsPlayer(Bullet bullet, Character player) {
        double dx = bullet.getX() - player.getX();
        double dy = bullet.getY() - player.getY();
        double distanceSq = dx * dx + dy * dy;
        double radiusSum = bullet.getRadius() + player.getRadius();
        return distanceSq <= radiusSum * radiusSum;
    }

    private void drawScore(Graphics2D g2) {
        // Score in top-left, below health bar
        String scoreText = "Score: " + score;
        g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        java.awt.FontMetrics fm = g2.getFontMetrics();
        int x = 20;
        int y = 80; // Below health bar
        
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(x - 10, y - fm.getAscent() - 5, fm.stringWidth(scoreText) + 20, fm.getHeight() + 10, 10, 10);
        
        g2.setColor(Color.WHITE);
        g2.drawString(scoreText, x, y);
        
        // Player stats display
        int statsY = y + 30;
        String[] stats = {
            "Max Health: " + (int)player.getMaxHealth() + " (Lv " + player.getMaxHealthLevel() + ")",
            "Bullet Speed: " + (int)player.getBulletSpeed() + " (Lv " + player.getBulletSpeedLevel() + ")",
            "Fire Rate: " + String.format("%.1f", player.getFireRate()) + " (Lv " + player.getFireRateLevel() + ")",
            "Move Speed: " + (int)player.getMovementSpeed() + " (Lv " + player.getMovementSpeedLevel() + ")",
            "Bullet Damage: " + (int)player.getBulletDamage() + " (Lv " + player.getBulletDamageLevel() + ")",
            "Extra Currency: " + currency + " points"
        };
        
        g2.setFont(g2.getFont().deriveFont(java.awt.Font.PLAIN, 12f));
        fm = g2.getFontMetrics();
        for (int i = 0; i < stats.length; i++) {
            String stat = stats[i];
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRoundRect(x - 5, statsY + i * 18 - fm.getAscent() - 2, fm.stringWidth(stat) + 10, fm.getHeight() + 4, 5, 5);
            g2.setColor(new Color(200, 200, 200));
            g2.drawString(stat, x, statsY + i * 18);
        }
    }
    
    private void drawUpgradeShop(Graphics2D g2) {
        // Dark overlay
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, screenWidth, screenHeight);
        
        // Shop panel - make sure it fits on screen (screenHeight = 810)
        int panelWidth = 800;
        int panelHeight = Math.min(700, screenHeight - 100); // Leave 100px margin
        int panelX = screenWidth / 2 - panelWidth / 2;
        int panelY = (screenHeight - panelHeight) / 2; // Center vertically
        
        g2.setColor(new Color(30, 30, 40, 250));
        g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 20, 20);
        g2.setColor(Color.WHITE);
        g2.setStroke(new java.awt.BasicStroke(3));
        g2.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 20, 20);
        
        // Title
        g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 36f));
        java.awt.FontMetrics fm = g2.getFontMetrics();
        String title = "Upgrade Shop - Currency: " + currency;
        int titleX = panelX + (panelWidth - fm.stringWidth(title)) / 2;
        g2.setColor(Color.YELLOW);
        g2.drawString(title, titleX, panelY + 50);
        
        // Upgrade options with buttons and progress bars
        g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 18f));
        fm = g2.getFontMetrics();
        int yStart = panelY + 100;
        int buttonWidth = 200;
        int buttonHeight = 50;
        int buttonSpacing = 15;
        int progressBarWidth = 200;
        int progressBarHeight = 20;
        
        // Stat upgrades (with progress bars)
        String[] statNames = {
            "Max Health",
            "Bullet Speed",
            "Fire Rate",
            "Movement Speed",
            "Bullet Damage"
        };
        String[] descriptions = {
            "Increases max HP by 20",
            "Increases bullet speed by 120",
            "Increases fire rate by 0.3/s",
            "Increases movement speed by 50",
            "Increases bullet damage by 2"
        };
        int[] levels = {
            player.getMaxHealthLevel(),
            player.getBulletSpeedLevel(),
            player.getFireRateLevel(),
            player.getMovementSpeedLevel(),
            player.getBulletDamageLevel()
        };
        
        for (int i = 0; i < statNames.length; i++) {
            int y = yStart + i * (buttonHeight + buttonSpacing);
            
            // Stat name and description
            g2.setColor(Color.WHITE);
            g2.drawString(statNames[i] + " (Lv " + levels[i] + "/10)", panelX + 30, y + 20);
            g2.setFont(g2.getFont().deriveFont(java.awt.Font.PLAIN, 14f));
            g2.setColor(new Color(200, 200, 200));
            g2.drawString(descriptions[i], panelX + 30, y + 40);
            g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 18f));
            
            // Progress bar background
            int progX = panelX + 30;
            int progY = y + 50;
            g2.setColor(new Color(50, 50, 50));
            g2.fillRoundRect(progX, progY, progressBarWidth, progressBarHeight, 5, 5);
            
            // Progress bar segments (10 segments)
            int segmentWidth = progressBarWidth / 10;
            int filledSegments = levels[i];
            for (int seg = 0; seg < 10; seg++) {
                if (seg < filledSegments) {
                    g2.setColor(new Color(0, 200, 0));
                } else {
                    g2.setColor(new Color(100, 100, 100));
                }
                g2.fillRoundRect(progX + seg * segmentWidth + 2, progY + 2, 
                    segmentWidth - 4, progressBarHeight - 4, 3, 3);
            }
            
            // Upgrade button
            int btnX = panelX + 280;
            boolean canUpgrade = currency > 0 && levels[i] < 10;
            Color btnColor = canUpgrade ? new Color(0, 150, 0) : new Color(100, 100, 100);
            Color btnBorderColor = canUpgrade ? new Color(0, 200, 0) : new Color(150, 150, 150);
            
            g2.setColor(btnColor);
            g2.fillRoundRect(btnX, y, buttonWidth, buttonHeight, 10, 10);
            g2.setColor(btnBorderColor);
            g2.setStroke(new java.awt.BasicStroke(2));
            g2.drawRoundRect(btnX, y, buttonWidth, buttonHeight, 10, 10);
            
            g2.setColor(Color.WHITE);
            g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 16f));
            String btnText = canUpgrade ? "Upgrade (1 point)" : (levels[i] >= 10 ? "Max Level" : "Need Currency");
            int textX = btnX + (buttonWidth - g2.getFontMetrics().stringWidth(btnText)) / 2;
            g2.drawString(btnText, textX, y + 32);
            g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 18f));
        }
        
        // Extra options (Health and Score - no progress bars)
        int extraY = yStart + statNames.length * (buttonHeight + buttonSpacing) + 30;
        
        // Buy Health button
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 18f));
        g2.drawString("Buy Health +20 HP", panelX + 30, extraY + 20);
        g2.setFont(g2.getFont().deriveFont(java.awt.Font.PLAIN, 14f));
        g2.setColor(new Color(200, 200, 200));
        g2.drawString("Instantly heal 20 HP", panelX + 30, extraY + 40);
        
        boolean canBuyHealth = currency > 0;
        boolean isHealthHovered = (hoveredShopButtonIndex == statNames.length);
        Color healthBtnColor = canBuyHealth ? 
            (isHealthHovered ? new Color(180, 0, 0) : new Color(150, 0, 0)) : 
            new Color(100, 100, 100);
        Color healthBtnBorderColor = canBuyHealth ? 
            (isHealthHovered ? new Color(255, 0, 0) : new Color(200, 0, 0)) : 
            new Color(150, 150, 150);
        
        int healthBtnX = panelX + 280;
        g2.setColor(healthBtnColor);
        g2.fillRoundRect(healthBtnX, extraY, buttonWidth, buttonHeight, 10, 10);
        g2.setColor(healthBtnBorderColor);
        g2.setStroke(new java.awt.BasicStroke(isHealthHovered ? 3 : 2));
        g2.drawRoundRect(healthBtnX, extraY, buttonWidth, buttonHeight, 10, 10);
        
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        String healthBtnText = canBuyHealth ? "Buy (1 point)" : "Need Currency";
        int healthTextX = healthBtnX + (buttonWidth - g2.getFontMetrics().stringWidth(healthBtnText)) / 2;
        g2.drawString(healthBtnText, healthTextX, extraY + 32);
        
        // Buy Score button
        int scoreY = extraY + buttonHeight + buttonSpacing;
        g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 18f));
        g2.setColor(Color.WHITE);
        g2.drawString("Buy Score +10", panelX + 30, scoreY + 20);
        g2.setFont(g2.getFont().deriveFont(java.awt.Font.PLAIN, 14f));
        g2.setColor(new Color(200, 200, 200));
        g2.drawString("Add 10 points to your score", panelX + 30, scoreY + 40);
        
        boolean canBuyScore = currency > 0;
        boolean isScoreHovered = (hoveredShopButtonIndex == statNames.length + 1);
        Color scoreBtnColor = canBuyScore ? 
            (isScoreHovered ? new Color(0, 0, 180) : new Color(0, 0, 150)) : 
            new Color(100, 100, 100);
        Color scoreBtnBorderColor = canBuyScore ? 
            (isScoreHovered ? new Color(0, 0, 255) : new Color(0, 0, 200)) : 
            new Color(150, 150, 150);
        
        int scoreBtnX = panelX + 280;
        g2.setColor(scoreBtnColor);
        g2.fillRoundRect(scoreBtnX, scoreY, buttonWidth, buttonHeight, 10, 10);
        g2.setColor(scoreBtnBorderColor);
        g2.setStroke(new java.awt.BasicStroke(isScoreHovered ? 3 : 2));
        g2.drawRoundRect(scoreBtnX, scoreY, buttonWidth, buttonHeight, 10, 10);
        
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        String scoreBtnText = canBuyScore ? "Buy (1 point)" : "Need Currency";
        int scoreTextX = scoreBtnX + (buttonWidth - g2.getFontMetrics().stringWidth(scoreBtnText)) / 2;
        g2.drawString(scoreBtnText, scoreTextX, scoreY + 32);
        
        // Continue button at the bottom - make sure it fits on screen
        int continueButtonWidth = 350;
        int continueButtonHeight = 60;
        int continueButtonX = panelX + (panelWidth - continueButtonWidth) / 2;
        int continueButtonY = Math.min(panelY + panelHeight - continueButtonHeight - 20, screenHeight - continueButtonHeight - 20);
        
        // Store button positions for click detection FIRST
        shopButtonRects.clear();
        for (int i = 0; i < statNames.length; i++) {
            int y = yStart + i * (buttonHeight + buttonSpacing);
            shopButtonRects.add(new java.awt.Rectangle(panelX + 280, y, buttonWidth, buttonHeight));
        }
        shopButtonRects.add(new java.awt.Rectangle(healthBtnX, extraY, buttonWidth, buttonHeight));
        shopButtonRects.add(new java.awt.Rectangle(scoreBtnX, scoreY, buttonWidth, buttonHeight));
        shopButtonRects.add(new java.awt.Rectangle(continueButtonX, continueButtonY, continueButtonWidth, continueButtonHeight));
        
        // Button background - hover effect (check after shopButtonRects is populated)
        boolean isContinueHovered = (hoveredShopButtonIndex == shopButtonRects.size() - 1);
        Color continueBtnColor = isContinueHovered ? new Color(0, 180, 0) : new Color(0, 150, 0);
        Color continueBtnBorderColor = isContinueHovered ? new Color(0, 255, 0) : new Color(0, 200, 0);
        
        g2.setColor(continueBtnColor);
        g2.fillRoundRect(continueButtonX, continueButtonY, continueButtonWidth, continueButtonHeight, 10, 10);
        
        // Button border
        g2.setColor(continueBtnBorderColor);
        g2.setStroke(new java.awt.BasicStroke(isContinueHovered ? 4 : 3));
        g2.drawRoundRect(continueButtonX, continueButtonY, continueButtonWidth, continueButtonHeight, 10, 10);
        
        // Button text
        g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 24f));
        fm = g2.getFontMetrics();
        String continueText = "Continue to Next Wave";
        int continueTextX = continueButtonX + (continueButtonWidth - fm.stringWidth(continueText)) / 2;
        int continueTextY = continueButtonY + (continueButtonHeight + fm.getAscent()) / 2 - 5;
        g2.setColor(Color.WHITE);
        g2.drawString(continueText, continueTextX, continueTextY);
    }

    private void handleShopClick(int x, int y) {
        // Check which button was clicked
        for (int i = 0; i < shopButtonRects.size(); i++) {
            java.awt.Rectangle rect = shopButtonRects.get(i);
            if (rect.contains(x, y)) {
                // Continue button (last button, index 7)
                if (i == 7) {
                    showingUpgradeShop = false;
                    // After round 0, go to wave 1; otherwise go to next wave
                    int nextWave = (waveNumber == 0) ? 1 : (waveNumber + 1);
                    startNewWave(nextWave);
                    repaint();
                    return;
                }
                
                // Other buttons require currency
                if (currency <= 0) return;
                
                if (i == 0 && player.getMaxHealthLevel() < 10) {
                    // Max Health
                    player.upgradeMaxHealth();
                    currency--;
                } else if (i == 1 && player.getBulletSpeedLevel() < 10) {
                    // Bullet Speed
                    player.upgradeBulletSpeed();
                    currency--;
                } else if (i == 2 && player.getFireRateLevel() < 10) {
                    // Fire Rate
                    player.upgradeFireRate();
                    currency--;
                } else if (i == 3 && player.getMovementSpeedLevel() < 10) {
                    // Movement Speed
                    player.upgradeMovementSpeed();
                    currency--;
                } else if (i == 4 && player.getBulletDamageLevel() < 10) {
                    // Bullet Damage
                    player.upgradeBulletDamage();
                    currency--;
                } else if (i == 5) {
                    // Buy Health +20
                    player.buyHealth();
                    currency--;
                } else if (i == 6) {
                    // Buy Score +10
                    awardScore(10);
                    currency--;
                }
                repaint();
                return;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used anymore - shop uses mouse clicks
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        
        // No keyboard shortcuts for menus - everything uses buttons
        
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
        
        // Check for button hover
        if (gameState == GameState.MAIN_MENU || gameState == GameState.GAME_OVER || gameState == GameState.HOW_TO_PLAY) {
            hoveredButtonIndex = -1;
            for (int i = 0; i < menuButtonRects.size(); i++) {
                if (menuButtonRects.get(i).contains(mouseX, mouseY)) {
                    hoveredButtonIndex = i;
                    break;
                }
            }
            repaint();
        } else if (showingUpgradeShop) {
            hoveredShopButtonIndex = -1;
            for (int i = 0; i < shopButtonRects.size(); i++) {
                if (shopButtonRects.get(i).contains(mouseX, mouseY)) {
                    hoveredShopButtonIndex = i;
                    break;
                }
            }
            repaint();
        }
    }
    
    // Menu rendering methods
    private void drawMainMenu(Graphics2D g2) {
        // Dark background
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, screenWidth, screenHeight);
        
        // Title
        g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 72f));
        java.awt.FontMetrics fm = g2.getFontMetrics();
        String title = "GEOMETRY WARS";
        int titleX = (screenWidth - fm.stringWidth(title)) / 2;
        int titleY = 150;
        
        // Title glow effect
        g2.setColor(new Color(0, 200, 255, 100));
        for (int i = 0; i < 5; i++) {
            g2.drawString(title, titleX + i, titleY + i);
        }
        g2.setColor(new Color(0, 255, 255));
        g2.drawString(title, titleX, titleY);
        
        // Buttons - calculate positions to fit on screen
        int buttonWidth = 300;
        int buttonHeight = 60;
        int buttonSpacing = 30;
        String[] buttonTexts = {"Play", "How to Play", "Quit"};
        int totalButtonHeight = buttonTexts.length * buttonHeight + (buttonTexts.length - 1) * buttonSpacing;
        int startY = (screenHeight - totalButtonHeight - 100) / 2 + 100; // Leave space for title and high score
        
        menuButtonRects.clear();
        
        for (int i = 0; i < buttonTexts.length; i++) {
            int x = (screenWidth - buttonWidth) / 2;
            int y = startY + i * (buttonHeight + buttonSpacing);
            
            // Make sure button doesn't go below screen
            if (y + buttonHeight > screenHeight - 80) {
                break; // Stop if we're running out of space
            }
            
            // Button background - change color on hover
            boolean isHovered = (hoveredButtonIndex == i);
            Color bgColor = isHovered ? new Color(50, 50, 70, 220) : new Color(30, 30, 50, 200);
            Color borderColor = isHovered ? new Color(100, 255, 255) : new Color(0, 200, 255);
            
            g2.setColor(bgColor);
            g2.fillRoundRect(x, y, buttonWidth, buttonHeight, 15, 15);
            
            // Button border
            g2.setColor(borderColor);
            g2.setStroke(new java.awt.BasicStroke(isHovered ? 4 : 3));
            g2.drawRoundRect(x, y, buttonWidth, buttonHeight, 15, 15);
            
            // Button text
            g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 32f));
            fm = g2.getFontMetrics();
            int textX = x + (buttonWidth - fm.stringWidth(buttonTexts[i])) / 2;
            int textY = y + (buttonHeight + fm.getAscent()) / 2 - 5;
            g2.setColor(Color.WHITE);
            g2.drawString(buttonTexts[i], textX, textY);
            
            menuButtonRects.add(new java.awt.Rectangle(x, y, buttonWidth, buttonHeight));
        }
        
        // High score display - place it above buttons or below if there's space
        if (highScore > 0) {
            g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 24f));
            fm = g2.getFontMetrics();
            String highScoreText = "High Score: " + highScore;
            int hsX = (screenWidth - fm.stringWidth(highScoreText)) / 2;
            int hsY = startY + buttonTexts.length * (buttonHeight + buttonSpacing) + 40;
            
            // Make sure high score doesn't go below screen
            if (hsY + fm.getHeight() <= screenHeight - 20) {
                g2.setColor(Color.YELLOW);
                g2.drawString(highScoreText, hsX, hsY);
            }
        }
    }
    
    private void drawHowToPlay(Graphics2D g2) {
        // Dark background
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, screenWidth, screenHeight);
        
        // Title
        g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 48f));
        java.awt.FontMetrics fm = g2.getFontMetrics();
        String title = "How to Play";
        int titleX = (screenWidth - fm.stringWidth(title)) / 2;
        g2.setColor(new Color(0, 255, 255));
        g2.drawString(title, titleX, 60);
        
        // Instructions - calculate to fit on screen
        g2.setFont(g2.getFont().deriveFont(java.awt.Font.PLAIN, 18f));
        fm = g2.getFontMetrics();
        String[] instructions = {
            "CONTROLS:",
            "• WASD - Move character",
            "• Mouse - Aim and shoot automatically",
            "",
            "OBJECTIVE:",
            "• Survive waves of enemies",
            "• Clear all enemies to advance to the next wave",
            "• Earn currency to upgrade your character",
            "",
            "ENEMIES:",
            "• Triangle - Chases you",
            "• Square - Chases you and dodges bullets",
            "• Circle - Explodes when you're in its force field",
            "• Pentagon - Shoots at you from distance",
            "• Hexagon - Splits into 6 triangles when destroyed",
            "",
            "UPGRADES:",
            "• Max Health - Increase your HP",
            "• Bullet Speed - Faster projectiles",
            "• Fire Rate - Shoot more frequently",
            "• Movement Speed - Move faster",
            "• Bullet Damage - More damage per shot"
        };
        
        int y = 120;
        int lineHeight = fm.getHeight() + 5;
        int maxY = screenHeight - 100; // Leave space for back button
        
        for (String line : instructions) {
            // Skip if we're running out of space
            if (y + lineHeight > maxY) {
                break;
            }
            
            if (line.startsWith("CONTROLS:") || line.startsWith("OBJECTIVE:") || 
                line.startsWith("ENEMIES:") || line.startsWith("UPGRADES:")) {
                g2.setColor(new Color(0, 255, 255));
                g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 20f));
            } else {
                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(java.awt.Font.PLAIN, 16f));
            }
            fm = g2.getFontMetrics();
            int x = (screenWidth - fm.stringWidth(line)) / 2;
            g2.drawString(line, x, y);
            y += (line.isEmpty() ? lineHeight / 2 : lineHeight);
        }
        
        // Back button at the bottom
        int buttonWidth = 300;
        int buttonHeight = 60;
        int buttonX = (screenWidth - buttonWidth) / 2;
        int buttonY = screenHeight - buttonHeight - 20; // 20px from bottom
        
        menuButtonRects.clear();
        
        // Button background - hover effect
        boolean isBackHovered = (hoveredButtonIndex == 0);
        Color backBgColor = isBackHovered ? new Color(50, 50, 70, 220) : new Color(30, 30, 50, 200);
        Color backBorderColor = isBackHovered ? new Color(100, 255, 255) : new Color(0, 200, 255);
        
        g2.setColor(backBgColor);
        g2.fillRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 15, 15);
        
        // Button border
        g2.setColor(backBorderColor);
        g2.setStroke(new java.awt.BasicStroke(isBackHovered ? 4 : 3));
        g2.drawRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 15, 15);
        
        // Button text
        g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 28f));
        fm = g2.getFontMetrics();
        String backText = "Back to Main Menu";
        int textX = buttonX + (buttonWidth - fm.stringWidth(backText)) / 2;
        int textY = buttonY + (buttonHeight + fm.getAscent()) / 2 - 5;
        g2.setColor(Color.WHITE);
        g2.drawString(backText, textX, textY);
        
        menuButtonRects.add(new java.awt.Rectangle(buttonX, buttonY, buttonWidth, buttonHeight));
    }
    
    private void drawGameOver(Graphics2D g2) {
        // Dark overlay
        g2.setColor(new Color(0, 0, 0, 230));
        g2.fillRect(0, 0, screenWidth, screenHeight);
        
        // Game Over text
        g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 64f));
        java.awt.FontMetrics fm = g2.getFontMetrics();
        String gameOverText = "GAME OVER";
        int goX = (screenWidth - fm.stringWidth(gameOverText)) / 2;
        g2.setColor(Color.RED);
        g2.drawString(gameOverText, goX, 150);
        
        // Final score
        g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 36f));
        fm = g2.getFontMetrics();
        String scoreText = "Final Score: " + score;
        int scoreX = (screenWidth - fm.stringWidth(scoreText)) / 2;
        g2.setColor(Color.WHITE);
        g2.drawString(scoreText, scoreX, 250);
        
        // Wave reached
        String waveText = "Wave Reached: " + waveNumber;
        int waveX = (screenWidth - fm.stringWidth(waveText)) / 2;
        g2.drawString(waveText, waveX, 310);
        
        // New high score message
        if (score > 0 && score == highScore) {
            g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 32f));
            fm = g2.getFontMetrics();
            String newHighScoreText = "NEW HIGH SCORE!";
            int nhsX = (screenWidth - fm.stringWidth(newHighScoreText)) / 2;
            g2.setColor(Color.YELLOW);
            g2.drawString(newHighScoreText, nhsX, 380);
        }
        
        // High score display
        if (highScore > 0) {
            g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 28f));
            fm = g2.getFontMetrics();
            String highScoreText = "High Score: " + highScore;
            int hsX = (screenWidth - fm.stringWidth(highScoreText)) / 2;
            g2.setColor(Color.YELLOW);
            g2.drawString(highScoreText, hsX, 440);
        }
        
        // Buttons - make sure they fit on screen
        int buttonWidth = 300;
        int buttonHeight = 60;
        int buttonSpacing = 30;
        String[] buttonTexts = {"Return to Main Menu", "Play Again"};
        int totalButtonHeight = buttonTexts.length * buttonHeight + (buttonTexts.length - 1) * buttonSpacing;
        int startY = Math.min(520, screenHeight - totalButtonHeight - 40); // Leave 40px margin from bottom
        
        menuButtonRects.clear();
        
        for (int i = 0; i < buttonTexts.length; i++) {
            int x = (screenWidth - buttonWidth) / 2;
            int y = startY + i * (buttonHeight + buttonSpacing);
            
            // Make sure button doesn't go below screen
            if (y + buttonHeight > screenHeight - 20) {
                break;
            }
            
            // Button background - hover effect
            boolean isHovered = (hoveredButtonIndex == i);
            Color bgColor = isHovered ? new Color(50, 50, 70, 220) : new Color(30, 30, 50, 200);
            Color borderColor = isHovered ? new Color(100, 255, 255) : new Color(0, 200, 255);
            
            g2.setColor(bgColor);
            g2.fillRoundRect(x, y, buttonWidth, buttonHeight, 15, 15);
            
            // Button border
            g2.setColor(borderColor);
            g2.setStroke(new java.awt.BasicStroke(isHovered ? 4 : 3));
            g2.drawRoundRect(x, y, buttonWidth, buttonHeight, 15, 15);
            
            // Button text
            g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 24f));
            fm = g2.getFontMetrics();
            int textX = x + (buttonWidth - fm.stringWidth(buttonTexts[i])) / 2;
            int textY = y + (buttonHeight + fm.getAscent()) / 2 - 5;
            g2.setColor(Color.WHITE);
            g2.drawString(buttonTexts[i], textX, textY);
            
            menuButtonRects.add(new java.awt.Rectangle(x, y, buttonWidth, buttonHeight));
        }
    }
    
    // Menu click handlers
    private void handleMainMenuClick(int x, int y) {
        if (menuButtonRects.size() >= 3) {
            // Play button
            if (menuButtonRects.get(0).contains(x, y)) {
                startNewGame();
            }
            // How to Play button
            else if (menuButtonRects.get(1).contains(x, y)) {
                gameState = GameState.HOW_TO_PLAY;
            }
            // Quit button
            else if (menuButtonRects.get(2).contains(x, y)) {
                System.exit(0);
            }
        }
    }
    
    private void handleGameOverClick(int x, int y) {
        if (menuButtonRects.size() >= 2) {
            // Return to Main Menu button
            if (menuButtonRects.get(0).contains(x, y)) {
                gameState = GameState.MAIN_MENU;
            }
            // Play Again button
            else if (menuButtonRects.get(1).contains(x, y)) {
                startNewGame();
            }
        }
    }
    
    private void handleHowToPlayClick(int x, int y) {
        // Back button click
        if (menuButtonRects.size() > 0 && menuButtonRects.get(0).contains(x, y)) {
            gameState = GameState.MAIN_MENU;
        }
    }
}


