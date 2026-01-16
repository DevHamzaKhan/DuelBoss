import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the ultimate beam ability that follows a path and kills enemies.
 */
public class BeamAbility {
    
    private static final double BEAM_DURATION_SECONDS = 3.0; // Beam takes 3 seconds (slower)
    private static final double BEAM_WIDTH = 8.0;
    private static final Color BEAM_COLOR = new Color(255, 100, 0, 200); // Orange beam
    
    private boolean isActive = false;
    private double startTime;
    private List<double[]> path; // Path to follow: [x, y] points
    private double totalPathLength;
    private List<Double> segmentLengths; // Length of each segment
    private List<Double> cumulativeDistances; // Cumulative distance at each point
    
    // Enemies to kill (stored by reference) with their positions in the path
    private List<Enemy> targetEnemies;
    private List<Integer> enemyPathIndices; // Which path index each enemy is at
    private List<Boolean> enemiesKilled; // Track which enemies have been killed
    private ParticleManager particleManager; // For spawning explosions
    
    public BeamAbility() {
        path = new ArrayList<>();
        segmentLengths = new ArrayList<>();
        cumulativeDistances = new ArrayList<>();
        targetEnemies = new ArrayList<>();
        enemyPathIndices = new ArrayList<>();
        enemiesKilled = new ArrayList<>();
    }
    
    /**
     * Activate the beam with a calculated path.
     * @param path The path to follow (including start and end)
     * @param enemiesToKill List of enemies that will be killed
     * @param particleManager For spawning death effects
     */
    public void activate(List<double[]> path, List<Enemy> enemiesToKill, ParticleManager particleManager) {
        this.path = new ArrayList<>(path);
        this.targetEnemies = new ArrayList<>(enemiesToKill);
        this.particleManager = particleManager;
        this.isActive = true;
        this.startTime = System.currentTimeMillis();
        this.enemyPathIndices.clear();
        this.enemiesKilled.clear();
        
        // Map each enemy to its position in the path
        for (Enemy enemy : enemiesToKill) {
            if (enemy == null || !enemy.isAlive()) {
                enemyPathIndices.add(-1);
                enemiesKilled.add(true); // Already dead
                continue;
            }
            
            // Find which path point is closest to this enemy's center
            double enemyX = enemy.getX();
            double enemyY = enemy.getY();
            int closestIndex = 0;
            double minDist = Double.MAX_VALUE;
            
            for (int i = 0; i < path.size(); i++) {
                double[] point = path.get(i);
                double dist = distance(enemyX, enemyY, point[0], point[1]);
                if (dist < minDist) {
                    minDist = dist;
                    closestIndex = i;
                }
            }
            
            enemyPathIndices.add(closestIndex);
            enemiesKilled.add(false);
        }
        
        // Calculate segment lengths and cumulative distances
        calculatePathMetrics();
    }
    
    private void calculatePathMetrics() {
        segmentLengths.clear();
        cumulativeDistances.clear();
        
        if (path.size() < 2) {
            totalPathLength = 0;
            return;
        }
        
        double cumulative = 0;
        cumulativeDistances.add(0.0);
        
        for (int i = 0; i < path.size() - 1; i++) {
            double[] p1 = path.get(i);
            double[] p2 = path.get(i + 1);
            double dist = distance(p1[0], p1[1], p2[0], p2[1]);
            segmentLengths.add(dist);
            cumulative += dist;
            cumulativeDistances.add(cumulative);
        }
        
        totalPathLength = cumulative;
    }
    
    /**
     * Update the beam animation.
     * @param deltaSeconds Time since last update
     * @return true if beam is still active, false if finished
     */
    public boolean update(double deltaSeconds) {
        if (!isActive) return false;
        
        long currentTime = System.currentTimeMillis();
        double elapsed = (currentTime - startTime) / 1000.0;
        
        if (elapsed >= BEAM_DURATION_SECONDS) {
            // Beam finished - kill any remaining enemies
            killRemainingEnemies();
            isActive = false;
            return false;
        }
        
        // Check if beam has passed through any enemies
        checkAndKillEnemiesOnPath();
        
        return true;
    }
    
    /**
     * Check if the beam has reached any enemy positions and kill them.
     * Uses distance-based detection to ensure beam passes through enemy centers.
     */
    private void checkAndKillEnemiesOnPath() {
        double[] currentPos = getCurrentPosition();
        double killRadius = 30.0; // Distance threshold for killing enemies
        
        // Check each enemy
        for (int i = 0; i < targetEnemies.size(); i++) {
            if (enemiesKilled.get(i)) continue; // Already killed
            
            Enemy enemy = targetEnemies.get(i);
            if (enemy == null || !enemy.isAlive()) {
                enemiesKilled.set(i, true);
                continue;
            }
            
            // Check if beam is close enough to enemy center
            double dx = currentPos[0] - enemy.getX();
            double dy = currentPos[1] - enemy.getY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            
            // If beam is within kill radius of enemy center, kill it
            if (dist <= killRadius) {
                killEnemy(enemy, i);
            }
        }
    }
    
    /**
     * Kill a single enemy and spawn explosion effect.
     */
    private void killEnemy(Enemy enemy, int index) {
        if (enemy == null || !enemy.isAlive() || enemiesKilled.get(index)) {
            return;
        }
        
        // Kill the enemy
        enemy.takeDamage(enemy.getMaxHealth() * 10);
        enemiesKilled.set(index, true);
        
        // Spawn explosion effect
        if (particleManager != null) {
            particleManager.spawnDeathEffect(enemy);
        }
    }
    
    /**
     * Kill any remaining enemies at the end.
     */
    private void killRemainingEnemies() {
        for (int i = 0; i < targetEnemies.size(); i++) {
            if (!enemiesKilled.get(i)) {
                killEnemy(targetEnemies.get(i), i);
            }
        }
    }
    
    /**
     * Get current beam position along the path.
     */
    public double[] getCurrentPosition() {
        if (path.isEmpty()) {
            return new double[]{0, 0};
        }
        
        if (path.size() == 1) {
            return path.get(0);
        }
        
        long currentTime = System.currentTimeMillis();
        double elapsed = (currentTime - startTime) / 1000.0;
        double progress = Math.min(1.0, elapsed / BEAM_DURATION_SECONDS);
        
        double targetDistance = progress * totalPathLength;
        
        // Find which segment we're on
        int segmentIndex = 0;
        for (int i = 0; i < cumulativeDistances.size() - 1; i++) {
            if (targetDistance <= cumulativeDistances.get(i + 1)) {
                segmentIndex = i;
                break;
            }
        }
        
        if (segmentIndex >= path.size() - 1) {
            return path.get(path.size() - 1);
        }
        
        // Interpolate within the segment
        double segmentStartDist = cumulativeDistances.get(segmentIndex);
        double segmentEndDist = cumulativeDistances.get(segmentIndex + 1);
        double segmentProgress = (targetDistance - segmentStartDist) / 
                                (segmentEndDist - segmentStartDist);
        
        double[] p1 = path.get(segmentIndex);
        double[] p2 = path.get(segmentIndex + 1);
        
        double x = p1[0] + (p2[0] - p1[0]) * segmentProgress;
        double y = p1[1] + (p2[1] - p1[1]) * segmentProgress;
        
        return new double[]{x, y};
    }
    
    /**
     * Draw the beam effect.
     */
    public void draw(Graphics2D g2) {
        if (!isActive || path.size() < 2) return;
        
        long currentTime = System.currentTimeMillis();
        double elapsed = (currentTime - startTime) / 1000.0;
        double progress = Math.min(1.0, elapsed / BEAM_DURATION_SECONDS);
        double targetDistance = progress * totalPathLength;
        
        // Draw the full path first (faded preview)
        g2.setColor(new Color(BEAM_COLOR.getRed(), BEAM_COLOR.getGreen(), BEAM_COLOR.getBlue(), 60));
        g2.setStroke(new java.awt.BasicStroke((float)(BEAM_WIDTH * 0.4f), 
                     java.awt.BasicStroke.CAP_ROUND, 
                     java.awt.BasicStroke.JOIN_ROUND));
        
        for (int i = 0; i < path.size() - 1; i++) {
            double[] p1 = path.get(i);
            double[] p2 = path.get(i + 1);
            g2.drawLine((int)p1[0], (int)p1[1], (int)p2[0], (int)p2[1]);
        }
        
        // Draw bright beam along traveled path
        g2.setColor(BEAM_COLOR);
        g2.setStroke(new java.awt.BasicStroke((float)BEAM_WIDTH, 
                     java.awt.BasicStroke.CAP_ROUND, 
                     java.awt.BasicStroke.JOIN_ROUND));
        
        double[] currentPos = getCurrentPosition();
        double cumulativeDist = 0;
        
        // Draw all completed segments
        for (int i = 0; i < path.size() - 1; i++) {
            double[] p1 = path.get(i);
            double[] p2 = path.get(i + 1);
            double segmentDist = cumulativeDistances.get(i + 1) - cumulativeDist;
            
            if (cumulativeDist + segmentDist <= targetDistance) {
                // Full segment completed
                g2.drawLine((int)p1[0], (int)p1[1], (int)p2[0], (int)p2[1]);
            } else if (cumulativeDist < targetDistance) {
                // Partial segment - draw up to current position
                double segmentProgress = (targetDistance - cumulativeDist) / segmentDist;
                double partialX = p1[0] + (p2[0] - p1[0]) * segmentProgress;
                double partialY = p1[1] + (p2[1] - p1[1]) * segmentProgress;
                g2.drawLine((int)p1[0], (int)p1[1], (int)partialX, (int)partialY);
                break;
            } else {
                break;
            }
            
            cumulativeDist += segmentDist;
        }
        
        // Draw bright glow at current position
        g2.setColor(new Color(255, 200, 100, 255));
        int glowSize = 25;
        g2.fillOval((int)(currentPos[0] - glowSize/2), 
                   (int)(currentPos[1] - glowSize/2), 
                   glowSize, glowSize);
        
        // Draw glow at start position
        double[] startPos = path.get(0);
        g2.setColor(new Color(255, 150, 50, 180));
        g2.fillOval((int)(startPos[0] - glowSize/2), 
                   (int)(startPos[1] - glowSize/2), 
                   glowSize, glowSize);
    }
    
    
    private double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void deactivate() {
        isActive = false;
    }
}

