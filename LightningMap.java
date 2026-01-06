public class LightningMap extends Map {
    private Platform[] platforms;
    private GameImage background;

    public LightningMap() {
        stunInterval = 700;
        platforms = new Platform[] {
            new Platform(0, 400, 960, 20, 1, "newplatform.png"),
            new Platform(80, 300, 120, 10, 1, "newplatform.png"),
            new Platform(280, 300, 120, 10, 1, "newplatform.png"),
            new Platform(560, 300, 120, 10, 1, "newplatform.png"),
            new Platform(760, 300, 120, 10, 1, "newplatform.png"),
            new Platform(160, 200, 130, 10, 1, "newplatform.png"),
            new Platform(430, 200, 130, 10, 1, "newplatform.png"),
            new Platform(670, 200, 130, 10, 1, "newplatform.png"),
            new Platform(200, 100, 140, 10, 1, "newplatform.png"),
            new Platform(420, 100, 140, 10, 1, "newplatform.png"),
            new Platform(620, 100, 140, 10, 1, "newplatform.png")
        };
        background = new GameImage("LightningBackground.png", 0, 0, Main.WIDTH, Main.HEIGHT, false);
    }

    public Platform[] getPlatforms() { return platforms; }
    public GameImage getBackground() { return background; }
    public String getName() { return "Lightning"; }
}
