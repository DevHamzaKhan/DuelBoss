package core;

/*
Name: Game.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Main entry point, screen management.
*/

import ui.*;
import entity.Character;
import javax.swing.JFrame;
import java.awt.CardLayout;
import java.awt.Container;

public class Game extends JFrame {

    // 16:9 aspect ratio
    public static final int SCREEN_WIDTH = 1440;
    public static final int SCREEN_HEIGHT = 810;

    // cardlayout manages screen transitions
    private final CardLayout cardLayout;
    private final Container cardContainer;

    // screen identifiers
    private static final String MAIN_MENU = "MAIN_MENU";
    private static final String HOW_TO_PLAY = "HOW_TO_PLAY";
    private static final String PLAYING = "PLAYING";
    private static final String SHOP = "SHOP";
    private static final String GAME_OVER = "GAME_OVER";

    // screen panels
    private MainMenuPanel mainMenuPanel;
    private HowToPlayPanel howToPlayPanel;
    private GamePanel gamePanel;
    private ShopPanel shopPanel;
    private GameOverPanel gameOverPanel;

    public Game() {
        super("DuelBoss - New Game");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        setLocationRelativeTo(null);

        // setup cardlayout for screen management
        cardLayout = new CardLayout();
        cardContainer = getContentPane();
        cardContainer.setLayout(cardLayout);

        initializePanels();
        setupListeners();

        cardLayout.show(cardContainer, MAIN_MENU);
    }

    private void initializePanels() {
        mainMenuPanel = new MainMenuPanel(SCREEN_WIDTH, SCREEN_HEIGHT);
        howToPlayPanel = new HowToPlayPanel(SCREEN_WIDTH, SCREEN_HEIGHT);
        gamePanel = new GamePanel(SCREEN_WIDTH, SCREEN_HEIGHT);
        shopPanel = new ShopPanel(SCREEN_WIDTH, SCREEN_HEIGHT);
        gameOverPanel = new GameOverPanel(SCREEN_WIDTH, SCREEN_HEIGHT);

        cardContainer.add(mainMenuPanel, MAIN_MENU);
        cardContainer.add(howToPlayPanel, HOW_TO_PLAY);
        cardContainer.add(gamePanel, PLAYING);
        cardContainer.add(shopPanel, SHOP);
        cardContainer.add(gameOverPanel, GAME_OVER);
    }

    private void setupListeners() {
        // main menu event handling
        mainMenuPanel.setListener(new MainMenuPanel.ScreenSwitchListener() {
            @Override
            public void onPlayClicked() {
                gamePanel.startNewGame();
                cardLayout.show(cardContainer, PLAYING);
                gamePanel.requestFocusInWindow();
            }

            @Override
            public void onHowToPlayClicked() {
                cardLayout.show(cardContainer, HOW_TO_PLAY);
            }

            @Override
            public void onQuitClicked() {
                System.exit(0);
            }
        });

        // how to play event handling
        howToPlayPanel.setListener(() -> {
            mainMenuPanel.setHighScore(gamePanel.getHighScore());
            cardLayout.show(cardContainer, MAIN_MENU);
        });

        // game panel events
        gamePanel.setGameListener(new GamePanel.GameListener() {
            @Override
            public void onShopOpen(Character player, int currency, int score) {
                gamePanel.pauseGame();
                shopPanel.setShopData(player, currency, score);
                shopPanel.repaint();
                cardLayout.show(cardContainer, SHOP);
            }

            @Override
            public void onGameOver(int score, int waveNumber, int highScore) {
                gamePanel.stopGame();
                gameOverPanel.setGameOverData(score, waveNumber, highScore);
                gameOverPanel.repaint();
                cardLayout.show(cardContainer, GAME_OVER);
            }
        });

        // shop event handling
        shopPanel.setListener(new ShopPanel.ShopListener() {
            @Override
            public void onUpgradeClicked(int upgradeIndex) {
                gamePanel.handleShopPurchase(upgradeIndex);
                shopPanel.setShopData(gamePanel.getPlayer(), gamePanel.getCurrency(), gamePanel.getScore());
                shopPanel.repaint();
            }

            @Override
            public void onBuyHealthClicked() {
                gamePanel.handleShopPurchase(5);
                shopPanel.setShopData(gamePanel.getPlayer(), gamePanel.getCurrency(), gamePanel.getScore());
                shopPanel.repaint();
            }

            @Override
            public void onBuyScoreClicked() {
                gamePanel.handleShopPurchase(6);
                shopPanel.setShopData(gamePanel.getPlayer(), gamePanel.getCurrency(), gamePanel.getScore());
                shopPanel.repaint();
            }

            @Override
            public void onContinueClicked() {
                gamePanel.resumeFromShop();
                gamePanel.resumeGame();
                cardLayout.show(cardContainer, PLAYING);
                gamePanel.requestFocusInWindow();
            }
        });

        // game over event handling
        gameOverPanel.setListener(new GameOverPanel.GameOverListener() {
            @Override
            public void onMainMenuClicked() {
                mainMenuPanel.setHighScore(gamePanel.getHighScore());
                cardLayout.show(cardContainer, MAIN_MENU);
            }

            @Override
            public void onPlayAgainClicked() {
                gamePanel.startNewGame();
                cardLayout.show(cardContainer, PLAYING);
                gamePanel.requestFocusInWindow();
            }
        });
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.setVisible(true);
    }
}
