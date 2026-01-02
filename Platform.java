import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Platform {
    private int x, y, width, height;
    private int surfaceY;
    private boolean active;
    private boolean isGround;
    private BufferedImage image;

    public Platform(int x, int y, int width, int height, int active, String imagePath) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.active = (active == 1);
        this.surfaceY = y + 20;
        this.isGround = (y >= 380);
        loadImage(imagePath);
    }

    private void loadImage(String imagePath) {
        try {
            image = ImageIO.read(getClass().getResourceAsStream("Images/" + imagePath));
        } catch (Exception e) {
            image = null;
        }
    }

    public boolean checkCollision(int charX, int charY, int charWidth, int charHeight,
                                   double velocityY, boolean droppingThrough) {
        if (!active) return false;
        if (droppingThrough && !isGround) return false;

        boolean horizontalOverlap = charX + charWidth > x && charX < x + width;
        boolean landingOnTop = velocityY >= 0 &&
                               charY + charHeight >= surfaceY &&
                               charY + charHeight <= surfaceY + 20;

        return horizontalOverlap && landingOnTop;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getActive() {
        return active ? 1 : 0;
    }

    public int getRealY() {
        return surfaceY;
    }

    public int getSurfaceY() {
        return surfaceY;
    }

    public boolean isGround() {
        return isGround;
    }

    public boolean isActive() {
        return active;
    }

    public void draw(Graphics2D g) {
        if (!active) return;

        if (image != null) {
            g.drawImage(image, x, y, width, height * 4, null);
        } else {
            g.setColor(new Color(100, 100, 100));
            g.fillRect(x, y, width, height * 4);
        }
    }
}
