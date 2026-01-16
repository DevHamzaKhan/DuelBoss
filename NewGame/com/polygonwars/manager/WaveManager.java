package com.polygonwars.manager;

import com.polygonwars.enemy.Enemy;
import com.polygonwars.enemy.TriangleEnemy;
import com.polygonwars.enemy.CircleEnemy;
import com.polygonwars.enemy.SquareEnemy;
import com.polygonwars.enemy.ShooterEnemy;
import com.polygonwars.enemy.HexagonEnemy;
import com.polygonwars.enemy.OctagonEnemy;
import com.polygonwars.enemy.SpawnerEnemy;
import com.polygonwars.entity.Bullet;

import java.util.List;
import java.util.Random;

public class WaveManager {

    private static final long WAVE_DURATION_MS = 60_000;
    private static final long SPAWN_INTERVAL_MS = 2000;
    private static final int CORNER_REGION_SIZE = 400;
    private static final int NUM_CORNERS = 4;

    // Round zero constants
    private static final int ROUND_ZERO_ENEMIES_PER_CORNER = 4;
    private static final double ROUND_ZERO_CLUSTER_RADIUS = 40;
    private static final double OCTAGON_RADIUS = 28;
    private static final double OCTAGON_HEALTH = 100;
    private static final double OCTAGON_DAMAGE = 5;
    private static final double OCTAGON_SPEED = 200;

    // Enemy spawn constants
    private static final double TRIANGLE_RADIUS = 24;
    private static final double TRIANGLE_HEALTH = 50;
    private static final double TRIANGLE_DAMAGE = 5;
    private static final double TRIANGLE_SPEED = 260;

    private static final double CIRCLE_RADIUS = 28;
    private static final double CIRCLE_FORCE_FIELD_RADIUS = 180;
    private static final double CIRCLE_HEALTH = 80;
    private static final double CIRCLE_DAMAGE = 20;
    private static final double CIRCLE_SPEED = 160;

    private static final double SQUARE_HALF_SIZE = 22;
    private static final double SQUARE_DODGE_RADIUS = 150;
    private static final double SQUARE_HEALTH = 60;
    private static final double SQUARE_DAMAGE = 7.5;
    private static final double SQUARE_SPEED = 320;

    private static final double SHOOTER_RADIUS = 26;
    private static final double SHOOTER_HEALTH = 70;
    private static final double SHOOTER_DAMAGE = 5;
    private static final double SHOOTER_SPEED = 220;

    // Spawn probabilities for wave 2
    private static final double WAVE2_TRIANGLE_CHANCE = 0.6;

    // Spawn probabilities for wave 3+
    private static final double TRIANGLE_SPAWN_CHANCE = 0.4;
    private static final double SQUARE_SPAWN_CHANCE = 0.7;
    private static final double CIRCLE_SPAWN_CHANCE = 0.9;

    private final int mapWidth;
    private final int mapHeight;
    private final Random random = new Random();

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
        double padding = 30;
        for (int corner = 0; corner < NUM_CORNERS; corner++) {
            double[] cornerPos = positionInCorner(corner, padding);
            for (int i = 0; i < ROUND_ZERO_ENEMIES_PER_CORNER; i++) {
                double angle = Math.toRadians(i * 90);
                double offsetX = Math.cos(angle) * ROUND_ZERO_CLUSTER_RADIUS;
                double offsetY = Math.sin(angle) * ROUND_ZERO_CLUSTER_RADIUS;
                double spawnX = cornerPos[0] + offsetX;
                double spawnY = cornerPos[1] + offsetY;
                enemies.add(new OctagonEnemy(spawnX, spawnY, OCTAGON_RADIUS,
                        OCTAGON_HEALTH, OCTAGON_DAMAGE, OCTAGON_SPEED));
            }
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

        if (elapsed > WAVE_DURATION_MS) {
            return;
        }
        if (enemiesSpawnedThisWave >= enemiesToSpawnThisWave) {
            return;
        }
        if (now - lastSpawnTime < SPAWN_INTERVAL_MS) {
            return;
        }

        lastSpawnTime = now;
        spawnEnemyForCurrentWave(enemies);
    }

    private void spawnEnemyForCurrentWave(List<Enemy> enemies) {
        if (waveNumber == 1) {
            spawnTriangleEnemy(enemies);
            return;
        }

        if (waveNumber == 2) {
            if (random.nextDouble() < WAVE2_TRIANGLE_CHANCE) {
                spawnTriangleEnemy(enemies);
            } else {
                spawnSquareEnemy(enemies);
            }
            return;
        }

        double roll = random.nextDouble();
        if (roll < TRIANGLE_SPAWN_CHANCE) {
            spawnTriangleEnemy(enemies);
        } else if (roll < SQUARE_SPAWN_CHANCE) {
            spawnSquareEnemy(enemies);
        } else if (roll < CIRCLE_SPAWN_CHANCE) {
            spawnCircleEnemy(enemies);
        } else {
            spawnShooterEnemy(enemies);
        }
    }

    private void spawnTriangleEnemy(List<Enemy> enemies) {
        double[] position = getNextSpawnPosition(TRIANGLE_RADIUS);
        enemies.add(new TriangleEnemy(position[0], position[1], TRIANGLE_RADIUS,
                TRIANGLE_HEALTH, TRIANGLE_DAMAGE, TRIANGLE_SPEED));
        enemiesSpawnedThisWave++;
    }

    private void spawnCircleEnemy(List<Enemy> enemies) {
        double[] position = getNextSpawnPosition(CIRCLE_RADIUS);
        enemies.add(new CircleEnemy(position[0], position[1], CIRCLE_RADIUS,
                CIRCLE_HEALTH, CIRCLE_DAMAGE, CIRCLE_SPEED, CIRCLE_FORCE_FIELD_RADIUS));
        enemiesSpawnedThisWave++;
    }

    private void spawnSquareEnemy(List<Enemy> enemies) {
        double[] position = getNextSpawnPosition(SQUARE_HALF_SIZE);
        enemies.add(new SquareEnemy(position[0], position[1], SQUARE_HALF_SIZE,
                SQUARE_HEALTH, SQUARE_DAMAGE, SQUARE_SPEED, SQUARE_DODGE_RADIUS));
        enemiesSpawnedThisWave++;
    }

    private void spawnShooterEnemy(List<Enemy> enemies) {
        double[] position = getNextSpawnPosition(SHOOTER_RADIUS);
        enemies.add(new ShooterEnemy(position[0], position[1], SHOOTER_RADIUS,
                SHOOTER_HEALTH, SHOOTER_DAMAGE, SHOOTER_SPEED));
        enemiesSpawnedThisWave++;
    }

    private double[] getNextSpawnPosition(double padding) {
        int corner = nextCornerIndex;
        nextCornerIndex = (nextCornerIndex + 1) % NUM_CORNERS;
        return positionInCorner(corner, padding);
    }

    private double[] positionInCorner(int corner, double padding) {
        double spawnX;
        double spawnY;
        double regionSize = CORNER_REGION_SIZE - 2 * padding;

        switch (corner) {
            case 0: // Top-left
                spawnX = padding + random.nextDouble() * regionSize;
                spawnY = padding + random.nextDouble() * regionSize;
                break;
            case 1: // Top-right
                spawnX = mapWidth - CORNER_REGION_SIZE + padding + random.nextDouble() * regionSize;
                spawnY = padding + random.nextDouble() * regionSize;
                break;
            case 2: // Bottom-left
                spawnX = padding + random.nextDouble() * regionSize;
                spawnY = mapHeight - CORNER_REGION_SIZE + padding + random.nextDouble() * regionSize;
                break;
            case 3: // Bottom-right
                spawnX = mapWidth - CORNER_REGION_SIZE + padding + random.nextDouble() * regionSize;
                spawnY = mapHeight - CORNER_REGION_SIZE + padding + random.nextDouble() * regionSize;
                break;
            default:
                spawnX = mapWidth / 2.0;
                spawnY = mapHeight / 2.0;
        }

        return new double[]{spawnX, spawnY};
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
        boolean isSpawningPhase = elapsed <= WAVE_DURATION_MS && enemiesSpawnedThisWave < enemiesToSpawnThisWave;
        return isSpawningPhase ? "Spawning" : "Kill enemies to go to next wave";
    }

    public int getWaveNumber() {
        return waveNumber;
    }

    public long getWaveStartTime() {
        return waveStartTime;
    }
}
