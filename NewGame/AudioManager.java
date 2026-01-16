import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioManager {
    private static final String SHOT_FILE = "Audio/shot.mp3";
    private static final String EXPLOSION_FILE = "Audio/explosion.mp3";
    private final ExecutorService audioExecutor;
    private final String os;
    private final String shotPath;
    private final String explosionPath;
    
    public AudioManager() {
        // Use cached thread pool for instant audio playback
        audioExecutor = Executors.newCachedThreadPool();
        
        // Get absolute paths once for faster execution
        File shotFile = new File(SHOT_FILE);
        File explosionFile = new File(EXPLOSION_FILE);
        shotPath = shotFile.getAbsolutePath();
        explosionPath = explosionFile.getAbsolutePath();
        
        if (!shotFile.exists()) {
            System.err.println("Warning: " + SHOT_FILE + " not found");
        }
        if (!explosionFile.exists()) {
            System.err.println("Warning: " + EXPLOSION_FILE + " not found");
        }
        
        // Determine OS once
        os = System.getProperty("os.name").toLowerCase();
    }
    
    public void playShot() {
        playMP3(shotPath);
    }
    
    public void playExplosion() {
        playMP3(explosionPath);
    }
    
    private void playMP3(String filePath) {
        // Submit to thread pool for instant execution - no blocking
        audioExecutor.submit(() -> {
            try {
                if (os.contains("mac") || os.contains("darwin")) {
                    // macOS - use afplay with absolute path for instant playback
                    // Use Runtime.exec directly for minimal overhead
                    Runtime.getRuntime().exec(new String[]{"afplay", filePath});
                } else if (os.contains("win")) {
                    // Windows
                    Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "/min", filePath});
                } else {
                    // Linux - try common players
                    String[] players = {"mpg123", "mpg321", "play", "aplay"};
                    for (String player : players) {
                        try {
                            Process test = Runtime.getRuntime().exec(new String[]{"which", player});
                            if (test.waitFor() == 0) {
                                Runtime.getRuntime().exec(new String[]{player, filePath});
                                break;
                            }
                        } catch (Exception e) {
                            // Continue to next player
                        }
                    }
                }
            } catch (Exception e) {
                // Silently ignore audio errors - game should continue without sound
            }
        });
    }
}

