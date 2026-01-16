/*
Name: ButtonManager.java
Authors: Hamza Khan & Alec Li
Date: January 16, 2026
Description: Manages button state and click detection for menus.
*/

package ui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

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
    
    // updates hover state for menu buttons
    public void updateMenuHover(int mouseX, int mouseY) {
        hoveredMenuButtonIndex = -1;
        for (int i = 0; i < menuButtonRects.size(); i++) {
            if (menuButtonRects.get(i).contains(mouseX, mouseY)) {
                hoveredMenuButtonIndex = i;
                break;
            }
        }
    }
    
    // updates hover state for shop buttons
    public void updateShopHover(int mouseX, int mouseY) {
        hoveredShopButtonIndex = -1;
        for (int i = 0; i < shopButtonRects.size(); i++) {
            if (shopButtonRects.get(i).contains(mouseX, mouseY)) {
                hoveredShopButtonIndex = i;
                break;
            }
        }
    }
    
    // gets the index of the clicked menu button, or -1 if none
    public int getClickedMenuButton(int x, int y) {
        for (int i = 0; i < menuButtonRects.size(); i++) {
            if (menuButtonRects.get(i).contains(x, y)) {
                return i;
            }
        }
        return -1;
    }
    
    // gets the index of the clicked shop button, or -1 if none
    public int getClickedShopButton(int x, int y) {
        for (int i = 0; i < shopButtonRects.size(); i++) {
            if (shopButtonRects.get(i).contains(x, y)) {
                return i;
            }
        }
        return -1;
    }
    
    // registers a menu button rectangle
    public void addMenuButton(Rectangle rect) {
        menuButtonRects.add(rect);
    }
    
    // registers a shop button rectangle
    public void addShopButton(Rectangle rect) {
        shopButtonRects.add(rect);
    }
    
    // clears all menu buttons (hover state managed by updatemenuhover)
    public void clearMenuButtons() {
        menuButtonRects.clear();
    }
    
    // clears all shop buttons (hover state managed by updateshophover)
    public void clearShopButtons() {
        shopButtonRects.clear();
    }
    
    // gets the index of the hovered menu button, or -1 if none
    public int getHoveredMenuButtonIndex() {
        return hoveredMenuButtonIndex;
    }
    
    // gets the index of the hovered shop button, or -1 if none
    public int getHoveredShopButtonIndex() {
        return hoveredShopButtonIndex;
    }
    
    // checks if a specific menu button is hovered
    public boolean isMenuButtonHovered(int index) {
        return hoveredMenuButtonIndex == index;
    }
    
    // checks if a specific shop button is hovered
    public boolean isShopButtonHovered(int index) {
        return hoveredShopButtonIndex == index;
    }
}
