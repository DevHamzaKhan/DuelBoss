package com.polygonwars.ui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages button state and click detection for menus.
 * Extracts button management from MenuRenderer.
 */
public class ButtonManager {
    
    private final List<Rectangle> menuButtonRects;
    private final List<Rectangle> shopButtonRects;
    private int hoveredMenuButtonIndex;
    private int hoveredShopButtonIndex;
    
    public ButtonManager() {
        this.menuButtonRects = new ArrayList<>();
        this.shopButtonRects = new ArrayList<>();
        this.hoveredMenuButtonIndex = -1;
        this.hoveredShopButtonIndex = -1;
    }
    
    /**
     * Updates hover state for menu buttons.
     */
    public void updateMenuHover(int mouseX, int mouseY) {
        hoveredMenuButtonIndex = -1;
        for (int i = 0; i < menuButtonRects.size(); i++) {
            if (menuButtonRects.get(i).contains(mouseX, mouseY)) {
                hoveredMenuButtonIndex = i;
                break;
            }
        }
    }
    
    /**
     * Updates hover state for shop buttons.
     */
    public void updateShopHover(int mouseX, int mouseY) {
        hoveredShopButtonIndex = -1;
        for (int i = 0; i < shopButtonRects.size(); i++) {
            if (shopButtonRects.get(i).contains(mouseX, mouseY)) {
                hoveredShopButtonIndex = i;
                break;
            }
        }
    }
    
    /**
     * Gets the index of the clicked menu button, or -1 if none.
     */
    public int getClickedMenuButton(int x, int y) {
        for (int i = 0; i < menuButtonRects.size(); i++) {
            if (menuButtonRects.get(i).contains(x, y)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Gets the index of the clicked shop button, or -1 if none.
     */
    public int getClickedShopButton(int x, int y) {
        for (int i = 0; i < shopButtonRects.size(); i++) {
            if (shopButtonRects.get(i).contains(x, y)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Registers a menu button rectangle.
     */
    public void addMenuButton(Rectangle rect) {
        menuButtonRects.add(rect);
    }
    
    /**
     * Registers a shop button rectangle.
     */
    public void addShopButton(Rectangle rect) {
        shopButtonRects.add(rect);
    }
    
    /**
     * Clears all menu buttons.
     * Note: Does not reset hover state - that's managed by updateMenuHover().
     */
    public void clearMenuButtons() {
        menuButtonRects.clear();
    }
    
    /**
     * Clears all shop buttons.
     * Note: Does not reset hover state - that's managed by updateShopHover().
     */
    public void clearShopButtons() {
        shopButtonRects.clear();
    }
    
    /**
     * Gets the index of the hovered menu button, or -1 if none.
     */
    public int getHoveredMenuButtonIndex() {
        return hoveredMenuButtonIndex;
    }
    
    /**
     * Gets the index of the hovered shop button, or -1 if none.
     */
    public int getHoveredShopButtonIndex() {
        return hoveredShopButtonIndex;
    }
    
    /**
     * Checks if a specific menu button is hovered.
     */
    public boolean isMenuButtonHovered(int index) {
        return hoveredMenuButtonIndex == index;
    }
    
    /**
     * Checks if a specific shop button is hovered.
     */
    public boolean isShopButtonHovered(int index) {
        return hoveredShopButtonIndex == index;
    }
}
