import java.util.List;

public class CollisionManager {

    private final int mapWidth;
    private final int mapHeight;

    public CollisionManager(int mapWidth, int mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    public boolean bulletHitsEnemy(Bullet bullet, Enemy enemy) {
        double dx = bullet.getX() - enemy.getX();
        double dy = bullet.getY() - enemy.getY();
        double distanceSq = dx * dx + dy * dy;
        double radiusSum = bullet.getRadius() + enemy.getRadius();
        return distanceSq <= radiusSum * radiusSum;
    }

    public boolean bulletHitsPlayer(Bullet bullet, Character player) {
        double dx = bullet.getX() - player.getX();
        double dy = bullet.getY() - player.getY();
        double distanceSq = dx * dx + dy * dy;
        double radiusSum = bullet.getRadius() + player.getRadius();
        return distanceSq <= radiusSum * radiusSum;
    }

    public void resolveEnemyCollisions(Enemy currentEnemy, List<Enemy> enemies, int currentIndex) {
        for (int i = currentIndex + 1; i < enemies.size(); i++) {
            Enemy other = enemies.get(i);
            if (!other.isAlive()) continue;

            double dx = currentEnemy.getX() - other.getX();
            double dy = currentEnemy.getY() - other.getY();
            double distanceSq = dx * dx + dy * dy;
            double minDistance = currentEnemy.getRadius() + other.getRadius();
            double minDistanceSq = minDistance * minDistance;

            if (distanceSq < minDistanceSq && distanceSq > 0) {
                double distance = Math.sqrt(distanceSq);
                double nx = dx / distance;
                double ny = dy / distance;
                double overlap = minDistance - distance;
                double pushX = nx * overlap * 0.5;
                double pushY = ny * overlap * 0.5;

                double newX1 = clampX(currentEnemy.getX() + pushX, currentEnemy.getRadius());
                double newY1 = clampY(currentEnemy.getY() + pushY, currentEnemy.getRadius());
                double newX2 = clampX(other.getX() - pushX, other.getRadius());
                double newY2 = clampY(other.getY() - pushY, other.getRadius());

                currentEnemy.setPosition(newX1, newY1);
                other.setPosition(newX2, newY2);
            }
        }
    }

    private double clampX(double x, double radius) {
        double minX = radius;
        double maxX = mapWidth - radius;
        if (x < minX) return minX;
        if (x > maxX) return maxX;
        return x;
    }

    private double clampY(double y, double radius) {
        double minY = radius;
        double maxY = mapHeight - radius;
        if (y < minY) return minY;
        if (y > maxY) return maxY;
        return y;
    }
}
