/*
Name: Camera.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Simple camera system that centers viewport on target position (usually player). Converts world coordinates to screen coordinates by subtracting camera offset.
*/

package ui;

public class Camera {

    private double x;
    private double y;
    private final int screenWidth;
    private final int screenHeight;

    public Camera(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void centerOn(double targetX, double targetY) {
        this.x = targetX - screenWidth / 2.0;
        this.y = targetY - screenHeight / 2.0;
    }

    public int getX() {
        return (int) x;
    }

    public int getY() {
        return (int) y;
    }
}
