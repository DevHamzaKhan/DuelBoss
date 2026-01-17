/*
Name: GameOverPanel.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Game over screen.
*/

package ui;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class GameOverPanel extends JPanel {

  private final MenuRenderer menuRenderer;
  private int score;
  private int waveNumber;
  private int highScore;
  private GameOverListener listener;

  public interface GameOverListener {
    void onMainMenuClicked();

    void onPlayAgainClicked();
  }

  public GameOverPanel(int screenWidth, int screenHeight) {
    this.menuRenderer = new MenuRenderer(screenWidth, screenHeight);

    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        menuRenderer.updateHover(e.getX(), e.getY(), true, false);
        repaint();
      }
    });

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        int button = menuRenderer.getClickedMenuButton(e.getX(), e.getY());
        if (listener != null) {
          if (button == 0)
            listener.onMainMenuClicked();
          else if (button == 1)
            listener.onPlayAgainClicked();
        }
      }
    });
  }

  public void setGameOverData(int score, int waveNumber, int highScore) {
    this.score = score;
    this.waveNumber = waveNumber;
    this.highScore = highScore;
  }

  public void setListener(GameOverListener listener) {
    this.listener = listener;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    menuRenderer.drawGameOver(g2, score, waveNumber, highScore);
  }
}
