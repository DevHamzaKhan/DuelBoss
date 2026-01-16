import java.util.List;

public class CollisionManager {

    private static final double COLLISION_PUSH_FACTOR = 0.5;

    private final int mapWidth;
    private final int mapHeight;

    public CollisionManager(int mapWidth, int mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    public boolean bulletHitsEnemy(Bullet bullet, Enemy enemy) {
        double distanceSq = MathUtils.distanceSquared(bullet.getX(), bullet.getY(),
                enemy.getX(), enemy.getY());
        double radiusSum = bullet.getRadius() + enemy.getRadius();
        return distanceSq <= radiusSum * radiusSum;
    }

    public boolean bulletHitsPlayer(Bullet bullet, Character player) {
        double distanceSq = MathUtils.distanceSquared(bullet.getX(), bullet.getY(),
                player.getX(), player.getY());
        double radiusSum = bullet.getRadius() + player.getRadius();
        return distanceSq <= radiusSum * radiusSum;
    }

    public void resolveEnemyCollisions(Enemy currentEnemy, List<Enemy> enemies, int currentIndex) {
        for (int i = currentIndex + 1; i < enemies.size(); i++) {
            Enemy other = enemies.get(i);
            if (!other.isAlive()) {
                continue;
            }

            resolveCollisionBetween(currentEnemy, other);
        }
    }

    private void resolveCollisionBetween(Enemy enemyA, Enemy enemyB) {
        double deltaX = enemyA.getX() - enemyB.getX();
        double deltaY = enemyA.getY() - enemyB.getY();
        double distanceSq = deltaX * deltaX + deltaY * deltaY;
        double minDistance = enemyA.getRadius() + enemyB.getRadius();
        double minDistanceSq = minDistance * minDistance;

        if (distanceSq >= minDistanceSq || distanceSq == 0) {
            return;
        }

        double distance = Math.sqrt(distanceSq);
        double normalX = deltaX / distance;
        double normalY = deltaY / distance;
        double overlap = minDistance - distance;
        double pushAmount = overlap * COLLISION_PUSH_FACTOR;

        double newX1 = clampX(enemyA.getX() + normalX * pushAmount, enemyA.getRadius());
        double newY1 = clampY(enemyA.getY() + normalY * pushAmount, enemyA.getRadius());
        double newX2 = clampX(enemyB.getX() - normalX * pushAmount, enemyB.getRadius());
        double newY2 = clampY(enemyB.getY() - normalY * pushAmount, enemyB.getRadius());

        enemyA.setPosition(newX1, newY1);
        enemyB.setPosition(newX2, newY2);
    }

    private double clampX(double xPos, double radius) {
        return MathUtils.clamp(xPos, radius, mapWidth - radius);
    }

    private double clampY(double yPos, double radius) {
        return MathUtils.clamp(yPos, radius, mapHeight - radius);
    }
}
