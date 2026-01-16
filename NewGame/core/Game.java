package core;

/*
Name: Game.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Main entry point for Polygon Wars
*/

import javax.swing.JFrame;

public class Game extends JFrame {

    // 16:9 aspect ratio
    public static final int SCREEN_WIDTH = 1440;
    public static final int SCREEN_HEIGHT = 810;

    public Game() {
        super("DuelBoss - New Game");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        setLocationRelativeTo(null);

        GamePanel gamePanel = new GamePanel(SCREEN_WIDTH, SCREEN_HEIGHT);
        setContentPane(gamePanel);
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.setVisible(true);
    }
}
