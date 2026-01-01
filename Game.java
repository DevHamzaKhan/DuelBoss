/*
Programmers: Hamza Khan & Alec Li
Program Name: Game
Program Date: 2025-12-31
Program Description: Main game loop and logic
*/

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class Game extends JPanel {
	private Timer timer;
	private Map currentMap;
	private final GamePanel gamePanel;
	
	private final JButton fireButton;
	private final JButton iceButton;
	private final JButton lightningButton;
	private final JButton waterButton;
	private final JButton earthButton;
    
    public Game() {
		setLayout(new BorderLayout());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		
		fireButton = new JButton("Fire");
		iceButton = new JButton("Ice");
		lightningButton = new JButton("Lightning");
		waterButton = new JButton("Water");
		earthButton = new JButton("Earth");
		
		fireButton.addActionListener(e -> switchMap(new FireMap()));
		iceButton.addActionListener(e -> switchMap(new IceMap()));
		lightningButton.addActionListener(e -> switchMap(new LightningMap()));
		waterButton.addActionListener(e -> switchMap(new WaterMap()));
		earthButton.addActionListener(e -> switchMap(new EarthMap()));
		
		buttonPanel.add(fireButton);
		buttonPanel.add(iceButton);
		buttonPanel.add(lightningButton);
		buttonPanel.add(waterButton);
		buttonPanel.add(earthButton);
		
		add(buttonPanel, BorderLayout.NORTH);
		
		gamePanel = new GamePanel();
		add(gamePanel, BorderLayout.CENTER);
		
		currentMap = new DefaultMap();
		switchMap(currentMap);
		
		initialize();
    }
	
	private void switchMap(Map map) {
		currentMap = map;
		gamePanel.platforms = map.getPlatforms();
		gamePanel.background = map.getBackground();
		gamePanel.repaint();
	}
	
	private void initialize() {
        timer = new Timer(10, (ActionEvent e) -> {
            gamePanel.repaint();
        });
		timer.start();
	}
	
	private class GamePanel extends JPanel {
		private Platform[] platforms;
		private GameImage background;
		
		public GamePanel() {
			setPreferredSize(new Dimension(Main.WIDTH, Main.HEIGHT));
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			Graphics2D graphics2d = (Graphics2D) g;

			if (background != null) {
				background.draw(g);
			}

			if (platforms != null) {
				for (Platform p : platforms) {
					if (p.getActive() == 1)
						p.draw(graphics2d);
				}
			}
		}
	}
}
