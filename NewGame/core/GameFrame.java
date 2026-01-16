/*
Name: GameFrame.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Main window container for the game. Configures window properties (size, resizable, close operation) and hosts the GamePanel. Defines standard 1440x810 resolution.
*/

package core;

import javax.swing.JFrame;

public class GameFrame extends JFrame {

    // 16:9 aspect ratio for modern displays
    public static final int SCREEN_WIDTH = 1440;
    public static final int SCREEN_HEIGHT = 810;

    public GameFrame() {
        super("DuelBoss - New Game");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        setLocationRelativeTo(null);

        GamePanel gamePanel = new GamePanel(SCREEN_WIDTH, SCREEN_HEIGHT);
        setContentPane(gamePanel);
    }
}


