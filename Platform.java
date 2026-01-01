/*
Programmers: Hamza Khan & Alec Li
Program Name: Platform
Program Date: 2025-12-31
Program Description: Platform objects for gameplay
*/

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Platform {
    private int x, y, width, height, active, realY;
    private BufferedImage image;
	
    public Platform(int x, int y, int width, int height, int active, String imagePath) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.active = active;
        this.LoadImage(imagePath);
		this.realY = this.y + 20;
    }
	
    public void LoadImage(String imagePath) {
        try {
            image = ImageIO.read(getClass().getResourceAsStream("Images/" + imagePath));
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }
    }
	
	public int getX() {
		return x;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getActive() {
		return active;
	}
	
	public int getRealY() {
		return realY;
	}
	
    public void draw(Graphics2D graphic) {
        if (image != null && active == 1) {
            graphic.drawImage(image, x, y, width, height * 4, null);
        }
    }
}
