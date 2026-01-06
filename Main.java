/*
Programmers: Hamza Khan & Alec Li
Program Name: Main
Program Date: 2025-12-31
Program Description: Entry point for the DuelBoss game
*/

import java.awt.*;
import javax.swing.*;

public class Main {
    public static final int WIDTH = 960;
    public static final int HEIGHT = 520;
	
    public static void main(String[] args) {
        SoundManager.init();
        SoundManager.playMusic();

        JFrame f = new JFrame("DuelBoss");

		Game gameScreen = new Game();

        f.add(gameScreen, BorderLayout.CENTER);

        f.setVisible(true);
        f.setSize(WIDTH, HEIGHT);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setResizable(false);
    }
}
