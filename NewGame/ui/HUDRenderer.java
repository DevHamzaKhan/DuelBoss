/*
Name: HUDRenderer.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Renders in-game UI overlay
*/

package ui;

import entity.Character;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class HUDRenderer {

    // beam cooldown bar constants
    private static final int BEAM_BAR_WIDTH = 200;
    private static final int BEAM_BAR_HEIGHT = 20;
    private static final int BEAM_BAR_MARGIN = 40;

    // score panel constants
    private static final int SCORE_X = 20;
    private static final int SCORE_Y = 70;
    private static final int STATS_LINE_HEIGHT = 28;

    // health bar constants
    private static final int HEALTH_BAR_WIDTH = 400;
    private static final int HEALTH_BAR_HEIGHT = 30;
    private static final int HEALTH_BAR_BOTTOM_MARGIN = 100;

    // beam cooldown constants
    private static final long ULTIMATE_COOLDOWN_MS = 10_000; // 10 seconds
    private static final int SCORE_POINTS_PER_CURRENCY = 10;

    // draws the complete hud overlay
    public void drawHUD(Graphics2D g2, int screenWidth, int screenHeight,
            int score, int currency, Character player,
            long lastUltimateTime, long currentTime,
            int waveNumber, long waveStartTime, int enemiesRemaining, String waveStatusText) {
        drawScore(g2, score, currency, player);
        drawBeamCooldown(g2, screenWidth, screenHeight, lastUltimateTime, currentTime);
        drawHealthBar(g2, screenWidth, screenHeight, player);
        drawTimerTopLeft(g2, waveStartTime, currentTime);
        drawWaveInfo(g2, screenWidth, waveNumber, waveStatusText);
        drawEnemyCounter(g2, screenWidth, enemiesRemaining);
    }

    // draws the beam ability cooldown bar
    public void drawBeamCooldown(Graphics2D g2, int screenWidth, int screenHeight,
            long lastUltimateTime, long currentTime) {
        long timeSinceLastUltimate = currentTime - lastUltimateTime;
        double cooldownProgress = Math.min(1.0, (double) timeSinceLastUltimate / ULTIMATE_COOLDOWN_MS);

        // position centered above health bar
        int barX = (screenWidth - BEAM_BAR_WIDTH) / 2;
        int barY = screenHeight - 170; // 40 pixels above health bar

        // background
        g2.setColor(new Color(40, 40, 40, 220));
        g2.fillRoundRect(barX - 3, barY - 3, BEAM_BAR_WIDTH + 6, BEAM_BAR_HEIGHT + 6, 8, 8);

        // cooldown background
        g2.setColor(new Color(60, 0, 0));
        g2.fillRoundRect(barX, barY, BEAM_BAR_WIDTH, BEAM_BAR_HEIGHT, 6, 6);

        // progress fill
        int filledWidth = (int) (BEAM_BAR_WIDTH * cooldownProgress);
        if (filledWidth > 0) {
            Color fillColor = cooldownProgress >= 1.0 ? new Color(0, 200, 0) : new Color(255, 150, 0);
            g2.setColor(fillColor);
            g2.fillRoundRect(barX, barY, filledWidth, BEAM_BAR_HEIGHT, 6, 6);
        }

        // border
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(barX, barY, BEAM_BAR_WIDTH, BEAM_BAR_HEIGHT, 6, 6);

        // text
        String text = cooldownProgress >= 1.0 ? "BEAM READY"
                : String.format("BEAM: %.1fs", (ULTIMATE_COOLDOWN_MS - timeSinceLastUltimate) / 1000.0);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
        FontMetrics fm = g2.getFontMetrics();
        int textX = barX + (BEAM_BAR_WIDTH - fm.stringWidth(text)) / 2;
        int textY = barY + (BEAM_BAR_HEIGHT + fm.getAscent()) / 2 - 2;

        g2.setColor(Color.BLACK);
        g2.drawString(text, textX + 1, textY + 1);
        g2.setColor(Color.WHITE);
        g2.drawString(text, textX, textY);
    }

    // draws the score and player stats panel
    public void drawScore(Graphics2D g2, int score, int currency, Character player) {
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16f));
        FontMetrics fm = g2.getFontMetrics();
        String scoreText = "Score: " + score;

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(SCORE_X - 10, SCORE_Y - fm.getAscent() - 5,
                fm.stringWidth(scoreText) + 20, fm.getHeight() + 10, 10, 10);
        g2.setColor(Color.WHITE);
        g2.drawString(scoreText, SCORE_X, SCORE_Y);

        int statsStartY = SCORE_Y + 40;
        String[] stats = {
                "Currency: " + currency + " points",
                "Max Health: " + (int) player.getMaxHealth() + " (Lv " + player.getMaxHealthLevel() + ")",
                "Bullet Speed: Lv " + player.getBulletSpeedLevel(),
                "Fire Rate: Lv " + player.getFireRateLevel(),
                "Movement Speed: Lv " + player.getMovementSpeedLevel(),
                "Bullet Damage: Lv " + player.getBulletDamageLevel()
        };

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
        fm = g2.getFontMetrics();
        for (int i = 0; i < stats.length; i++) {
            int lineY = statsStartY + i * STATS_LINE_HEIGHT;
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRoundRect(SCORE_X - 10, lineY - fm.getAscent() - 2,
                    fm.stringWidth(stats[i]) + 20, fm.getHeight() + 4, 5, 5);
            g2.setColor(new Color(200, 200, 200));
            g2.drawString(stats[i], SCORE_X, lineY);
        }
    }

    // draws player health bar centered at bottom of screen
    private void drawHealthBar(Graphics2D g2, int screenWidth, int screenHeight, Character player) {
        int x = (screenWidth - HEALTH_BAR_WIDTH) / 2;
        int y = screenHeight - HEALTH_BAR_HEIGHT - HEALTH_BAR_BOTTOM_MARGIN;

        // calculate health percentage and clamp between 0 and 1
        double healthPercent = player.getHealthLeft() / player.getMaxHealth();
        healthPercent = Math.max(0, Math.min(1, healthPercent));

        // draw layered health bar: shadow, empty bar, filled bar
        g2.setColor(new Color(40, 40, 40, 220));
        g2.fillRoundRect(x - 3, y - 3, HEALTH_BAR_WIDTH + 6, HEALTH_BAR_HEIGHT + 6, 10, 10);

        g2.setColor(new Color(80, 0, 0));
        g2.fillRoundRect(x, y, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT, 8, 8);

        int filledWidth = (int) (HEALTH_BAR_WIDTH * healthPercent);
        g2.setColor(new Color(0, 200, 0));
        g2.fillRoundRect(x, y, filledWidth, HEALTH_BAR_HEIGHT, 8, 8);

        g2.setColor(Color.WHITE);
        g2.drawRoundRect(x, y, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT, 8, 8);

        String text = (int) player.getHealthLeft() + " / " + (int) player.getMaxHealth();
        Font font = g2.getFont().deriveFont(Font.BOLD, 16f);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (HEALTH_BAR_WIDTH - fm.stringWidth(text)) / 2;
        int ty = y + (HEALTH_BAR_HEIGHT + fm.getAscent()) / 2 - 4;

        // draw text with shadow effect for readability
        g2.setColor(Color.BLACK);
        g2.drawString(text, tx + 1, ty + 1);
        g2.setColor(Color.WHITE);
        g2.drawString(text, tx, ty);
    }

    // draws wave information centered at top of screen
    private void drawWaveInfo(Graphics2D g2, int screenWidth, int waveNumber, String waveStatusText) {
        String text = "Wave " + waveNumber;
        if (waveStatusText != null && !waveStatusText.isEmpty()) {
            text += " - " + waveStatusText;
        }
        Font font = g2.getFont().deriveFont(Font.BOLD, 22f);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

        int textWidth = fm.stringWidth(text);
        int x = (screenWidth - textWidth) / 2;
        int y = 40;

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(x - 12, y - fm.getAscent(), textWidth + 24, fm.getHeight() + 4, 12, 12);

        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);
    }

    // draws elapsed wave time at top-left corner
    private void drawTimerTopLeft(Graphics2D g2, long waveStartTime, long currentTime) {
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

    // draws remaining enemy count at top-right corner
    private void drawEnemyCounter(Graphics2D g2, int screenWidth, int enemiesRemaining) {
        String text = "Enemies left: " + enemiesRemaining;
        Font font = g2.getFont().deriveFont(Font.BOLD, 18f);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

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
