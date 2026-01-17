/*
Name: HowToPlayPanel.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: How to play screen.
*/

package ui;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class HowToPlayPanel extends JPanel {

  private final MenuRenderer menuRenderer;
  private BackListener listener;

  public interface BackListener {
    void onBackClicked();
  }

  public HowToPlayPanel(int screenWidth, int screenHeight) {
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
        if (button == 0 && listener != null) {
          listener.onBackClicked();
        }
      }
    });
  }

  public void setListener(BackListener listener) {
    this.listener = listener;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    menuRenderer.drawHowToPlay(g2);
  }
}
