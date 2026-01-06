public class EarthMap extends Map {
    private Platform[] platforms;
    private GameImage background;

    public EarthMap() {
        speedMod = 0.7;
        platforms = new Platform[] {
            new Platform(0, 400, 960, 20, 1, "newplatform.png"),
            new Platform(50, 300, 860, 10, 1, "newplatform.png"),
            new Platform(100, 200, 760, 10, 1, "newplatform.png"),
            new Platform(230, 100, 500, 10, 1, "newplatform.png")
        };
        background = new GameImage("EarthBackground.png", 0, 0, Main.WIDTH, Main.HEIGHT, false);
    }

    public Platform[] getPlatforms() { return platforms; }
    public GameImage getBackground() { return background; }
    public String getName() { return "Earth"; }
}
