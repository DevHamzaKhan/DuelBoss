import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Professional animation system for loading and managing sprite animations
 * Supports multiple animation states with frame-by-frame rendering
 */
public class AnimationManager {
    private Map<String, Animation> animations;
    private String currentAnimation;
    private boolean facingRight;
    
    public AnimationManager() {
        this.animations = new HashMap<>();
        this.currentAnimation = "idle";
        this.facingRight = true;
    }
    
    /**
     * Load animation frames from a directory
     * @param name Animation name (e.g., "idle", "run", "attack")
     * @param basePath Base path to animation folder
     * @param frameCount Number of frames in animation
     * @param frameRate Frames to wait between sprite changes
     */
    public void loadAnimation(String name, String basePath, int frameCount, int frameRate) {
        Animation anim = new Animation(frameRate);
        for (int i = 1; i <= frameCount; i++) {
            String path;
            if (basePath.endsWith("_")) {
                path = basePath + i + ".png";
            } else if (basePath.endsWith("/")) {
                path = basePath + name + "_" + i + ".png";
            } else {
                path = basePath + "_" + i + ".png";
            }
            BufferedImage frame = loadImage(path);
            if (frame != null) {
                anim.addFrame(frame);
            }
        }
        animations.put(name, anim);
    }
    
    /**
     * Load animation with custom filename pattern
     */
    public void loadAnimationCustom(String name, String basePath, String prefix, int startIndex, int endIndex, int frameRate) {
        Animation anim = new Animation(frameRate);
        for (int i = startIndex; i <= endIndex; i++) {
            String path = basePath + prefix + i + ".png";
            BufferedImage frame = loadImage(path);
            if (frame != null) {
                anim.addFrame(frame);
            }
        }
        animations.put(name, anim);
    }
    
    /**
     * Load a single frame as an animation (for static poses)
     */
    public void loadSingleFrame(String name, String imagePath, int frameRate) {
        Animation anim = new Animation(frameRate);
        BufferedImage frame = loadImage(imagePath);
        if (frame != null) {
            anim.addFrame(frame);
        }
        animations.put(name, anim);
    }
    
    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(getClass().getResourceAsStream("Images/" + path));
        } catch (Exception e) {
            // Silently fail - some animations might not exist
            return null;
        }
    }
    
    /**
     * Set the current animation
     */
    public void setAnimation(String name) {
        if (animations.containsKey(name) && !name.equals(currentAnimation)) {
            currentAnimation = name;
            Animation anim = animations.get(name);
            if (anim != null) {
                anim.reset();
            }
        }
    }
    
    /**
     * Update animation (advance frame if needed)
     */
    public void update() {
        Animation anim = animations.get(currentAnimation);
        if (anim != null) {
            anim.update();
        }
    }
    
    /**
     * Draw current frame
     * Accounts for sprite sheets where the actual sprite is smaller than the frame
     * Player sprites: 288x128 frames, actual sprite ~32x40 pixels, centered-bottom positioned
     */
    public void draw(Graphics2D g, int x, int y, int width, int height, boolean facingRight) {
        // Default player sprite ratios
        drawWithRatio(g, x, y, width, height, facingRight, 4.0/36.0, 5.0/16.0);
    }
    
    /**
     * Draw current frame with custom sprite ratios
     * @param spriteWidthRatio The ratio of actual sprite width to frame width
     * @param spriteHeightRatio The ratio of actual sprite height to frame height
     */
    public void drawWithRatio(Graphics2D g, int x, int y, int width, int height, boolean facingRight,
                              double spriteWidthRatio, double spriteHeightRatio) {
        drawWithRatio(g, x, y, width, height, facingRight, spriteWidthRatio, spriteHeightRatio, 1.0);
    }

    /**
     * Draw current frame with custom sprite ratios and vertical anchor
     * @param spriteWidthRatio The ratio of actual sprite width to frame width
     * @param spriteHeightRatio The ratio of actual sprite height to frame height
     * @param verticalAnchor Vertical position of sprite in frame (0.0=top, 0.5=center, 1.0=bottom)
     */
    public void drawWithRatio(Graphics2D g, int x, int y, int width, int height, boolean facingRight,
                              double spriteWidthRatio, double spriteHeightRatio, double verticalAnchor) {
        this.facingRight = facingRight;
        Animation anim = animations.get(currentAnimation);
        if (anim != null) {
            BufferedImage frame = anim.getCurrentFrame();
            if (frame != null) {
                int frameWidth = frame.getWidth();
                int frameHeight = frame.getHeight();

                // Calculate how much to scale the frame to make sprite match hitbox
                double scaleX = width / (frameWidth * spriteWidthRatio);
                double scaleY = height / (frameHeight * spriteHeightRatio);

                int scaledFrameWidth = (int)(frameWidth * scaleX);
                int scaledFrameHeight = (int)(frameHeight * scaleY);

                // Sprite is centered horizontally in frame
                // Vertical position depends on verticalAnchor (1.0=bottom, 0.5=center, 0.0=top)
                int offsetX = (scaledFrameWidth - width) / 2;
                int offsetY = (int)(scaledFrameHeight - height - (scaledFrameHeight - height) * (1.0 - verticalAnchor));

                if (facingRight) {
                    g.drawImage(frame, x - offsetX, y - offsetY, scaledFrameWidth, scaledFrameHeight, null);
                } else {
                    // Flip horizontally for left-facing
                    g.drawImage(frame, x + width + offsetX, y - offsetY, -scaledFrameWidth, scaledFrameHeight, null);
                }
            }
        }
    }
    
    /**
     * Get current animation name
     */
    public String getCurrentAnimation() {
        return currentAnimation;
    }
    
    /**
     * Check if animation exists and has frames loaded
     */
    public boolean hasAnimation(String name) {
        Animation anim = animations.get(name);
        return anim != null && anim.hasFrames();
    }
    
    /**
     * Inner class representing a single animation sequence
     */
    private class Animation {
        private ArrayList<BufferedImage> frames;
        private int currentFrame;
        private int frameDelay;
        private int frameCounter;
        
        public Animation(int frameDelay) {
            this.frames = new ArrayList<>();
            this.currentFrame = 0;
            this.frameDelay = frameDelay;
            this.frameCounter = 0;
        }
        
        public void addFrame(BufferedImage frame) {
            frames.add(frame);
        }
        
        public void update() {
            if (frames.isEmpty()) return;
            
            frameCounter++;
            if (frameCounter >= frameDelay) {
                frameCounter = 0;
                currentFrame = (currentFrame + 1) % frames.size();
            }
        }
        
        public BufferedImage getCurrentFrame() {
            if (frames.isEmpty()) return null;
            return frames.get(currentFrame);
        }
        
        public void reset() {
            currentFrame = 0;
            frameCounter = 0;
        }

        public boolean hasFrames() {
            return !frames.isEmpty();
        }
    }
}
