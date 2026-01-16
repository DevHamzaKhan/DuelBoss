/*
Name: UIOverlay.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Renders in-game overlay elements (health bar, timer, wave info, enemy counter).
*/

package ui;

import entity.Character;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class UIOverlay {

    private final Character player;

    public UIOverlay(Character player) {
        this.player = player;
    }

    public void draw(Graphics2D g2,
            int screenWidth,
            int screenHeight,
            int waveNumber,
            long waveStartTime,
            long currentTime,
            int enemiesRemaining,
            String waveStatusText) {

        drawHealthBar(g2, screenWidth, screenHeight);
        drawTimerTopLeft(g2, waveStartTime, currentTime);
        drawWaveInfo(g2, screenWidth, waveNumber, waveStatusText);
        drawEnemyCounter(g2, screenWidth, enemiesRemaining);
    }

    private void drawHealthBar(Graphics2D g2, int screenWidth, int screenHeight) {
        // center health bar at bottom of screen
        int width = 400;
        int height = 30;
        int x = (screenWidth - width) / 2;
        int y = screenHeight - height - 100;

        // calculate health percentage and clamp between 0 and 1
        double healthPercent = player.getHealthLeft() / player.getMaxHealth();
        healthPercent = Math.max(0, Math.min(1, healthPercent));

        // draw layered health bar: shadow, empty bar, filled bar
        g2.setColor(new Color(40, 40, 40, 220));
        g2.fillRoundRect(x - 3, y - 3, width + 6, height + 6, 10, 10);

        g2.setColor(new Color(80, 0, 0));
        g2.fillRoundRect(x, y, width, height, 8, 8);

        int filledWidth = (int) (width * healthPercent);
        g2.setColor(new Color(0, 200, 0));
        g2.fillRoundRect(x, y, filledWidth, height, 8, 8);

        g2.setColor(Color.WHITE);
        g2.drawRoundRect(x, y, width, height, 8, 8);

        String text = (int) player.getHealthLeft() + " / " + (int) player.getMaxHealth();
        Font font = g2.getFont().deriveFont(Font.BOLD, 16f);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (width - fm.stringWidth(text)) / 2;
        int ty = y + (height + fm.getAscent()) / 2 - 4;

        // draw text with shadow effect for readability
        g2.setColor(Color.BLACK);
        g2.drawString(text, tx + 1, ty + 1);
        g2.setColor(Color.WHITE);
        g2.drawString(text, tx, ty);
    }

    private void drawWaveInfo(Graphics2D g2, int screenWidth, int waveNumber, String waveStatusText) {
        // format wave text with optional status message
        String text = "Wave " + waveNumber;
        if (waveStatusText != null && !waveStatusText.isEmpty()) {
            text += " - " + waveStatusText;
        }
        Font font = g2.getFont().deriveFont(Font.BOLD, 22f);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

        // center wave info at top of screen
        int textWidth = fm.stringWidth(text);
        int x = (screenWidth - textWidth) / 2;
        int y = 40;

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(x - 12, y - fm.getAscent(), textWidth + 24, fm.getHeight() + 4, 12, 12);

        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);
    }

    private void drawTimerTopLeft(Graphics2D g2, long waveStartTime, long currentTime) {
        // convert elapsed milliseconds to mm:ss format
        long elapsedMs = currentTime - waveStartTime;
        long seconds = elapsedMs / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        String text = String.format("Wave Time: %02d:%02d", minutes, seconds);
        Font font = g2.getFont().deriveFont(Font.BOLD, 18f);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

        int x = 20;
        int y = 30;
        int textWidth = fm.stringWidth(text);

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(x - 10, y - fm.getAscent(), textWidth + 20, fm.getHeight() + 4, 10, 10);

        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);
    }

    private void drawEnemyCounter(Graphics2D g2, int screenWidth, int enemiesRemaining) {
        String text = "Enemies left: " + enemiesRemaining;
        Font font = g2.getFont().deriveFont(Font.BOLD, 18f);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

        // position counter at top-right corner
        int padding = 20;
        int textWidth = fm.stringWidth(text);
        int x = screenWidth - textWidth - padding;
        int y = 30;

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(x - 10, y - fm.getAscent(), textWidth + 20, fm.getHeight() + 4, 10, 10);

        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);
    }
}
