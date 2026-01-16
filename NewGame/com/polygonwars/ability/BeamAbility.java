package com.polygonwars.ability;

import com.polygonwars.enemy.Enemy;
import com.polygonwars.manager.ParticleManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class BeamAbility {
    private static final double BEAM_DURATION_SECONDS = 3.0;
    private static final double BEAM_WIDTH = 8.0;
    private static final Color BEAM_COLOR = new Color(255, 100, 0, 200);
    private static final double KILL_RADIUS = 30.0;
    
    private boolean isActive = false;
    private long startTime;
    private List<double[]> path;
    private List<Double> cumulativeDistances;
    private double totalPathLength;
    private List<Enemy> targetEnemies;
    private List<Boolean> enemiesKilled;
    private ParticleManager particleManager;
    
    public BeamAbility() {
        path = new ArrayList<>();
        cumulativeDistances = new ArrayList<>();
        targetEnemies = new ArrayList<>();
        enemiesKilled = new ArrayList<>();
    }
    
    public void activate(List<double[]> path, List<Enemy> enemiesToKill, ParticleManager particleManager) {
        this.path = new ArrayList<>(path);
        this.targetEnemies = new ArrayList<>(enemiesToKill);
        this.particleManager = particleManager;
        this.isActive = true;
        this.startTime = System.currentTimeMillis();
        this.enemiesKilled.clear();
        for (int i = 0; i < enemiesToKill.size(); i++) {
            enemiesKilled.add(enemiesToKill.get(i) == null || !enemiesToKill.get(i).isAlive());
        }
        calculatePathMetrics();
    }
    
    private void calculatePathMetrics() {
        cumulativeDistances.clear();
        if (path.size() < 2) {
            totalPathLength = 0;
            return;
        }
        double cumulative = 0;
        cumulativeDistances.add(0.0);
        for (int i = 0; i < path.size() - 1; i++) {
            double[] p1 = path.get(i), p2 = path.get(i + 1);
            cumulative += dist(p1[0], p1[1], p2[0], p2[1]);
            cumulativeDistances.add(cumulative);
        }
        totalPathLength = cumulative;
    }
    
    public boolean update(double deltaSeconds) {
        if (!isActive) return false;
        double elapsed = (System.currentTimeMillis() - startTime) / 1000.0;
        if (elapsed >= BEAM_DURATION_SECONDS) {
            for (int i = 0; i < targetEnemies.size(); i++) {
                if (!enemiesKilled.get(i)) killEnemy(targetEnemies.get(i), i);
            }
            isActive = false;
            return false;
        }
        checkAndKillEnemies();
        return true;
    }
    
    private void checkAndKillEnemies() {
        double[] pos = getCurrentPosition();
        for (int i = 0; i < targetEnemies.size(); i++) {
            if (enemiesKilled.get(i)) continue;
            Enemy e = targetEnemies.get(i);
            if (e == null || !e.isAlive()) {
                enemiesKilled.set(i, true);
                continue;
            }
            if (dist(pos[0], pos[1], e.getX(), e.getY()) <= KILL_RADIUS) {
                killEnemy(e, i);
            }
        }
    }
    
    private void killEnemy(Enemy enemy, int index) {
        if (enemy == null || !enemy.isAlive() || enemiesKilled.get(index)) return;
        enemy.takeDamage(enemy.getMaxHealth() * 10);
        enemiesKilled.set(index, true);
        if (particleManager != null) particleManager.spawnDeathEffect(enemy);
    }
    
    public double[] getCurrentPosition() {
        if (path.isEmpty()) return new double[]{0, 0};
        if (path.size() == 1) return path.get(0);
        
        double progress = Math.min(1.0, (System.currentTimeMillis() - startTime) / 1000.0 / BEAM_DURATION_SECONDS);
        double targetDist = progress * totalPathLength;
        
        int segIdx = 0;
        for (int i = 0; i < cumulativeDistances.size() - 1; i++) {
            if (targetDist <= cumulativeDistances.get(i + 1)) {
                segIdx = i;
                break;
            }
        }
        if (segIdx >= path.size() - 1) return path.get(path.size() - 1);
        
        double segStart = cumulativeDistances.get(segIdx);
        double segEnd = cumulativeDistances.get(segIdx + 1);
        double segProgress = (targetDist - segStart) / (segEnd - segStart);
        double[] p1 = path.get(segIdx), p2 = path.get(segIdx + 1);
        return new double[]{p1[0] + (p2[0] - p1[0]) * segProgress, p1[1] + (p2[1] - p1[1]) * segProgress};
    }
    
    public void draw(Graphics2D g2) {
        if (!isActive || path.size() < 2) return;
        
        double progress = Math.min(1.0, (System.currentTimeMillis() - startTime) / 1000.0 / BEAM_DURATION_SECONDS);
        double targetDist = progress * totalPathLength;
        
        g2.setColor(new Color(BEAM_COLOR.getRed(), BEAM_COLOR.getGreen(), BEAM_COLOR.getBlue(), 60));
        g2.setStroke(new java.awt.BasicStroke((float)(BEAM_WIDTH * 0.4f), 
                     java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        for (int i = 0; i < path.size() - 1; i++) {
            double[] p1 = path.get(i), p2 = path.get(i + 1);
            g2.drawLine((int)p1[0], (int)p1[1], (int)p2[0], (int)p2[1]);
        }
        
        g2.setColor(BEAM_COLOR);
        g2.setStroke(new java.awt.BasicStroke((float)BEAM_WIDTH, 
                     java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        
        double[] currentPos = getCurrentPosition();
        double cumDist = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            double[] p1 = path.get(i), p2 = path.get(i + 1);
            double segDist = cumulativeDistances.get(i + 1) - cumDist;
            if (cumDist + segDist <= targetDist) {
                g2.drawLine((int)p1[0], (int)p1[1], (int)p2[0], (int)p2[1]);
            } else if (cumDist < targetDist) {
                double segProg = (targetDist - cumDist) / segDist;
                g2.drawLine((int)p1[0], (int)p1[1], 
                           (int)(p1[0] + (p2[0] - p1[0]) * segProg), 
                           (int)(p1[1] + (p2[1] - p1[1]) * segProg));
                break;
            } else break;
            cumDist += segDist;
        }
        
        int glowSize = 25;
        g2.setColor(new Color(255, 200, 100, 255));
        g2.fillOval((int)(currentPos[0] - glowSize/2), (int)(currentPos[1] - glowSize/2), glowSize, glowSize);
        g2.setColor(new Color(255, 150, 50, 180));
        double[] startPos = path.get(0);
        g2.fillOval((int)(startPos[0] - glowSize/2), (int)(startPos[1] - glowSize/2), glowSize, glowSize);
    }
    
    private double dist(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1, dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public boolean isActive() { return isActive; }
    public void deactivate() { isActive = false; }
}
