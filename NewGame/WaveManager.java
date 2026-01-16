import java.util.List;

public class WaveManager {

    private static final long WAVE_DURATION_MS = 60_000;

    private final int mapWidth;
    private final int mapHeight;

    private int waveNumber = 0;
    private long waveStartTime;
    private long lastSpawnTime;
    private int enemiesToSpawnThisWave;
    private int enemiesSpawnedThisWave;
    private int nextCornerIndex = 0;

    public WaveManager(int mapWidth, int mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    public void setupRoundZero(List<Enemy> enemies, List<Bullet> bullets) {
        waveNumber = 0;
        waveStartTime = System.currentTimeMillis();
        lastSpawnTime = waveStartTime;

        enemies.clear();
        bullets.clear();

        enemiesSpawnedThisWave = 0;
        enemiesToSpawnThisWave = 0;
        nextCornerIndex = 0;

        spawnRoundZeroEnemies(enemies);
    }

    private void spawnRoundZeroEnemies(List<Enemy> enemies) {
        for (int corner = 0; corner < 4; corner++) {
            double padding = 30;
            double[] pos = positionInCorner(corner, padding);

            enemies.add(new TriangleEnemy(pos[0], pos[1], 24, 50, 5, 260));
            enemies.add(new SquareEnemy(pos[0], pos[1], 22, 60, 7.5, 320, 150));
            enemies.add(new CircleEnemy(pos[0], pos[1], 28, 80, 20, 160, 180));
            enemies.add(new ShooterEnemy(pos[0], pos[1], 26, 70, 5, 220));
            enemies.add(new HexagonEnemy(pos[0], pos[1], 30, 140, 10, 220));
            enemies.add(new SpawnerEnemy(pos[0], pos[1], 28, 100, 5, 200));
            enemies.add(new OctagonEnemy(pos[0], pos[1], 32, 200, 7.5, 150));
        }
    }

    public void startNewWave(int newWaveNumber, List<Enemy> enemies, List<Bullet> bullets) {
        waveNumber = newWaveNumber;
        waveStartTime = System.currentTimeMillis();
        lastSpawnTime = waveStartTime;

        enemies.clear();
        bullets.clear();

        enemiesSpawnedThisWave = 0;
        enemiesToSpawnThisWave = calculateEnemiesForWave(waveNumber);
        nextCornerIndex = 0;
    }

    private int calculateEnemiesForWave(int wave) {
        int base = 6 + (wave - 1) * 4;
        if (wave > 3) {
            base += (wave - 3) * 2;
        }
        return base;
    }

    public void updateSpawning(List<Enemy> enemies) {
        long now = System.currentTimeMillis();
        long elapsed = now - waveStartTime;

        if (elapsed > WAVE_DURATION_MS) return;
        if (enemiesSpawnedThisWave >= enemiesToSpawnThisWave) return;

        long spawnIntervalMs = 2000;
        if (now - lastSpawnTime < spawnIntervalMs) return;
        lastSpawnTime = now;

        spawnEnemyForCurrentWave(enemies);
    }

    private void spawnEnemyForCurrentWave(List<Enemy> enemies) {
        if (waveNumber == 1) {
            spawnRandomTriangleEnemy(enemies);
            return;
        }

        if (waveNumber == 2) {
            if (Math.random() < 0.6) {
                spawnRandomTriangleEnemy(enemies);
            } else {
                spawnRandomSquareEnemy(enemies);
            }
            return;
        }

        double roll = Math.random();
        if (roll < 0.4) {
            spawnRandomTriangleEnemy(enemies);
        } else if (roll < 0.7) {
            spawnRandomSquareEnemy(enemies);
        } else if (roll < 0.9) {
            spawnRandomCircleEnemy(enemies);
        } else {
            spawnRandomShooterEnemy(enemies);
        }
    }

    private void spawnRandomTriangleEnemy(List<Enemy> enemies) {
        double r = 24;
        double[] pos = randomEdgePosition(r);
        enemies.add(new TriangleEnemy(pos[0], pos[1], r, 50, 5, 260));
        enemiesSpawnedThisWave++;
    }

    private void spawnRandomCircleEnemy(List<Enemy> enemies) {
        double r = 28;
        double ffRadius = 180;
        double[] pos = randomEdgePosition(r);
        enemies.add(new CircleEnemy(pos[0], pos[1], r, 80, 20, 160, ffRadius));
        enemiesSpawnedThisWave++;
    }

    private void spawnRandomSquareEnemy(List<Enemy> enemies) {
        double halfSize = 22;
        double dodgeRadius = 150;
        double[] pos = randomEdgePosition(halfSize);
        enemies.add(new SquareEnemy(pos[0], pos[1], halfSize, 60, 7.5, 320, dodgeRadius));
        enemiesSpawnedThisWave++;
    }

    private void spawnRandomShooterEnemy(List<Enemy> enemies) {
        double r = 26;
        double[] pos = randomEdgePosition(r);
        enemies.add(new ShooterEnemy(pos[0], pos[1], r, 70, 5, 220));
        enemiesSpawnedThisWave++;
    }

    private double[] randomEdgePosition(double padding) {
        int corner = nextCornerIndex;
        nextCornerIndex = (nextCornerIndex + 1) % 4;
        return positionInCorner(corner, padding);
    }

    private double[] positionInCorner(int corner, double padding) {
        double x = mapWidth / 2.0;
        double y = mapHeight / 2.0;

        switch (corner) {
            case 0:
                x = padding + Math.random() * (400 - 2 * padding);
                y = padding + Math.random() * (400 - 2 * padding);
                break;
            case 1:
                x = mapWidth - 400 + padding + Math.random() * (400 - 2 * padding);
                y = padding + Math.random() * (400 - 2 * padding);
                break;
            case 2:
                x = padding + Math.random() * (400 - 2 * padding);
                y = mapHeight - 400 + padding + Math.random() * (400 - 2 * padding);
                break;
            case 3:
                x = mapWidth - 400 + padding + Math.random() * (400 - 2 * padding);
                y = mapHeight - 400 + padding + Math.random() * (400 - 2 * padding);
                break;
        }

        return new double[]{x, y};
    }

    public boolean isSpawningComplete() {
        long elapsed = System.currentTimeMillis() - waveStartTime;
        return elapsed > WAVE_DURATION_MS || enemiesSpawnedThisWave >= enemiesToSpawnThisWave;
    }

    public int getEnemiesRemaining(int aliveCount) {
        int remainingToSpawn = Math.max(0, enemiesToSpawnThisWave - enemiesSpawnedThisWave);
        return aliveCount + remainingToSpawn;
    }

    public String getWaveStatusText() {
        long elapsed = System.currentTimeMillis() - waveStartTime;
        boolean spawningPhase = elapsed <= WAVE_DURATION_MS && enemiesSpawnedThisWave < enemiesToSpawnThisWave;
        if (spawningPhase) return "Spawning";
        return "Kill enemies to go to next wave";
    }

    public int getWaveNumber() { return waveNumber; }
    public long getWaveStartTime() { return waveStartTime; }
}
