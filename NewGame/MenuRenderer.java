import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class MenuRenderer {

    private final int screenWidth;
    private final int screenHeight;

    private List<Rectangle> menuButtonRects = new ArrayList<>();
    private List<Rectangle> shopButtonRects = new ArrayList<>();
    private int hoveredButtonIndex = -1;
    private int hoveredShopButtonIndex = -1;
    private BufferedImage playerImage = null;

    private static void drawSpaceBackground(Graphics2D g2, int width, int height) {
        g2.setColor(new Color(10, 10, 30));
        g2.fillRect(0, 0, width, height);

        g2.setColor(new Color(255, 255, 255, 200));
        java.util.Random rand = new java.util.Random(12345);
        for (int i = 0; i < 200; i++) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            int size = rand.nextInt(3) + 1;
            g2.fillOval(x, y, size, size);
        }

        g2.setColor(new Color(255, 255, 255, 255));
        rand = new java.util.Random(54321);
        for (int i = 0; i < 40; i++) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            g2.fillOval(x, y, 2, 2);
        }
    }

    public MenuRenderer(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        loadPlayerImage();
    }

    private void loadPlayerImage() {
        try {
            playerImage = ImageIO.read(new File("Images/player.png"));
        } catch (Exception e) {
            // Image not found, will draw placeholder
        }
    }

    public void updateHover(int mouseX, int mouseY, boolean isMenu, boolean isShop) {
        if (isMenu) {
            hoveredButtonIndex = -1;
            for (int i = 0; i < menuButtonRects.size(); i++) {
                if (menuButtonRects.get(i).contains(mouseX, mouseY)) {
                    hoveredButtonIndex = i;
                    break;
                }
            }
        } else if (isShop) {
            hoveredShopButtonIndex = -1;
            for (int i = 0; i < shopButtonRects.size(); i++) {
                if (shopButtonRects.get(i).contains(mouseX, mouseY)) {
                    hoveredShopButtonIndex = i;
                    break;
                }
            }
        }
    }

    public int getClickedMenuButton(int x, int y) {
        for (int i = 0; i < menuButtonRects.size(); i++) {
            if (menuButtonRects.get(i).contains(x, y))
                return i;
        }
        return -1;
    }

    public int getClickedShopButton(int x, int y) {
        for (int i = 0; i < shopButtonRects.size(); i++) {
            if (shopButtonRects.get(i).contains(x, y))
                return i;
        }
        return -1;
    }

    public void drawMainMenu(Graphics2D g2, int highScore) {
        drawSpaceBackground(g2, screenWidth, screenHeight);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 72f));
        FontMetrics fm = g2.getFontMetrics();
        String title = "POLYGON WARS";
        int titleX = (screenWidth - fm.stringWidth(title)) / 2;
        int titleY = 150;

        g2.setColor(new Color(0, 200, 255, 100));
        for (int i = 0; i < 5; i++) {
            g2.drawString(title, titleX + i, titleY + i);
        }
        g2.setColor(new Color(0, 255, 255));
        g2.drawString(title, titleX, titleY);

        int buttonWidth = 300;
        int buttonHeight = 60;
        int buttonSpacing = 30;
        String[] buttonTexts = { "Play", "How to Play", "Quit" };
        int totalButtonHeight = buttonTexts.length * buttonHeight + (buttonTexts.length - 1) * buttonSpacing;
        int startY = (screenHeight - totalButtonHeight - 100) / 2 + 100;

        menuButtonRects.clear();

        for (int i = 0; i < buttonTexts.length; i++) {
            int x = (screenWidth - buttonWidth) / 2;
            int y = startY + i * (buttonHeight + buttonSpacing);

            if (y + buttonHeight > screenHeight - 80)
                break;

            boolean isHovered = (hoveredButtonIndex == i);
            Color bgColor = isHovered ? new Color(50, 50, 70, 220) : new Color(30, 30, 50, 200);
            Color borderColor = isHovered ? new Color(100, 255, 255) : new Color(0, 200, 255);

            g2.setColor(bgColor);
            g2.fillRoundRect(x, y, buttonWidth, buttonHeight, 15, 15);
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(isHovered ? 4 : 3));
            g2.drawRoundRect(x, y, buttonWidth, buttonHeight, 15, 15);

            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32f));
            fm = g2.getFontMetrics();
            int textX = x + (buttonWidth - fm.stringWidth(buttonTexts[i])) / 2;
            int textY = y + (buttonHeight + fm.getAscent()) / 2 - 5;
            g2.setColor(Color.WHITE);
            g2.drawString(buttonTexts[i], textX, textY);

            menuButtonRects.add(new Rectangle(x, y, buttonWidth, buttonHeight));
        }

        if (highScore > 0) {
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24f));
            fm = g2.getFontMetrics();
            String highScoreText = "High Score: " + highScore;
            int hsX = (screenWidth - fm.stringWidth(highScoreText)) / 2;
            int hsY = startY + buttonTexts.length * (buttonHeight + buttonSpacing) + 40;
            if (hsY + fm.getHeight() <= screenHeight - 20) {
                g2.setColor(Color.YELLOW);
                g2.drawString(highScoreText, hsX, hsY);
            }
        }
    }

    public void drawHowToPlay(Graphics2D g2) {
        drawSpaceBackground(g2, screenWidth, screenHeight);

        // Title
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 48f));
        FontMetrics fm = g2.getFontMetrics();
        String title = "How to Play";
        int titleX = (screenWidth - fm.stringWidth(title)) / 2;
        g2.setColor(Color.WHITE);
        g2.drawString(title, titleX, 50);

        // Objective paragraph at top
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 16f));
        fm = g2.getFontMetrics();
        String objective = "OBJECTIVE: Level up your character in this endless game mode. Survive waves of enemies, " +
                          "earn currency, and upgrade your abilities to last as long as possible!";
        int objY = 90;
        int objWidth = screenWidth - 100;
        int objX = 50;
        drawWrappedText(g2, objective, objX, objY, objWidth, fm);

        // Split screen into left and right sections with boxes
        int startY = 140;
        int boxSpacing = 20;
        int leftBoxWidth = (screenWidth - boxSpacing * 3) / 3; // Left takes 1/3
        int rightBoxWidth = (screenWidth - boxSpacing * 3) * 2 / 3; // Right takes 2/3
        int boxHeight = screenHeight - startY - 100; // Leave space for back button
        int leftBoxX = boxSpacing;
        int rightBoxX = leftBoxX + leftBoxWidth + boxSpacing;
        int boxY = startY;
        int cornerRadius = 20;

        // LEFT SIDE: Character section box
        g2.setColor(new Color(30, 30, 50, 220));
        g2.fillRoundRect(leftBoxX, boxY, leftBoxWidth, boxHeight, cornerRadius, cornerRadius);
        g2.setColor(new Color(0, 200, 255));
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(leftBoxX, boxY, leftBoxWidth, boxHeight, cornerRadius, cornerRadius);
        drawCharacterSection(g2, leftBoxX + 20, boxY, leftBoxWidth - 40, boxHeight);

        // RIGHT SIDE: Enemy types section box
        g2.setColor(new Color(30, 30, 50, 220));
        g2.fillRoundRect(rightBoxX, boxY, rightBoxWidth, boxHeight, cornerRadius, cornerRadius);
        g2.setColor(new Color(0, 200, 255));
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(rightBoxX, boxY, rightBoxWidth, boxHeight, cornerRadius, cornerRadius);
        drawEnemySection(g2, rightBoxX + 20, boxY, rightBoxWidth - 40, boxHeight);

        // Back button (smaller)
        int buttonWidth = 250;
        int buttonHeight = 50;
        int buttonX = (screenWidth - buttonWidth) / 2;
        int buttonY = screenHeight - buttonHeight - 40;

        menuButtonRects.clear();

        boolean isBackHovered = (hoveredButtonIndex == 0);
        Color backBgColor = isBackHovered ? new Color(50, 50, 70, 220) : new Color(30, 30, 50, 200);
        Color backBorderColor = isBackHovered ? new Color(100, 255, 255) : new Color(0, 200, 255);

        g2.setColor(backBgColor);
        g2.fillRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 15, 15);
        g2.setColor(backBorderColor);
        g2.setStroke(new BasicStroke(isBackHovered ? 4 : 3));
        g2.drawRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 15, 15);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24f));
        fm = g2.getFontMetrics();
        String backText = "Back to Main Menu";
        int textX = buttonX + (buttonWidth - fm.stringWidth(backText)) / 2;
        int textY = buttonY + (buttonHeight + fm.getAscent()) / 2 - 5;
        g2.setColor(Color.WHITE);
        g2.drawString(backText, textX, textY);

        menuButtonRects.add(new Rectangle(buttonX, buttonY, buttonWidth, buttonHeight));
    }

    private void drawWrappedText(Graphics2D g2, String text, int x, int y, int maxWidth, FontMetrics fm) {
        String[] words = text.split(" ");
        String line = "";
        int currentY = y;

        for (String word : words) {
            String testLine = line + (line.isEmpty() ? "" : " ") + word;
            int width = fm.stringWidth(testLine);
            if (width > maxWidth && !line.isEmpty()) {
                g2.drawString(line, x, currentY);
                line = word;
                currentY += fm.getHeight() + 5;
            } else {
                line = testLine;
            }
        }
        if (!line.isEmpty()) {
            g2.drawString(line, x, currentY);
        }
    }

    private void drawCharacterSection(Graphics2D g2, int x, int y, int width, int height) {
        // Heading centered at top
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32f));
        FontMetrics fm = g2.getFontMetrics();
        String heading = "Character";
        int headingX = x + (width - fm.stringWidth(heading)) / 2;
        g2.setColor(Color.WHITE);
        g2.drawString(heading, headingX, y + 35);

        int currentY = y + 60;

        // Draw character image
        int imageSize = 80;
        int imageX = x + (width - imageSize) / 2;
        int imageY = currentY;
        if (playerImage != null) {
            g2.drawImage(playerImage, imageX, imageY, imageSize, imageSize, null);
        } else {
            // Draw placeholder circle if image not found
            g2.setColor(new Color(100, 150, 255));
            g2.fillOval(imageX, imageY, imageSize, imageSize);
        }
        currentY += imageSize + 20;

        // Controls
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 20f));
        g2.setColor(Color.WHITE);
        g2.drawString("Controls:", x, currentY);
        currentY += 30;

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 16f));
        FontMetrics fmControls = g2.getFontMetrics();
        g2.setColor(Color.WHITE);

        // WASD keys in T-shape: W on top, A-S-D below (W above S)
        int keySize = 35;
        int keySpacing = 10;
        int keysCenterX = x + width / 2;
        int keysY = currentY;
        
        // Draw W centered on top
        drawKey(g2, "W", keysCenterX - keySize / 2, keysY, keySize);
        // Draw A, S, D in a row below (S centered, A left, D right)
        int bottomRowY = keysY + keySize + keySpacing;
        drawKey(g2, "A", keysCenterX - keySize - keySpacing - keySize / 2, bottomRowY, keySize);
        drawKey(g2, "S", keysCenterX - keySize / 2, bottomRowY, keySize);
        drawKey(g2, "D", keysCenterX + keySpacing + keySize / 2, bottomRowY, keySize);
        
        g2.drawString("Move", keysCenterX - fmControls.stringWidth("Move") / 2, bottomRowY + keySize + 20);
        currentY += (keySize + keySpacing) * 2 + 40;

        // Auto shoot
        g2.drawString("Auto shoot in direction of mouse", x, currentY);
        currentY += 25;

        // Right click for laser beam
        g2.drawString("Right click for laser beam attack", x, currentY);
        currentY += 40;

        // Upgrading options
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
        g2.setColor(Color.WHITE);
        g2.drawString("Upgrading Options:", x, currentY);
        currentY += 30;

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 18f));
        g2.setColor(Color.WHITE);
        String[] upgrades = {
            "Max Health",
            "Bullet Speed",
            "Fire Rate",
            "Movement Speed",
            "Bullet Damage"
        };
        for (String upgrade : upgrades) {
            g2.drawString(upgrade, x, currentY);
            currentY += 30;
        }
    }

    private void drawKey(Graphics2D g2, String key, int x, int y, int size) {
        g2.setColor(new Color(60, 60, 80));
        g2.fillRoundRect(x, y, size, size, 5, 5);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, size, size, 5, 5);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
        FontMetrics fm = g2.getFontMetrics();
        int textX = x + (size - fm.stringWidth(key)) / 2;
        int textY = y + (size + fm.getAscent()) / 2 - 2;
        g2.drawString(key, textX, textY);
    }

    private void drawEnemySection(Graphics2D g2, int x, int y, int width, int height) {
        // Heading centered at top
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32f));
        FontMetrics fm = g2.getFontMetrics();
        String heading = "Enemies";
        int headingX = x + (width - fm.stringWidth(heading)) / 2;
        g2.setColor(Color.WHITE);
        g2.drawString(heading, headingX, y + 35);

        int startY = y + 70;
        int sectionSpacing = 20;
        int leftSectionWidth = (width - sectionSpacing) / 2;
        int rightSectionWidth = (width - sectionSpacing) / 2;
        int leftSectionX = x + 40; // Shift regular enemies right
        int rightSectionX = x + leftSectionWidth + sectionSpacing;
        int enemySize = 90; // Bigger enemy sizes
        int rowSpacing = 130;

        // LEFT SECTION: Regular Enemies
        // Regular Enemies heading
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 22f));
        fm = g2.getFontMetrics();
        String regularHeading = "Regular Enemy";
        int regularHeadingX = leftSectionX + (leftSectionWidth - fm.stringWidth(regularHeading)) / 2;
        g2.setColor(Color.WHITE);
        g2.drawString(regularHeading, regularHeadingX, startY);

        int currentY = startY + 35;

        // Row 1: 2 enemies (Triangle, Square)
        EnemyInfo[] row1 = {
            new EnemyInfo("Triangle", "Chases you", 0),
            new EnemyInfo("Square", "Dodges bullets", 1)
        };
        currentY = drawEnemyRow(g2, leftSectionX, currentY, leftSectionWidth, row1, 2, enemySize);
        currentY += rowSpacing;

        // Row 2: 2 enemies (Circle, Pentagon)
        EnemyInfo[] row2 = {
            new EnemyInfo("Circle", "Explodes in force field", 2),
            new EnemyInfo("Pentagon", "Shoots from distance", 3)
        };
        currentY = drawEnemyRow(g2, leftSectionX, currentY, leftSectionWidth, row2, 2, enemySize);
        currentY += rowSpacing;

        // Row 3: 1 enemy (Hexagon)
        EnemyInfo[] row3 = {
            new EnemyInfo("Hexagon", "Splits into triangles", 4)
        };
        drawEnemyRow(g2, leftSectionX, currentY, leftSectionWidth, row3, 1, enemySize);

        // RIGHT SECTION: Bosses
        // Bosses heading
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 22f));
        fm = g2.getFontMetrics();
        String bossesHeading = "Bosses";
        int bossesHeadingX = rightSectionX + (rightSectionWidth - fm.stringWidth(bossesHeading)) / 2;
        g2.setColor(Color.WHITE);
        g2.drawString(bossesHeading, bossesHeadingX, startY);

        currentY = startY + 35;

        // Calculate center position based on heading
        int bossCenterX = bossesHeadingX + fm.stringWidth(bossesHeading) / 2;

        // Row 1: 1 enemy (Hexagon)
        EnemyInfo[] bossRow1 = {
            new EnemyInfo("Hexagon", "Splits into triangles", 4)
        };
        currentY = drawEnemyRowCentered(g2, bossCenterX, currentY, enemySize, bossRow1[0]);
        currentY += rowSpacing;

        // Row 2: 1 enemy (Star)
        EnemyInfo[] bossRow2 = {
            new EnemyInfo("Star", "Spawns enemies", 6)
        };
        drawEnemyRowCentered(g2, bossCenterX, currentY, enemySize, bossRow2[0]);
    }

    private int drawEnemyRow(Graphics2D g2, int x, int y, int width, EnemyInfo[] enemies, int cols, int enemySize) {
        int colSpacing = 20;
        int cellWidth = cols > 1 ? (width - colSpacing * (cols - 1)) / cols : width;
        int currentY = y;

        for (int i = 0; i < enemies.length; i++) {
            int col = i % cols;
            int cellX = x + (cols > 1 ? col * (cellWidth + colSpacing) : (cellWidth - enemySize) / 2);
            
            // Center enemy image in cell
            int enemyX = cellX + (cellWidth - enemySize) / 2;
            int enemyY = currentY;
            
            // Draw enemy shape
            drawEnemyShape(g2, enemyX, enemyY, enemySize, enemies[i].type);
            
            // Draw enemy name and description centered below image
            int textY = enemyY + enemySize + 20;
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(Color.WHITE);
            int nameX = cellX + (cellWidth - fm.stringWidth(enemies[i].name)) / 2;
            g2.drawString(enemies[i].name, nameX, textY);
            
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 16f));
            fm = g2.getFontMetrics();
            g2.setColor(Color.WHITE);
            int descX = cellX + (cellWidth - fm.stringWidth(enemies[i].description)) / 2;
            g2.drawString(enemies[i].description, descX, textY + 22);
        }

        return currentY + 130; // Return next Y position with row spacing
    }

    private int drawEnemyRowCentered(Graphics2D g2, int centerX, int y, int enemySize, EnemyInfo enemy) {
        // Center enemy image at centerX
        int enemyX = centerX - enemySize / 2;
        int enemyY = y;
        
        // Draw enemy shape
        drawEnemyShape(g2, enemyX, enemyY, enemySize, enemy.type);
        
        // Draw enemy name and description centered below image
        int textY = enemyY + enemySize + 20;
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(Color.WHITE);
        int nameX = centerX - fm.stringWidth(enemy.name) / 2;
        g2.drawString(enemy.name, nameX, textY);
        
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 16f));
        fm = g2.getFontMetrics();
        g2.setColor(Color.WHITE);
        int descX = centerX - fm.stringWidth(enemy.description) / 2;
        g2.drawString(enemy.description, descX, textY + 22);
        
        return y + 130;
    }

    private void drawEnemyShape(Graphics2D g2, int x, int y, int size, int type) {
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        int radius = size / 2 - 5;

        java.awt.geom.AffineTransform old = g2.getTransform();
        g2.translate(centerX, centerY);

        switch (type) {
            case 0: // Triangle
                int[] triX = { 0, -radius, radius };
                int[] triY = { -radius, radius, radius };
                Polygon triangle = new Polygon(triX, triY, 3);
                g2.setColor(new Color(255, 80, 80));
                g2.fillPolygon(triangle);
                g2.setColor(Color.DARK_GRAY);
                g2.drawPolygon(triangle);
                break;
            case 1: // Square
                int half = radius;
                g2.setColor(new Color(255, 150, 80));
                g2.fillRect(-half, -half, half * 2, half * 2);
                g2.setColor(Color.BLACK);
                g2.drawRect(-half, -half, half * 2, half * 2);
                break;
            case 2: // Circle
                g2.setColor(new Color(120, 120, 255));
                g2.fillOval(-radius, -radius, radius * 2, radius * 2);
                g2.setColor(Color.WHITE);
                g2.drawOval(-radius, -radius, radius * 2, radius * 2);
                break;
            case 3: // Pentagon (Shooter)
                int sides = 5;
                int[] pentX = new int[sides];
                int[] pentY = new int[sides];
                for (int i = 0; i < sides; i++) {
                    double ang = -Math.PI / 2 + i * 2 * Math.PI / sides;
                    pentX[i] = (int)(Math.cos(ang) * radius);
                    pentY[i] = (int)(Math.sin(ang) * radius);
                }
                Polygon pentagon = new Polygon(pentX, pentY, sides);
                g2.setColor(new Color(180, 120, 255));
                g2.fillPolygon(pentagon);
                g2.setColor(Color.DARK_GRAY);
                g2.drawPolygon(pentagon);
                break;
            case 4: // Hexagon
                int hexSides = 6;
                int[] hexX = new int[hexSides];
                int[] hexY = new int[hexSides];
                for (int i = 0; i < hexSides; i++) {
                    double ang = -Math.PI / 2 + i * 2 * Math.PI / hexSides;
                    hexX[i] = (int)(Math.cos(ang) * radius);
                    hexY[i] = (int)(Math.sin(ang) * radius);
                }
                Polygon hex = new Polygon(hexX, hexY, hexSides);
                g2.setColor(new Color(120, 200, 120));
                g2.fillPolygon(hex);
                g2.setColor(Color.DARK_GRAY);
                g2.drawPolygon(hex);
                break;
            case 5: // Octagon
                int octSides = 8;
                int[] octX = new int[octSides];
                int[] octY = new int[octSides];
                for (int i = 0; i < octSides; i++) {
                    double ang = i * (2 * Math.PI / octSides) - Math.PI / 2.0;
                    octX[i] = (int)(Math.cos(ang) * radius);
                    octY[i] = (int)(Math.sin(ang) * radius);
                }
                Polygon octagon = new Polygon(octX, octY, octSides);
                g2.setColor(new Color(150, 80, 200));
                g2.fillPolygon(octagon);
                g2.setColor(Color.DARK_GRAY);
                g2.drawPolygon(octagon);
                break;
            case 6: // Star (Spawner)
                int[] starX = new int[10];
                int[] starY = new int[10];
                for (int i = 0; i < 10; i++) {
                    double ang = i * Math.PI / 5.0 - Math.PI / 2.0;
                    double dist = (i % 2 == 0) ? radius : radius * 0.4;
                    starX[i] = (int)(Math.cos(ang) * dist);
                    starY[i] = (int)(Math.sin(ang) * dist);
                }
                Polygon star = new Polygon(starX, starY, 10);
                g2.setColor(new Color(255, 255, 0));
                g2.fillPolygon(star);
                g2.setColor(new Color(200, 200, 0));
                g2.drawPolygon(star);
                break;
        }

        g2.setTransform(old);
    }

    private static class EnemyInfo {
        String name;
        String description;
        int type;

        EnemyInfo(String name, String description, int type) {
            this.name = name;
            this.description = description;
            this.type = type;
        }
    }

    public void drawGameOver(Graphics2D g2, int score, int waveNumber, int highScore) {
        drawSpaceBackground(g2, screenWidth, screenHeight);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 64f));
        FontMetrics fm = g2.getFontMetrics();
        String gameOverText = "GAME OVER";
        int goX = (screenWidth - fm.stringWidth(gameOverText)) / 2;
        g2.setColor(Color.RED);
        g2.drawString(gameOverText, goX, 150);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 36f));
        fm = g2.getFontMetrics();
        String scoreText = "Final Score: " + score;
        int scoreX = (screenWidth - fm.stringWidth(scoreText)) / 2;
        g2.setColor(Color.WHITE);
        g2.drawString(scoreText, scoreX, 250);

        String waveText = "Wave Reached: " + waveNumber;
        int waveX = (screenWidth - fm.stringWidth(waveText)) / 2;
        g2.drawString(waveText, waveX, 310);

        if (score > 0 && score == highScore) {
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32f));
            fm = g2.getFontMetrics();
            String newHighScoreText = "NEW HIGH SCORE!";
            int nhsX = (screenWidth - fm.stringWidth(newHighScoreText)) / 2;
            g2.setColor(Color.YELLOW);
            g2.drawString(newHighScoreText, nhsX, 380);
        }

        int buttonWidth = 300;
        int buttonHeight = 60;
        int buttonSpacing = 30;
        String[] buttonTexts = { "Return to Main Menu", "Play Again" };
        int totalButtonHeight = buttonTexts.length * buttonHeight + (buttonTexts.length - 1) * buttonSpacing;
        int startY = Math.min(520, screenHeight - totalButtonHeight - 40);

        menuButtonRects.clear();

        for (int i = 0; i < buttonTexts.length; i++) {
            int x = (screenWidth - buttonWidth) / 2;
            int y = startY + i * (buttonHeight + buttonSpacing);

            if (y + buttonHeight > screenHeight - 20)
                break;

            boolean isHovered = (hoveredButtonIndex == i);
            Color bgColor = isHovered ? new Color(50, 50, 70, 220) : new Color(30, 30, 50, 200);
            Color borderColor = isHovered ? new Color(100, 255, 255) : new Color(0, 200, 255);

            g2.setColor(bgColor);
            g2.fillRoundRect(x, y, buttonWidth, buttonHeight, 15, 15);
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(isHovered ? 4 : 3));
            g2.drawRoundRect(x, y, buttonWidth, buttonHeight, 15, 15);

            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24f));
            fm = g2.getFontMetrics();
            int textX = x + (buttonWidth - fm.stringWidth(buttonTexts[i])) / 2;
            int textY = y + (buttonHeight + fm.getAscent()) / 2 - 5;
            g2.setColor(Color.WHITE);
            g2.drawString(buttonTexts[i], textX, textY);

            menuButtonRects.add(new Rectangle(x, y, buttonWidth, buttonHeight));
        }
    }

    public void drawUpgradeShop(Graphics2D g2, Character player, int currency, int score) {
        drawSpaceBackground(g2, screenWidth, screenHeight);

        int panelWidth = 800;
        int panelHeight = Math.min(700, screenHeight - 100);
        int panelX = screenWidth / 2 - panelWidth / 2;
        int panelY = (screenHeight - panelHeight) / 2;

        g2.setColor(new Color(30, 30, 40, 250));
        g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 20, 20);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 20, 20);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 36f));
        FontMetrics fm = g2.getFontMetrics();
        String title = "Upgrade Shop - Currency: " + currency;
        int titleX = panelX + (panelWidth - fm.stringWidth(title)) / 2;
        g2.setColor(Color.YELLOW);
        g2.drawString(title, titleX, panelY + 50);

        int yStart = panelY + 100;
        int buttonWidth = 200;
        int buttonHeight = 50;
        int rowSpacing = 65; // Total height per row (stat + progress bar)
        int progressBarWidth = 200;
        int progressBarHeight = 20;
        int containerWidth = 500;
        int containerX = panelX + (panelWidth - containerWidth) / 2;

        String[] statNames = { "Max Health", "Bullet Speed", "Fire Rate", "Movement Speed", "Bullet Damage" };
        int[] levels = {
                player.getMaxHealthLevel(),
                player.getBulletSpeedLevel(),
                player.getFireRateLevel(),
                player.getMovementSpeedLevel(),
                player.getBulletDamageLevel()
        };

        shopButtonRects.clear();

        for (int i = 0; i < statNames.length; i++) {
            int y = yStart + i * rowSpacing;

            g2.setColor(Color.WHITE);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
            g2.drawString(statNames[i] + " (Lv " + levels[i] + "/10)", containerX, y + 20);

            int progX = containerX;
            int progY = y + 30;
            g2.setColor(new Color(50, 50, 50));
            g2.fillRoundRect(progX, progY, progressBarWidth, progressBarHeight, 5, 5);

            int segmentWidth = progressBarWidth / 10;
            for (int seg = 0; seg < 10; seg++) {
                g2.setColor(seg < levels[i] ? new Color(0, 200, 0) : new Color(100, 100, 100));
                g2.fillRoundRect(progX + seg * segmentWidth + 2, progY + 2, segmentWidth - 4, progressBarHeight - 4, 3,
                        3);
            }

            int btnX = containerX + containerWidth - buttonWidth;
            boolean canUpgrade = currency > 0 && levels[i] < 10;
            g2.setColor(canUpgrade ? new Color(0, 150, 0) : new Color(100, 100, 100));
            g2.fillRoundRect(btnX, y, buttonWidth, buttonHeight, 10, 10);
            g2.setColor(canUpgrade ? new Color(0, 200, 0) : new Color(150, 150, 150));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(btnX, y, buttonWidth, buttonHeight, 10, 10);

            g2.setColor(Color.WHITE);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16f));
            String btnText = canUpgrade ? "Upgrade (1 point)" : (levels[i] >= 10 ? "Max Level" : "Need Currency");
            fm = g2.getFontMetrics();
            int textX = btnX + (buttonWidth - fm.stringWidth(btnText)) / 2;
            g2.drawString(btnText, textX, y + 32);

            shopButtonRects.add(new Rectangle(btnX, y, buttonWidth, buttonHeight));
        }

        int extraY = yStart + statNames.length * rowSpacing + 30;

        // Buy Health button
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
        g2.drawString("Buy Health +20 HP", containerX, extraY + 20);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 14f));
        g2.setColor(new Color(200, 200, 200));
        g2.drawString("Current: " + (int)player.getHealthLeft() + "/" + (int)player.getMaxHealth(), containerX, extraY + 38);

        int healthBtnX = containerX + containerWidth - buttonWidth;
        boolean canBuyHealth = currency > 0;
        g2.setColor(canBuyHealth ? new Color(150, 0, 0) : new Color(100, 100, 100));
        g2.fillRoundRect(healthBtnX, extraY, buttonWidth, buttonHeight, 10, 10);
        g2.setColor(canBuyHealth ? new Color(200, 0, 0) : new Color(150, 150, 150));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(healthBtnX, extraY, buttonWidth, buttonHeight, 10, 10);

        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16f));
        String healthBtnText = canBuyHealth ? "Buy (1 point)" : "Need Currency";
        fm = g2.getFontMetrics();
        g2.drawString(healthBtnText, healthBtnX + (buttonWidth - fm.stringWidth(healthBtnText)) / 2, extraY + 32);
        shopButtonRects.add(new Rectangle(healthBtnX, extraY, buttonWidth, buttonHeight));

        // Buy Score button
        int scoreY = extraY + rowSpacing;
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
        g2.drawString("Buy Score +10", containerX, scoreY + 20);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 14f));
        g2.setColor(new Color(200, 200, 200));
        g2.drawString("Current Score: " + score, containerX, scoreY + 38);

        int scoreBtnX = containerX + containerWidth - buttonWidth;
        boolean canBuyScore = currency > 0;
        g2.setColor(canBuyScore ? new Color(0, 0, 150) : new Color(100, 100, 100));
        g2.fillRoundRect(scoreBtnX, scoreY, buttonWidth, buttonHeight, 10, 10);
        g2.setColor(canBuyScore ? new Color(0, 0, 200) : new Color(150, 150, 150));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(scoreBtnX, scoreY, buttonWidth, buttonHeight, 10, 10);

        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16f));
        String scoreBtnText = canBuyScore ? "Buy (1 point)" : "Need Currency";
        fm = g2.getFontMetrics();
        g2.drawString(scoreBtnText, scoreBtnX + (buttonWidth - fm.stringWidth(scoreBtnText)) / 2, scoreY + 32);
        shopButtonRects.add(new Rectangle(scoreBtnX, scoreY, buttonWidth, buttonHeight));

        // Continue button
        int continueButtonWidth = 350;
        int continueButtonHeight = 60;
        int continueButtonX = panelX + (panelWidth - continueButtonWidth) / 2;
        int continueButtonY = Math.min(panelY + panelHeight - continueButtonHeight - 20,
                screenHeight - continueButtonHeight - 20);

        boolean isContinueHovered = (hoveredShopButtonIndex == shopButtonRects.size());
        g2.setColor(isContinueHovered ? new Color(0, 180, 0) : new Color(0, 150, 0));
        g2.fillRoundRect(continueButtonX, continueButtonY, continueButtonWidth, continueButtonHeight, 10, 10);
        g2.setColor(isContinueHovered ? new Color(0, 255, 0) : new Color(0, 200, 0));
        g2.setStroke(new BasicStroke(isContinueHovered ? 4 : 3));
        g2.drawRoundRect(continueButtonX, continueButtonY, continueButtonWidth, continueButtonHeight, 10, 10);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24f));
        fm = g2.getFontMetrics();
        String continueText = "Continue to Next Wave";
        int continueTextX = continueButtonX + (continueButtonWidth - fm.stringWidth(continueText)) / 2;
        int continueTextY = continueButtonY + (continueButtonHeight + fm.getAscent()) / 2 - 5;
        g2.setColor(Color.WHITE);
        g2.drawString(continueText, continueTextX, continueTextY);

        shopButtonRects.add(new Rectangle(continueButtonX, continueButtonY, continueButtonWidth, continueButtonHeight));
    }
}
