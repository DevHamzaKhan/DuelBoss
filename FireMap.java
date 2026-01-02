public class FireMap extends Map {
    private Platform[] platforms;
    private GameImage background;

    public FireMap() {
        burnDamage = 1;
        platforms = new Platform[] {
            new Platform(0, 400, 960, 20, 1, "newplatform.png"),
            new Platform(100, 300, 150, 10, 1, "newplatform.png"),
            new Platform(350, 300, 150, 10, 1, "newplatform.png"),
            new Platform(710, 300, 150, 10, 1, "newplatform.png"),
            new Platform(250, 200, 200, 10, 1, "newplatform.png"),
            new Platform(600, 200, 200, 10, 1, "newplatform.png"),
            new Platform(150, 100, 180, 10, 1, "newplatform.png"),
            new Platform(630, 100, 180, 10, 1, "newplatform.png")
        };
        background = new GameImage("FireBackground.png", 0, 0, Main.WIDTH, Main.HEIGHT, false);
    }

    public Platform[] getPlatforms() { return platforms; }
    public GameImage getBackground() { return background; }
    public String getName() { return "Fire"; }
}
