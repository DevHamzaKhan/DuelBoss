/*
Programmers: Hamza Khan & Alec Li
Program Name: LightningMap
Program Date: 2025-12-31
Program Description: Lightning map with space background
*/

public class LightningMap extends Map {
	private Platform[] platforms;
	private GameImage background;
	
	public LightningMap() {
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
		background = new GameImage("SpaceBackground.png", 0, 0, Main.WIDTH, Main.HEIGHT, false);
	}
	
	public Platform[] getPlatforms() {
		return platforms;
	}
	
	public GameImage getBackground() {
		return background;
	}
}

