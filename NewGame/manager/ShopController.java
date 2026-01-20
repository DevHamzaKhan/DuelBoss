/*
Name: ShopController.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Manages upgrade shop logic and purchases.
*/

package manager;

import entity.Character;

public class ShopController {

    private static final int MAX_UPGRADE_LEVEL = 10;
    private static final int CONTINUE_BUTTON_ID = 7;
    private static final int BUY_HEALTH_BUTTON_ID = 5;
    private static final int BUY_SCORE_BUTTON_ID = 6;
    private static final int SCORE_POINTS_PER_CURRENCY = 10;

    // attempts to purchase an upgrade or special item from the shop
    public ShopPurchaseResult handlePurchase(int buttonId, Character player, ScoreManager scoreManager) {
        // Continue button
        if (buttonId == CONTINUE_BUTTON_ID) {
            return new ShopPurchaseResult(ShopPurchaseType.CONTINUE, 0);
        }

        // Check if player has currency
        if (!scoreManager.canAfford(1)) {
            return new ShopPurchaseResult(ShopPurchaseType.INSUFFICIENT_CURRENCY, 0);
        }

        boolean purchaseSuccessful = false;
        int scoreAwarded = 0;

        switch (buttonId) {
            case 0: // Max Health
                if (player.getMaxHealthLevel() < MAX_UPGRADE_LEVEL) {
                    player.upgradeMaxHealth();
                    purchaseSuccessful = true;
                }
                break;

            case 1: // Bullet Speed
                if (player.getBulletSpeedLevel() < MAX_UPGRADE_LEVEL) {
                    player.upgradeBulletSpeed();
                    purchaseSuccessful = true;
                }
                break;

            case 2: // Fire Rate
                if (player.getFireRateLevel() < MAX_UPGRADE_LEVEL) {
                    player.upgradeFireRate();
                    purchaseSuccessful = true;
                }
                break;

            case 3: // Movement Speed
                if (player.getMovementSpeedLevel() < MAX_UPGRADE_LEVEL) {
                    player.upgradeMovementSpeed();
                    purchaseSuccessful = true;
                }
                break;

            case 4: // Bullet Damage
                if (player.getBulletDamageLevel() < MAX_UPGRADE_LEVEL) {
                    player.upgradeBulletDamage();
                    purchaseSuccessful = true;
                }
                break;

            case BUY_HEALTH_BUTTON_ID: // Buy health
                player.buyHealth();
                purchaseSuccessful = true;
                break;

            case BUY_SCORE_BUTTON_ID: // Buy score
                scoreAwarded = SCORE_POINTS_PER_CURRENCY;
                purchaseSuccessful = true;
                break;

            default:
                return new ShopPurchaseResult(ShopPurchaseType.INVALID_BUTTON, 0);
        }

        if (purchaseSuccessful) {
            scoreManager.spendCurrency(1);
            if (buttonId == BUY_SCORE_BUTTON_ID) {
                return new ShopPurchaseResult(ShopPurchaseType.SCORE_PURCHASED, scoreAwarded);
            }
            return new ShopPurchaseResult(ShopPurchaseType.SUCCESS, 0);
        }

        return new ShopPurchaseResult(ShopPurchaseType.MAX_LEVEL_REACHED, 0);
    }

    // result of a shop purchase attempt
    public static class ShopPurchaseResult {
        private final ShopPurchaseType type;
        private final int scoreAwarded;

        public ShopPurchaseResult(ShopPurchaseType type, int scoreAwarded) {
            this.type = type;
            this.scoreAwarded = scoreAwarded;
        }

        public ShopPurchaseType getType() {
            return type;
        }

        public int getScoreAwarded() {
            return scoreAwarded;
        }
    }

    // type of shop purchase result
    public enum ShopPurchaseType {
        SUCCESS, // purchase successful
        CONTINUE, // continue button pressed
        SCORE_PURCHASED, // score purchase (needs special handling)
        INSUFFICIENT_CURRENCY, // not enough currency
        MAX_LEVEL_REACHED, // upgrade at max level
        INVALID_BUTTON // invalid button id
    }
}
