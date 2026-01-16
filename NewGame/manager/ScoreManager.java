/*
Name: ScoreManager.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Manages game scoring, currency, and high score tracking.
*/

package manager;

import enemy.Enemy;

public class ScoreManager {

    private int score;
    private int currency;
    private int highScore;

    public ScoreManager() {
        reset();
    }

    public void reset() {
        score = 0;
        currency = 0;
    }

    public void addScore(int points) {
        score += points;
    }

    public void addCurrency(int amount) {
        currency += amount;
    }

    public boolean spendCurrency(int amount) {
        if (currency >= amount) {
            currency -= amount;
            return true;
        }
        return false;
    }

    public boolean canAfford(int amount) {
        return currency >= amount;
    }

    public void updateHighScore() {
        if (score > highScore) {
            highScore = score;
        }
    }

    // awards score based on enemy type using polymorphism
    public void awardScoreForEnemy(Enemy enemy) {
        addScore(enemy.getScoreValue());
    }

    // awards currency for completing a wave
    public void awardWaveCurrency(int waveNumber) {
        if (waveNumber == 0) {
            addCurrency(10);
        } else {
            addCurrency(waveNumber * 2);
        }
    }

    public int getScore() {
        return score;
    }

    public int getCurrency() {
        return currency;
    }

    public int getHighScore() {
        return highScore;
    }
}
