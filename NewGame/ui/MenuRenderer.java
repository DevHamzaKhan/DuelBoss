package ui;

import ui.ButtonManager;
import entity.Character;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.BasicStroke;
import java.util.ArrayList;
import java.util.List;

public class MenuRenderer {

    private final int screenWidth;
    private final int screenHeight;

    private final ButtonManager buttonManager;

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
        this.buttonManager = new ButtonManager();
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

        buttonManager.clearMenuButtons();

        for (int i = 0; i < buttonTexts.length; i++) {
            int x = (screenWidth - buttonWidth) / 2;
            int y = startY + i * (buttonHeight + buttonSpacing);

            if (y + buttonHeight > screenHeight - 80)
                break;

            boolean isHovered = buttonManager.isMenuButtonHovered(i);
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

            buttonManager.addMenuButton(new Rectangle(x, y, buttonWidth, buttonHeight));
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

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 48f));
        FontMetrics fm = g2.getFontMetrics();
        String title = "How to Play";
        int titleX = (screenWidth - fm.stringWidth(title)) / 2;
        g2.setColor(new Color(0, 255, 255));
        g2.drawString(title, titleX, 60);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 18f));
        fm = g2.getFontMetrics();
        String[] instructions = {
                "CONTROLS:",
                "WASD - Move character",
                "Mouse - Aim and shoot automatically",
                "",
                "OBJECTIVE:",
                "Survive waves of enemies",
                "Clear all enemies to advance to the next wave",
                "Earn currency to upgrade your character",
                "",
                "ENEMIES:",
                "Triangle - Chases you",
                "Square - Chases you and dodges bullets",
                "Circle - Explodes when you're in its force field",
                "Pentagon - Shoots at you from distance",
                "Hexagon - Splits into 6 triangles when destroyed"
        };

        int y = 120;
        int lineHeight = fm.getHeight() + 5;
        int maxY = screenHeight - 120;

        for (String line : instructions) {
            if (y + lineHeight > maxY)
                break;

            if (line.endsWith(":") && !line.isEmpty()) {
                g2.setColor(new Color(0, 255, 255));
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 20f));
            } else {
                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 16f));
            }
            fm = g2.getFontMetrics();
            int x = (screenWidth - fm.stringWidth(line)) / 2;
            g2.drawString(line, x, y);
            y += (line.isEmpty() ? lineHeight / 2 : lineHeight);
        }

        int buttonWidth = 300;
        int buttonHeight = 60;
        int buttonX = (screenWidth - buttonWidth) / 2;
        int buttonY = screenHeight - buttonHeight - 60;

        buttonManager.clearMenuButtons();

        boolean isBackHovered = buttonManager.isMenuButtonHovered(0);
        Color backBgColor = isBackHovered ? new Color(50, 50, 70, 220) : new Color(30, 30, 50, 200);
        Color backBorderColor = isBackHovered ? new Color(100, 255, 255) : new Color(0, 200, 255);

        g2.setColor(backBgColor);
        g2.fillRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 15, 15);
        g2.setColor(backBorderColor);
        g2.setStroke(new BasicStroke(isBackHovered ? 4 : 3));
        g2.drawRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 15, 15);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 28f));
        fm = g2.getFontMetrics();
        String backText = "Back to Main Menu";
        int textX = buttonX + (buttonWidth - fm.stringWidth(backText)) / 2;
        int textY = buttonY + (buttonHeight + fm.getAscent()) / 2 - 5;
        g2.setColor(Color.WHITE);
        g2.drawString(backText, textX, textY);

        buttonManager.addMenuButton(new Rectangle(buttonX, buttonY, buttonWidth, buttonHeight));
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

        buttonManager.clearMenuButtons();

        for (int i = 0; i < buttonTexts.length; i++) {
            int x = (screenWidth - buttonWidth) / 2;
            int y = startY + i * (buttonHeight + buttonSpacing);

            if (y + buttonHeight > screenHeight - 20)
                break;

            boolean isHovered = buttonManager.isMenuButtonHovered(i);
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

            buttonManager.addMenuButton(new Rectangle(x, y, buttonWidth, buttonHeight));
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

        buttonManager.clearShopButtons();

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

            buttonManager.addShopButton(new Rectangle(btnX, y, buttonWidth, buttonHeight));
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
        buttonManager.addShopButton(new Rectangle(healthBtnX, extraY, buttonWidth, buttonHeight));

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
        buttonManager.addShopButton(new Rectangle(scoreBtnX, scoreY, buttonWidth, buttonHeight));

        // Continue button
        int continueButtonWidth = 350;
        int continueButtonHeight = 60;
        int continueButtonX = panelX + (panelWidth - continueButtonWidth) / 2;
        int continueButtonY = Math.min(panelY + panelHeight - continueButtonHeight - 20,
                screenHeight - continueButtonHeight - 20);

        // Continue button is button index 7 (5 upgrades + health + score = 7)
        boolean isContinueHovered = buttonManager.isShopButtonHovered(7);
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
        
        buttonManager.addShopButton(new Rectangle(continueButtonX, continueButtonY, continueButtonWidth, continueButtonHeight));
    }
}
