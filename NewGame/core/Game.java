package core;

/*
Name: Game.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Main entry point for Polygon Wars. Launches the game window on the Swing event dispatch thread for thread-safe GUI operations.
*/

import javax.swing.SwingUtilities;

public class Game {

    // application entry point - starts the game by creating and displaying the main frame
    public static void main(String[] args) {
        // invokelater ensures gui creation happens on the event dispatch thread (thread-safe)
        SwingUtilities.invokeLater(() -> {
            GameFrame frame = new GameFrame();
            frame.setVisible(true);
        });
    }
}
