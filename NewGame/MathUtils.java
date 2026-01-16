import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;

/**
 * Utility class for common mathematical operations and rendering helpers.
 */
public final class MathUtils {

    private MathUtils() {
        // Prevent instantiation
    }

    /**
     * Calculates the Euclidean distance between two points.
     */
    public static double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Calculates the squared distance between two points (faster, avoids sqrt).
     */
    public static double distanceSquared(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return dx * dx + dy * dy;
    }

    /**
     * Normalizes a vector and returns it as an array [normalizedX, normalizedY].
     * Returns [0, 0] if the vector has zero length.
     */
    public static double[] normalize(double x, double y) {
        double length = Math.sqrt(x * x + y * y);
        if (length == 0) {
            return new double[]{0, 0};
        }
        return new double[]{x / length, y / length};
    }

    /**
     * Normalizes a vector, returning a default direction if zero length.
     * Use this when you need a valid direction even for zero-length input (e.g., bullets).
     */
    public static double[] normalizeWithDefault(double x, double y, double defaultX, double defaultY) {
        double length = Math.sqrt(x * x + y * y);
        if (length == 0) {
            return new double[]{defaultX, defaultY};
        }
        return new double[]{x / length, y / length};
    }

    /**
     * Clamps a value between min and max.
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamps an integer value between min and max.
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Draws a styled button with hover effect.
     */
    public static void drawButton(Graphics2D g2, Rectangle bounds, String text,
                                   boolean isHovered, Color baseColor, Color hoverColor) {
        Color bgColor = isHovered ? new Color(50, 50, 70, 220) : new Color(30, 30, 50, 200);
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
