import java.util.ArrayList;

/**
 * Strategy pattern for AI behavior.
 * Allows different boss types to have different AI strategies.
 */
public interface AIBehavior {
    /**
     * Update the AI logic for movement and combat decisions
     * @param boss The boss character using this behavior
     * @param target The target character
     * @param targets List of all target characters
     * @param platforms Available platforms
     * @param speedMod Speed modifier from game state
     */
    void update(Boss boss, Characters target, ArrayList<Characters> targets, 
                Platform[] platforms, double speedMod);
    
    /**
     * Get the name/type of this behavior
     */
    String getName();
}
