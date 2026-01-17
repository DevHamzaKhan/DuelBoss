/*
Name: CollisionManager.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Handles collision detection and resolution for all entities
*/

package manager;

import entity.Bullet;
import entity.Character;
import enemy.Enemy;
import util.Utils;

import java.util.List;

public class CollisionManager {

    private static final double COLLISION_PUSH_FACTOR = 0.5;

    private final int mapWidth;
    private final int mapHeight;

    public CollisionManager(int mapWidth, int mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    // checks circle-circle collision using squared distance for performance
    public boolean bulletHitsEnemy(Bullet bullet, Enemy enemy) {
        double distanceSq = Utils.distanceSquared(bullet.getX(), bullet.getY(),
                enemy.getX(), enemy.getY());
        double radiusSum = bullet.getRadius() + enemy.getRadius();
        return distanceSq <= radiusSum * radiusSum;
    }

    public boolean bulletHitsPlayer(Bullet bullet, Character player) {
        double distanceSq = Utils.distanceSquared(bullet.getX(), bullet.getY(),
                player.getX(), player.getY());
        double radiusSum = bullet.getRadius() + player.getRadius();
        return distanceSq <= radiusSum * radiusSum;
    }

    // pushes overlapping enemies apart to prevent stacking
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

        // skip if not colliding or enemies are at exact same position
        if (distanceSq >= minDistanceSq || distanceSq == 0) {
            return;
        }

        // calculate normalized direction from b to a
        double distance = Math.sqrt(distanceSq);
        double normalX = deltaX / distance;
        double normalY = deltaY / distance;

        // calculate how much the circles are overlapping and push them apart
        // overlap = how far inside each other they are
        // we push each enemy half the overlap distance in opposite directions
        double overlap = minDistance - distance;
        double pushAmount = overlap * COLLISION_PUSH_FACTOR;

        // apply push in opposite directions and clamp to map bounds
        double newX1 = clampX(enemyA.getX() + normalX * pushAmount, enemyA.getRadius());
        double newY1 = clampY(enemyA.getY() + normalY * pushAmount, enemyA.getRadius());
        double newX2 = clampX(enemyB.getX() - normalX * pushAmount, enemyB.getRadius());
        double newY2 = clampY(enemyB.getY() - normalY * pushAmount, enemyB.getRadius());

        enemyA.setPosition(newX1, newY1);
        enemyB.setPosition(newX2, newY2);
    }

    private double clampX(double xPos, double radius) {
        return Utils.clamp(xPos, radius, mapWidth - radius);
    }

    private double clampY(double yPos, double radius) {
        return Utils.clamp(yPos, radius, mapHeight - radius);
    }
}
