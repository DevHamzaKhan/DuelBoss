import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;

public class SoundManager {
    private static Clip musicClip;
    private static ArrayList<Clip> attackClips = new ArrayList<>();
    private static ArrayList<Clip> shotClips = new ArrayList<>();
    private static ArrayList<Clip> damageClips = new ArrayList<>();
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;

        loadMusic("Music&SFX/backgroundmusic.wav");

        // Pre-load 5 instances of each sound effect for rapid fire
        for (int i = 0; i < 5; i++) {
            attackClips.add(loadClip("Music&SFX/attack.wav"));
            shotClips.add(loadClip("Music&SFX/shot.wav"));
            damageClips.add(loadClip("Music&SFX/dmg.wav"));
        }

        initialized = true;
    }

    private static Clip loadClip(String path) {
        try {
            File soundFile = new File(path);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            return clip;
        } catch (Exception e) {
            System.err.println("Failed to load sound: " + path);
            return null;
        }
    }

    private static void loadMusic(String path) {
        try {
            File soundFile = new File(path);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            musicClip = AudioSystem.getClip();
            musicClip.open(audioStream);
        } catch (Exception e) {
            System.err.println("Failed to load music: " + path);
        }
    }

    public static void playMusic() {
        if (musicClip != null && !musicClip.isRunning()) {
            musicClip.setFramePosition(0);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public static void stopMusic() {
        if (musicClip != null && musicClip.isRunning()) {
            musicClip.stop();
        }
    }

    public static void playAttack() {
        playFromPool(attackClips);
    }

    public static void playShot() {
        playFromPool(shotClips);
    }

    public static void playDamage() {
        playFromPool(damageClips);
    }

    private static void playFromPool(ArrayList<Clip> pool) {
        for (Clip clip : pool) {
            if (clip != null && !clip.isRunning()) {
                clip.setFramePosition(0);
                clip.start();
                return;
            }
        }
        // If all clips are busy, just restart the first one
        if (!pool.isEmpty() && pool.get(0) != null) {
            pool.get(0).setFramePosition(0);
            pool.get(0).start();
        }
    }
}
