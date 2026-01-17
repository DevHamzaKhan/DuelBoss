/*
Name: MenuRenderer.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Renders all menu screens with space-theme
*/

package ui;

import entity.Character;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.BasicStroke;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class MenuRenderer {

    // ui color constants
    private static final Color BUTTON_BG_DEFAULT = new Color(30, 30, 50, 200);
    private static final Color BUTTON_BG_HOVER = new Color(50, 50, 70, 220);
    private static final Color BUTTON_BORDER_DEFAULT = new Color(0, 200, 255);
    private static final Color BUTTON_BORDER_HOVER = new Color(100, 255, 255);
    private static final Color SPACE_BG = new Color(10, 10, 30);
    private static final Color STAR_DIM = new Color(255, 255, 255, 200);
    private static final Color STAR_BRIGHT = new Color(255, 255, 255, 255);
    private static final Color PANEL_BG = new Color(30, 30, 50, 220);
    private static final Color TITLE_GLOW = new Color(0, 200, 255, 100);
    private static final Color TITLE_COLOR = new Color(0, 255, 255);
    private static final Color HOWTOPLAY_PANEL = new Color(30, 30, 50, 220);
    private static final Color PLAYER_SPRITE_TINT = new Color(100, 150, 255);
    private static final Color TEXT_DIVIDER = new Color(60, 60, 80);
    private static final Color SHOP_PANEL_BG = new Color(30, 30, 40, 250);
    private static final Color PROGRESS_BAR_BG = new Color(50, 50, 50);
    private static final Color PROGRESS_FILLED = new Color(0, 200, 0);
    private static final Color PROGRESS_EMPTY = new Color(100, 100, 100);
    private static final Color DISABLED_COLOR = new Color(100, 100, 100);
    private static final Color DISABLED_BORDER = new Color(150, 150, 150);
    private static final Color UPGRADE_BG = new Color(0, 150, 0);
    private static final Color UPGRADE_BG_HOVER = new Color(0, 180, 0);
    private static final Color UPGRADE_BORDER = new Color(0, 200, 0);
    private static final Color UPGRADE_BORDER_HOVER = new Color(0, 255, 0);
    private static final Color HEALTH_BG = new Color(150, 0, 0);
    private static final Color HEALTH_BG_HOVER = new Color(180, 0, 0);
    private static final Color HEALTH_BORDER = new Color(200, 0, 0);
    private static final Color HEALTH_BORDER_HOVER = new Color(255, 0, 0);
    private static final Color SCORE_BG = new Color(0, 0, 150);
    private static final Color SCORE_BG_HOVER = new Color(0, 0, 180);
    private static final Color SCORE_BORDER = new Color(0, 0, 200);
    private static final Color SCORE_BORDER_HOVER = new Color(0, 0, 255);
    private static final Color CONTINUE_BG = new Color(0, 150, 0);
    private static final Color CONTINUE_BG_HOVER = new Color(0, 180, 0);
    private static final Color CONTINUE_BORDER = new Color(0, 200, 0);
    private static final Color CONTINUE_BORDER_HOVER = new Color(0, 255, 0);
    private static final Color SUBTEXT_COLOR = new Color(200, 200, 200);

    private final int screenWidth;
    private final int screenHeight;

    private final ButtonManager buttonManager;
    private BufferedImage playerImage = null;

    private static void drawSpaceBackground(Graphics2D g2, int width, int height) {
        g2.setColor(SPACE_BG);
        g2.fillRect(0, 0, width, height);

        // use fixed random seeds so star positions are consistent across redraws
        g2.setColor(STAR_DIM);
        java.util.Random rand = new java.util.Random(12345);
        for (int i = 0; i < 200; i++) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            int size = rand.nextInt(3) + 1;
            g2.fillOval(x, y, size, size);
        }

        g2.setColor(STAR_BRIGHT);
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
        this.buttonManager = new ButtonManager();
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
            buttonManager.updateMenuHover(mouseX, mouseY);
        } else if (isShop) {
            buttonManager.updateShopHover(mouseX, mouseY);
        }
    }

    public int getClickedMenuButton(int x, int y) {
        return buttonManager.getClickedMenuButton(x, y);
    }

    public int getClickedShopButton(int x, int y) {
        return buttonManager.getClickedShopButton(x, y);
    }

    public void drawMainMenu(Graphics2D g2, int highScore) {
        drawSpaceBackground(g2, screenWidth, screenHeight);

        // main menu title with glow effect
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 72f));
        FontMetrics fm = g2.getFontMetrics();
        String title = "POLYGON WARS";
        int titleX = (screenWidth - fm.stringWidth(title)) / 2;
        int titleY = 150;

        g2.setColor(TITLE_GLOW);
        for (int i = 0; i < 5; i++) {
            g2.drawString(title, titleX + i, titleY + i);
        }
        g2.setColor(TITLE_COLOR);
        g2.drawString(title, titleX, titleY);

        // menu buttons (play, how to play, quit)
        int buttonWidth = 300;
        int buttonHeight = 60;
        int buttonSpacing = 30;
        String[] buttonTexts = { "Play", "How to Play", "Quit" };
        int totalButtonHeight = buttonTexts.length * buttonHeight + (buttonTexts.length - 1) * buttonSpacing;
        int startY = (screenHeight - totalButtonHeight - 100) / 2 + 100;

        buttonManager.clearMenuButtons();

        for (int i = 0; i < buttonTexts.length; i++) {
            int x = (screenWidth - buttonWidth) / 2;
            int y = startY + i * (buttonHeight + buttonSpacing);

            if (y + buttonHeight > screenHeight - 80)
                break;

            boolean isHovered = buttonManager.isMenuButtonHovered(i);
            Color bgColor = isHovered ? BUTTON_BG_HOVER : BUTTON_BG_DEFAULT;
            Color borderColor = isHovered ? BUTTON_BORDER_HOVER : BUTTON_BORDER_DEFAULT;

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

            buttonManager.addMenuButton(new Rectangle(x, y, buttonWidth, buttonHeight));
        }

        // high score display
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

        // title
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 48f));
        FontMetrics fm = g2.getFontMetrics();
        String title = "How to Play";
        int titleX = (screenWidth - fm.stringWidth(title)) / 2;
        g2.setColor(Color.WHITE);
        g2.drawString(title, titleX, 50);

        // objective paragraph at top
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 16f));
        fm = g2.getFontMetrics();
        String objective = "OBJECTIVE: Level up your character in this endless game mode. Survive waves of enemies, " +
                "earn currency, and upgrade your abilities to last as long as possible!";
        int objY = 90;
        int objWidth = screenWidth - 100;
        int objX = 50;
        drawWrappedText(g2, objective, objX, objY, objWidth, fm);

        // split screen into left (1/3 character section) and right (2/3 enemy section)
        // with boxes
        int startY = 140;
        int boxSpacing = 20;
        int leftBoxWidth = (screenWidth - boxSpacing * 3) / 3; // Left takes 1/3
        int rightBoxWidth = (screenWidth - boxSpacing * 3) * 2 / 3; // Right takes 2/3
        int boxHeight = screenHeight - startY - 100; // Leave space for back button
        int leftBoxX = boxSpacing;
        int rightBoxX = leftBoxX + leftBoxWidth + boxSpacing;
        int boxY = startY;
        int cornerRadius = 20;

        // left side: character section box
        g2.setColor(PANEL_BG);
        g2.fillRoundRect(leftBoxX, boxY, leftBoxWidth, boxHeight, cornerRadius, cornerRadius);
        g2.setColor(BUTTON_BORDER_DEFAULT);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(leftBoxX, boxY, leftBoxWidth, boxHeight, cornerRadius, cornerRadius);
        drawCharacterSection(g2, leftBoxX + 20, boxY, leftBoxWidth - 40, boxHeight);

        // right side: enemy types section box
        g2.setColor(PANEL_BG);
        g2.fillRoundRect(rightBoxX, boxY, rightBoxWidth, boxHeight, cornerRadius, cornerRadius);
        g2.setColor(BUTTON_BORDER_DEFAULT);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(rightBoxX, boxY, rightBoxWidth, boxHeight, cornerRadius, cornerRadius);
        drawEnemySection(g2, rightBoxX + 20, boxY, rightBoxWidth - 40, boxHeight);

        // back button (smaller)
        int buttonWidth = 250;
        int buttonHeight = 50;
        int buttonX = (screenWidth - buttonWidth) / 2;
        int buttonY = screenHeight - buttonHeight - 40;

        buttonManager.clearMenuButtons();

        boolean isBackHovered = buttonManager.isMenuButtonHovered(0);
        Color backBgColor = isBackHovered ? BUTTON_BG_HOVER : BUTTON_BG_DEFAULT;
        Color backBorderColor = isBackHovered ? BUTTON_BORDER_HOVER : BUTTON_BORDER_DEFAULT;

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

        buttonManager.addMenuButton(new Rectangle(buttonX, buttonY, buttonWidth, buttonHeight));
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
        // heading centered at top
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32f));
        FontMetrics fm = g2.getFontMetrics();
        String heading = "Character";
        int headingX = x + (width - fm.stringWidth(heading)) / 2;
        g2.setColor(Color.WHITE);
        g2.drawString(heading, headingX, y + 35);

        int currentY = y + 60;

        // draw character image
        int imageSize = 80;
        int imageX = x + (width - imageSize) / 2;
        int imageY = currentY;
        if (playerImage != null) {
            g2.drawImage(playerImage, imageX, imageY, imageSize, imageSize, null);
        } else {
            // draw placeholder circle if image not found
            g2.setColor(PLAYER_SPRITE_TINT);
            g2.fillOval(imageX, imageY, imageSize, imageSize);
        }
        currentY += imageSize + 20;

        // controls
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 20f));
        g2.setColor(Color.WHITE);
        g2.drawString("Controls:", x, currentY);
        currentY += 30;

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 16f));
        FontMetrics fmControls = g2.getFontMetrics();
        g2.setColor(Color.WHITE);

        // wasd keys in t-shape: w on top, a-s-d in row below
        int keySize = 35;
        int keySpacing = 10;
        int keysCenterX = x + width / 2;
        int keysY = currentY;

        // draw w centered on top
        drawKey(g2, "W", keysCenterX - keySize / 2, keysY, keySize);
        // draw a, s, d in a row below (s centered, a left, d right)
        int bottomRowY = keysY + keySize + keySpacing;
        drawKey(g2, "A", keysCenterX - keySize - keySpacing - keySize / 2, bottomRowY, keySize);
        drawKey(g2, "S", keysCenterX - keySize / 2, bottomRowY, keySize);
        drawKey(g2, "D", keysCenterX + keySpacing + keySize / 2, bottomRowY, keySize);

        g2.drawString("Move", keysCenterX - fmControls.stringWidth("Move") / 2, bottomRowY + keySize + 20);
        currentY += (keySize + keySpacing) * 2 + 40;

        // auto shoot
        g2.drawString("Auto shoot in direction of mouse", x, currentY);
        currentY += 25;

        // right click for laser beam
        g2.drawString("Right click for laser beam attack", x, currentY);
        currentY += 40;

        // upgrading options
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
        g2.setColor(TEXT_DIVIDER);
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
        // heading centered at top
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

        // left section: regular enemies
        // regular enemies heading
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 22f));
        fm = g2.getFontMetrics();
        String regularHeading = "Regular Enemy";
        int regularHeadingX = leftSectionX + (leftSectionWidth - fm.stringWidth(regularHeading)) / 2;
        g2.setColor(Color.WHITE);
        g2.drawString(regularHeading, regularHeadingX, startY);

        int currentY = startY + 35;

        // row 1: 2 enemies (triangle, square)
        EnemyInfo[] row1 = {
                new EnemyInfo("Triangle", "Chases you", 0),
                new EnemyInfo("Square", "Dodges bullets", 1)
        };
        currentY = drawEnemyRow(g2, leftSectionX, currentY, leftSectionWidth, row1, 2, enemySize);
        currentY += rowSpacing;

        // row 2: 2 enemies (circle, pentagon)
        EnemyInfo[] row2 = {
                new EnemyInfo("Circle", "Explodes in force field", 2),
                new EnemyInfo("Pentagon", "Shoots from distance", 3)
        };
        currentY = drawEnemyRow(g2, leftSectionX, currentY, leftSectionWidth, row2, 2, enemySize);

        // right section: bosses
        // bosses heading
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 22f));
        fm = g2.getFontMetrics();
        String bossesHeading = "Bosses";
        int bossesHeadingX = rightSectionX + (rightSectionWidth - fm.stringWidth(bossesHeading)) / 2;
        g2.setColor(Color.WHITE);
        g2.drawString(bossesHeading, bossesHeadingX, startY);

        currentY = startY + 35;

        // center bosses under heading for visual consistency
        int bossCenterX = bossesHeadingX + fm.stringWidth(bossesHeading) / 2;

        // row 1: 1 enemy (hexagon)
        EnemyInfo[] bossRow1 = {
                new EnemyInfo("Hexagon", "Splits into triangles", 4)
        };
        currentY = drawEnemyRowCentered(g2, bossCenterX, currentY, enemySize, bossRow1[0]);
        currentY += rowSpacing;

        // row 2: 1 enemy (star)
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

            // center enemy image in cell
            int enemyX = cellX + (cellWidth - enemySize) / 2;
            int enemyY = currentY;

            // draw enemy shape
            drawEnemyShape(g2, enemyX, enemyY, enemySize, enemies[i].type);

            // draw enemy name and description centered below image
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

        return currentY + 130; // return next y position with row spacing
    }

    private int drawEnemyRowCentered(Graphics2D g2, int centerX, int y, int enemySize, EnemyInfo enemy) {
        // center enemy image at centerX
        int enemyX = centerX - enemySize / 2;
        int enemyY = y;

        // draw enemy shape
        drawEnemyShape(g2, enemyX, enemyY, enemySize, enemy.type);

        // draw enemy name and description centered below image
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

        // draw different enemy shapes based on type (0=triangle, 1=square, 2=circle,
        // etc)
        switch (type) {
            case 0: // triangle
                int[] triX = { 0, -radius, radius };
                int[] triY = { -radius, radius, radius };
                Polygon triangle = new Polygon(triX, triY, 3);
                g2.setColor(enemy.TriangleEnemy.DEFAULT_COLOR);
                g2.fillPolygon(triangle);
                g2.setColor(Color.DARK_GRAY);
                g2.drawPolygon(triangle);
                break;
            case 1: // square
                int half = radius;
                g2.setColor(enemy.SquareEnemy.DEFAULT_COLOR);
                g2.fillRect(-half, -half, half * 2, half * 2);
                g2.setColor(Color.BLACK);
                g2.drawRect(-half, -half, half * 2, half * 2);
                break;
            case 2: // circle
                g2.setColor(enemy.CircleEnemy.BODY_COLOR);
                g2.fillOval(-radius, -radius, radius * 2, radius * 2);
                g2.setColor(Color.WHITE);
                g2.drawOval(-radius, -radius, radius * 2, radius * 2);
                break;
            case 3: // pentagon (shooter)
                int sides = 5;
                int[] pentX = new int[sides];
                int[] pentY = new int[sides];
                for (int i = 0; i < sides; i++) {
                    double ang = -Math.PI / 2 + i * 2 * Math.PI / sides;
                    pentX[i] = (int) (Math.cos(ang) * radius);
                    pentY[i] = (int) (Math.sin(ang) * radius);
                }
                Polygon pentagon = new Polygon(pentX, pentY, sides);
                g2.setColor(enemy.PentagonEnemy.BODY_COLOR);
                g2.fillPolygon(pentagon);
                g2.setColor(Color.DARK_GRAY);
                g2.drawPolygon(pentagon);
                break;
            case 4: // hexagon
                int hexSides = 6;
                int[] hexX = new int[hexSides];
                int[] hexY = new int[hexSides];
                for (int i = 0; i < hexSides; i++) {
                    double ang = -Math.PI / 2 + i * 2 * Math.PI / hexSides;
                    hexX[i] = (int) (Math.cos(ang) * radius);
                    hexY[i] = (int) (Math.sin(ang) * radius);
                }
                Polygon hex = new Polygon(hexX, hexY, hexSides);
                g2.setColor(enemy.HexagonEnemy.BODY_COLOR);
                g2.fillPolygon(hex);
                g2.setColor(Color.DARK_GRAY);
                g2.drawPolygon(hex);
                break;
            case 5: // octagon
                int octSides = 8;
                int[] octX = new int[octSides];
                int[] octY = new int[octSides];
                for (int i = 0; i < octSides; i++) {
                    double ang = i * (2 * Math.PI / octSides) - Math.PI / 2.0;
                    octX[i] = (int) (Math.cos(ang) * radius);
                    octY[i] = (int) (Math.sin(ang) * radius);
                }
                Polygon octagon = new Polygon(octX, octY, octSides);
                g2.setColor(enemy.OctagonEnemy.DEFAULT_COLOR);
                g2.fillPolygon(octagon);
                g2.setColor(Color.DARK_GRAY);
                g2.drawPolygon(octagon);
                break;
            case 6: // star (spawner)
                int[] starX = new int[10];
                int[] starY = new int[10];
                for (int i = 0; i < 10; i++) {
                    double ang = i * Math.PI / 5.0 - Math.PI / 2.0;
                    double dist = (i % 2 == 0) ? radius : radius * 0.4;
                    starX[i] = (int) (Math.cos(ang) * dist);
                    starY[i] = (int) (Math.sin(ang) * dist);
                }
                Polygon star = new Polygon(starX, starY, 10);
                g2.setColor(enemy.StarEnemy.BODY_COLOR);
                g2.fillPolygon(star);
                g2.setColor(enemy.StarEnemy.BORDER_COLOR);
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

        // game over title
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 64f));
        FontMetrics fm = g2.getFontMetrics();
        String gameOverText = "GAME OVER";
        int goX = (screenWidth - fm.stringWidth(gameOverText)) / 2;
        g2.setColor(Color.RED);
        g2.drawString(gameOverText, goX, 150);

        // final score and wave stats
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 36f));
        fm = g2.getFontMetrics();
        String scoreText = "Final Score: " + score;
        int scoreX = (screenWidth - fm.stringWidth(scoreText)) / 2;
        g2.setColor(Color.WHITE);
        g2.drawString(scoreText, scoreX, 250);

        String waveText = "Wave Reached: " + waveNumber;
        int waveX = (screenWidth - fm.stringWidth(waveText)) / 2;
        g2.drawString(waveText, waveX, 310);

        // high score celebration
        if (score > 0 && score == highScore) {
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32f));
            fm = g2.getFontMetrics();
            String newHighScoreText = "NEW HIGH SCORE!";
            int nhsX = (screenWidth - fm.stringWidth(newHighScoreText)) / 2;
            g2.setColor(Color.YELLOW);
            g2.drawString(newHighScoreText, nhsX, 380);
        }

        // menu and play again buttons
        int buttonWidth = 300;
        int buttonHeight = 60;
        int buttonSpacing = 30;
        String[] buttonTexts = { "Return to Main Menu", "Play Again" };
        int totalButtonHeight = buttonTexts.length * buttonHeight + (buttonTexts.length - 1) * buttonSpacing;
        int startY = Math.min(520, screenHeight - totalButtonHeight - 40);

        buttonManager.clearMenuButtons();

        for (int i = 0; i < buttonTexts.length; i++) {
            int x = (screenWidth - buttonWidth) / 2;
            int y = startY + i * (buttonHeight + buttonSpacing);

            if (y + buttonHeight > screenHeight - 20)
                break;

            boolean isHovered = buttonManager.isMenuButtonHovered(i);
            Color bgColor = isHovered ? BUTTON_BG_HOVER : BUTTON_BG_DEFAULT;
            Color borderColor = isHovered ? BUTTON_BORDER_HOVER : BUTTON_BORDER_DEFAULT;

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

            buttonManager.addMenuButton(new Rectangle(x, y, buttonWidth, buttonHeight));
        }
    }

    public void drawUpgradeShop(Graphics2D g2, Character player, int currency, int score) {
        drawSpaceBackground(g2, screenWidth, screenHeight);

        // center shop panel on screen
        int panelWidth = 800;
        int panelHeight = Math.min(700, screenHeight - 100);
        int panelX = screenWidth / 2 - panelWidth / 2;
        int panelY = (screenHeight - panelHeight) / 2;

        g2.setColor(SHOP_PANEL_BG);
        g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 20, 20);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 20, 20);

        // shop title with currency
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 36f));
        FontMetrics fm = g2.getFontMetrics();
        String title = "Upgrade Shop - Currency: " + currency;
        int titleX = panelX + (panelWidth - fm.stringWidth(title)) / 2;
        g2.setColor(Color.YELLOW);
        g2.drawString(title, titleX, panelY + 50);

        // upgrade stats with progress bars
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

        buttonManager.clearShopButtons();

        for (int i = 0; i < statNames.length; i++) {
            int y = yStart + i * rowSpacing;

            g2.setColor(Color.WHITE);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
            g2.drawString(statNames[i] + " (Lv " + levels[i] + "/10)", containerX, y + 20);

            int progX = containerX;
            int progY = y + 30;
            g2.setColor(PROGRESS_BAR_BG);
            g2.fillRoundRect(progX, progY, progressBarWidth, progressBarHeight, 5, 5);

            // draw 10 segments, fill up to current level
            int segmentWidth = progressBarWidth / 10;
            for (int seg = 0; seg < 10; seg++) {
                g2.setColor(seg < levels[i] ? PROGRESS_FILLED : PROGRESS_EMPTY);
                g2.fillRoundRect(progX + seg * segmentWidth + 2, progY + 2, segmentWidth - 4, progressBarHeight - 4, 3,
                        3);
            }

            int btnX = containerX + containerWidth - buttonWidth;
            boolean canUpgrade = currency > 0 && levels[i] < 10;
            boolean isHovered = buttonManager.isShopButtonHovered(i);
            g2.setColor(
                    canUpgrade ? (isHovered ? UPGRADE_BG_HOVER : UPGRADE_BG) : DISABLED_COLOR);
            g2.fillRoundRect(btnX, y, buttonWidth, buttonHeight, 10, 10);
            g2.setColor(
                    canUpgrade ? (isHovered ? UPGRADE_BORDER_HOVER : UPGRADE_BORDER) : DISABLED_BORDER);
            g2.setStroke(new BasicStroke(isHovered ? 3 : 2));
            g2.drawRoundRect(btnX, y, buttonWidth, buttonHeight, 10, 10);

            g2.setColor(Color.WHITE);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16f));
            String btnText = canUpgrade ? "Upgrade (1 point)" : (levels[i] >= 10 ? "Max Level" : "Need Currency");
            fm = g2.getFontMetrics();
            int textX = btnX + (buttonWidth - fm.stringWidth(btnText)) / 2;
            g2.drawString(btnText, textX, y + 32);

            buttonManager.addShopButton(new Rectangle(btnX, y, buttonWidth, buttonHeight));
        }

        int extraY = yStart + statNames.length * rowSpacing + 30;

        // buy health button
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
        g2.drawString("Buy Health +20 HP", containerX, extraY + 20);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 14f));
        g2.setColor(SUBTEXT_COLOR);
        g2.drawString("Current: " + (int) player.getHealthLeft() + "/" + (int) player.getMaxHealth(), containerX,
                extraY + 38);

        int healthBtnX = containerX + containerWidth - buttonWidth;
        boolean canBuyHealth = currency > 0;
        boolean isHealthHovered = buttonManager.isShopButtonHovered(5);
        g2.setColor(canBuyHealth ? (isHealthHovered ? HEALTH_BG_HOVER : HEALTH_BG)
                : DISABLED_COLOR);
        g2.fillRoundRect(healthBtnX, extraY, buttonWidth, buttonHeight, 10, 10);
        g2.setColor(canBuyHealth ? (isHealthHovered ? HEALTH_BORDER_HOVER : HEALTH_BORDER)
                : DISABLED_BORDER);
        g2.setStroke(new BasicStroke(isHealthHovered ? 3 : 2));
        g2.drawRoundRect(healthBtnX, extraY, buttonWidth, buttonHeight, 10, 10);

        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16f));
        String healthBtnText = canBuyHealth ? "Buy (1 point)" : "Need Currency";
        fm = g2.getFontMetrics();
        g2.drawString(healthBtnText, healthBtnX + (buttonWidth - fm.stringWidth(healthBtnText)) / 2, extraY + 32);
        buttonManager.addShopButton(new Rectangle(healthBtnX, extraY, buttonWidth, buttonHeight));

        // buy score button
        int scoreY = extraY + rowSpacing;
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
        g2.drawString("Buy Score +10", containerX, scoreY + 20);
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 14f));
        g2.setColor(SUBTEXT_COLOR);
        g2.drawString("Current Score: " + score, containerX, scoreY + 38);

        int scoreBtnX = containerX + containerWidth - buttonWidth;
        boolean canBuyScore = currency > 0;
        boolean isScoreHovered = buttonManager.isShopButtonHovered(6);
        g2.setColor(canBuyScore ? (isScoreHovered ? SCORE_BG_HOVER : SCORE_BG)
                : DISABLED_COLOR);
        g2.fillRoundRect(scoreBtnX, scoreY, buttonWidth, buttonHeight, 10, 10);
        g2.setColor(canBuyScore ? (isScoreHovered ? SCORE_BORDER_HOVER : SCORE_BORDER)
                : DISABLED_BORDER);
        g2.setStroke(new BasicStroke(isScoreHovered ? 3 : 2));
        g2.drawRoundRect(scoreBtnX, scoreY, buttonWidth, buttonHeight, 10, 10);

        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16f));
        String scoreBtnText = canBuyScore ? "Buy (1 point)" : "Need Currency";
        fm = g2.getFontMetrics();
        g2.drawString(scoreBtnText, scoreBtnX + (buttonWidth - fm.stringWidth(scoreBtnText)) / 2, scoreY + 32);
        buttonManager.addShopButton(new Rectangle(scoreBtnX, scoreY, buttonWidth, buttonHeight));

        // continue button
        int continueButtonWidth = 350;
        int continueButtonHeight = 60;
        int continueButtonX = panelX + (panelWidth - continueButtonWidth) / 2;
        int continueButtonY = Math.min(panelY + panelHeight - continueButtonHeight - 20,
                screenHeight - continueButtonHeight - 20);

        // continue button is button index 7 (5 upgrades + health + score = 7)
        boolean isContinueHovered = buttonManager.isShopButtonHovered(7);
        g2.setColor(isContinueHovered ? CONTINUE_BG_HOVER : CONTINUE_BG);
        g2.fillRoundRect(continueButtonX, continueButtonY, continueButtonWidth, continueButtonHeight, 10, 10);
        g2.setColor(isContinueHovered ? CONTINUE_BORDER_HOVER : CONTINUE_BORDER);
        g2.setStroke(new BasicStroke(isContinueHovered ? 4 : 3));
        g2.drawRoundRect(continueButtonX, continueButtonY, continueButtonWidth, continueButtonHeight, 10, 10);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24f));
        fm = g2.getFontMetrics();
        String continueText = "Continue to Next Wave";
        int continueTextX = continueButtonX + (continueButtonWidth - fm.stringWidth(continueText)) / 2;
        int continueTextY = continueButtonY + (continueButtonHeight + fm.getAscent()) / 2 - 5;
        g2.setColor(Color.WHITE);
        g2.drawString(continueText, continueTextX, continueTextY);

        buttonManager.addShopButton(
                new Rectangle(continueButtonX, continueButtonY, continueButtonWidth, continueButtonHeight));
    }
}
