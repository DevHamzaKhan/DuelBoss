/*
Name: Utils.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Utils class for math operations and button drawing.
*/

package util;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.Rectangle;

public final class Utils {

    // button color constants
    private static final Color BUTTON_BG_DEFAULT = new Color(30, 30, 50, 200);
    private static final Color BUTTON_BG_HOVER = new Color(50, 50, 70, 220);

    // private constructor prevents instantiation of this utility class
    private Utils() {
    }

    // calculates euclidean distance between two points using pythagorean theorem
    public static double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // calculates squared distance (faster than distance since it avoids expensive
    // sqrt operation)
    // useful for distance comparisons where exact distance isn't needed
    public static double distanceSquared(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return dx * dx + dy * dy;
    }

    // normalizes a vector to unit length (length = 1), preserving direction
    // returns [0,0] for zero-length input to avoid division by zero
    public static double[] normalize(double x, double y) {
        double length = Math.sqrt(x * x + y * y);
        if (length == 0) {
            return new double[] { 0, 0 };
        }
        return new double[] { x / length, y / length };
    }

    // normalizes a vector but uses a default direction when input is zero-length
    // fixes crash when mouse is exactly on player position
    public static double[] normalizeWithDefault(double x, double y, double defaultX, double defaultY) {
        double length = Math.sqrt(x * x + y * y);
        if (length == 0) {
            return new double[] { defaultX, defaultY };
        }
        return new double[] { x / length, y / length };
    }

    // constrains a value to lie within a specified range [min, max]
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    // integer overload of clamp for whole number constraints
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    // renders a styled rectangular button with rounded corners and hover effect
    // used by menu renderer for consistent button appearance across all menus
    public static void drawButton(Graphics2D g2, Rectangle bounds, String text,
            boolean isHovered, Color baseColor, Color hoverColor) {
        // darker background and thicker border when hovered for visual feedback
        Color bgColor = isHovered ? BUTTON_BG_HOVER : BUTTON_BG_DEFAULT;
        Color borderColor = isHovered ? hoverColor : baseColor;

        g2.setColor(bgColor);
        g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 15, 15);
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(isHovered ? 4 : 3));
        g2.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 15, 15);

        FontMetrics fm = g2.getFontMetrics();
        int textX = bounds.x + (bounds.width - fm.stringWidth(text)) / 2;
        int textY = bounds.y + (bounds.height + fm.getAscent()) / 2 - 5;
        g2.setColor(Color.WHITE);
        g2.drawString(text, textX, textY);
    }
}
