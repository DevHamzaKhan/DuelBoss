package com.polygonwars.ui;

import com.polygonwars.entity.Character;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Renders the HUD (Heads-Up Display) including score, stats, and beam cooldown.
 * Extracts rendering responsibilities from GamePanel.
 */
public class HUDRenderer {
    
    // Beam cooldown bar constants
    private static final int BEAM_BAR_WIDTH = 200;
    private static final int BEAM_BAR_HEIGHT = 20;
    private static final int BEAM_BAR_MARGIN = 20;
    
    // Score panel constants
    private static final int SCORE_X = 20;
    private static final int SCORE_Y = 70;
    private static final int STATS_LINE_HEIGHT = 28;
    
    // Beam cooldown constants
    private static final long ULTIMATE_COOLDOWN_MS = 10_000; // 10 seconds
    private static final int SCORE_POINTS_PER_CURRENCY = 10;
    
    /**
     * Draws the complete HUD overlay.
     */
    public void drawHUD(Graphics2D g2, int screenWidth, int screenHeight, 
                        int score, int currency, Character player,
                        long lastUltimateTime, long currentTime) {
        drawScore(g2, score, currency, player);
        drawBeamCooldown(g2, screenWidth, screenHeight, lastUltimateTime, currentTime);
    }
    
    /**
     * Draws the beam ability cooldown bar.
     */
    public void drawBeamCooldown(Graphics2D g2, int screenWidth, int screenHeight,
                                  long lastUltimateTime, long currentTime) {
        long timeSinceLastUltimate = currentTime - lastUltimateTime;
        double cooldownProgress = Math.min(1.0, (double) timeSinceLastUltimate / ULTIMATE_COOLDOWN_MS);
        
        int barX = screenWidth - BEAM_BAR_WIDTH - BEAM_BAR_MARGIN;
        int barY = screenHeight - BEAM_BAR_HEIGHT - BEAM_BAR_MARGIN;
        
        // Background
        g2.setColor(new Color(40, 40, 40, 220));
        g2.fillRoundRect(barX - 3, barY - 3, BEAM_BAR_WIDTH + 6, BEAM_BAR_HEIGHT + 6, 8, 8);
        
        // Cooldown background
        g2.setColor(new Color(60, 0, 0));
        g2.fillRoundRect(barX, barY, BEAM_BAR_WIDTH, BEAM_BAR_HEIGHT, 6, 6);
        
        // Progress fill
        int filledWidth = (int) (BEAM_BAR_WIDTH * cooldownProgress);
        if (filledWidth > 0) {
            Color fillColor = cooldownProgress >= 1.0 ? new Color(0, 200, 0) : new Color(255, 150, 0);
            g2.setColor(fillColor);
            g2.fillRoundRect(barX, barY, filledWidth, BEAM_BAR_HEIGHT, 6, 6);
        }
        
        // Border
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(barX, barY, BEAM_BAR_WIDTH, BEAM_BAR_HEIGHT, 6, 6);
        
        // Text
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
    
    /**
     * Draws the score and player stats panel.
     */
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
}
