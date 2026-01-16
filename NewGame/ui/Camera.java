/*
Name: Camera.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Centers view on target position.
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

    // check if object is visible in camera viewport
    public boolean isInView(double objX, double objY, double objRadius) {
        return objX + objRadius >= x && objX - objRadius <= x + screenWidth &&
               objY + objRadius >= y && objY - objRadius <= y + screenHeight;
    }
}
