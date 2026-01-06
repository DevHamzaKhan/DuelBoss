public class WaterMap extends Map {
    private Platform[] platforms;
    private GameImage background;

    public WaterMap() {
        gravityMod = 0.6;
        platforms = new Platform[] {
            new Platform(0, 400, 960, 20, 1, "newplatform.png"),
            new Platform(150, 300, 300, 10, 1, "newplatform.png"),
            new Platform(610, 300, 250, 10, 1, "newplatform.png"),
            new Platform(320, 200, 220, 10, 1, "newplatform.png"),
            new Platform(680, 200, 180, 10, 1, "newplatform.png"),
            new Platform(380, 100, 350, 10, 1, "newplatform.png")
        };
        background = new GameImage("WaterBackground.png", 0, 0, Main.WIDTH, Main.HEIGHT, false);
    }

    public Platform[] getPlatforms() { return platforms; }
    public GameImage getBackground() { return background; }
    public String getName() { return "Water"; }
}
