/*
Name: ShopPanel.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Upgrade shop screen.
*/

package ui;

import entity.Character;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class ShopPanel extends JPanel {

  private final MenuRenderer menuRenderer;
  private Character player;
  private int currency;
  private int score;
  private ShopListener listener;

  public interface ShopListener {
    void onUpgradeClicked(int upgradeIndex);

    void onBuyHealthClicked();

    void onBuyScoreClicked();

    void onContinueClicked();
  }

  public ShopPanel(int screenWidth, int screenHeight) {
    this.menuRenderer = new MenuRenderer(screenWidth, screenHeight);

    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        menuRenderer.updateHover(e.getX(), e.getY(), false, true);
        repaint();
      }
    });

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        int button = menuRenderer.getClickedShopButton(e.getX(), e.getY());
        if (listener != null) {
          if (button >= 0 && button <= 4) {
            listener.onUpgradeClicked(button);
          } else if (button == 5) {
            listener.onBuyHealthClicked();
          } else if (button == 6) {
            listener.onBuyScoreClicked();
          } else if (button == 7) {
            listener.onContinueClicked();
          }
        }
      }
    });
  }

  public void setShopData(Character player, int currency, int score) {
    this.player = player;
    this.currency = currency;
    this.score = score;
  }

  public void setListener(ShopListener listener) {
    this.listener = listener;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (player == null)
      return;
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    menuRenderer.drawUpgradeShop(g2, player, currency, score);
  }
}
