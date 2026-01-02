public class DefaultMap extends Map {
    private Platform[] platforms;
    private GameImage background;

    public DefaultMap() {
        platforms = new Platform[] {
            new Platform(0, 400, 960, 20, 1, "newplatform.png"),
            new Platform(380, 300, 200, 10, 1, "newplatform.png"),
            new Platform(670, 300, 200, 10, 1, "newplatform.png"),
            new Platform(186, 200, 200, 10, 1, "newplatform.png"),
            new Platform(572, 200, 200, 10, 1, "newplatform.png"),
            new Platform(380, 100, 200, 10, 1, "newplatform.png")
        };
        background = new GameImage("DefaultBackground.png", 0, 0, Main.WIDTH, Main.HEIGHT, false);
    }

    public Platform[] getPlatforms() { return platforms; }
    public GameImage getBackground() { return background; }
    public String getName() { return "Default"; }
}
