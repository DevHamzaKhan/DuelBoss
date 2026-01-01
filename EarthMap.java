/*
Programmers: Hamza Khan & Alec Li
Program Name: EarthMap
Program Date: 2025-12-31
Program Description: Earth map with default background
*/

public class EarthMap extends Map {
	private Platform[] platforms;
	private GameImage background;
	
	public EarthMap() {
		platforms = new Platform[] {
			new Platform(0, 400, 960, 20, 1, "newplatform.png"),
			new Platform(50, 300, 860, 10, 1, "newplatform.png"),
			new Platform(100, 200, 760, 10, 1, "newplatform.png"),
			new Platform(230, 100, 500, 10, 1, "newplatform.png")
		};
		background = new GameImage("DefaultBackground.png", 0, 0, Main.WIDTH, Main.HEIGHT, false);
	}
	
	public Platform[] getPlatforms() {
		return platforms;
	}
	
	public GameImage getBackground() {
		return background;
	}
}

