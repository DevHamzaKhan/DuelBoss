/*
Name: GameStateManager.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Manages game state transitions and state-dependent behavior. Tracks current state (menu, playing, game over, how to play), shop visibility, and pause state. Extracted from GamePanel to centralize state logic.
*/

package manager;

public class GameStateManager {
    
    public enum GameState {
        MAIN_MENU, HOW_TO_PLAY, PLAYING, GAME_OVER
    }
    
    private GameState currentState;
    private boolean showingUpgradeShop;
    private boolean isGamePaused;
    
    public GameStateManager() {
        this.currentState = GameState.MAIN_MENU;
        this.showingUpgradeShop = false;
        this.isGamePaused = false;
    }
    
    public GameState getCurrentState() {
        return currentState;
    }
    
    public void setState(GameState newState) {
        this.currentState = newState;
    }
    
    public boolean isPlaying() {
        return currentState == GameState.PLAYING;
    }
    
    public boolean isMainMenu() {
        return currentState == GameState.MAIN_MENU;
    }
    
    public boolean isGameOver() {
        return currentState == GameState.GAME_OVER;
    }
    
    public boolean isHowToPlay() {
        return currentState == GameState.HOW_TO_PLAY;
    }
    
    public boolean isShowingUpgradeShop() {
        return showingUpgradeShop;
    }
    
    public void setShowingUpgradeShop(boolean showing) {
        this.showingUpgradeShop = showing;
    }
    
    public void toggleUpgradeShop() {
        this.showingUpgradeShop = !this.showingUpgradeShop;
    }
    
    public boolean isGamePaused() {
        return isGamePaused;
    }
    
    public void setGamePaused(boolean paused) {
        this.isGamePaused = paused;
    }
    
    public void startNewGame() {
        this.currentState = GameState.PLAYING;
        this.showingUpgradeShop = false;
        this.isGamePaused = false;
    }
    
    public void returnToMenu() {
        this.currentState = GameState.MAIN_MENU;
        this.showingUpgradeShop = false;
        this.isGamePaused = false;
    }
    
    public void endGame() {
        this.currentState = GameState.GAME_OVER;
        this.showingUpgradeShop = false;
        this.isGamePaused = false;
    }
}
