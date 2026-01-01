/*
Programmers: Hamza Khan & Alec Li
Program Name: IceMap
Program Date: 2025-12-31
Program Description: Ice map with ice background
*/

public class IceMap extends Map {
	private Platform[] platforms;
	private GameImage background;
	
	public IceMap() {
		platforms = new Platform[] {
			new Platform(0, 400, 960, 20, 1, "newplatform.png"),
			new Platform(180, 300, 600, 10, 1, "newplatform.png"),
			new Platform(100, 200, 760, 10, 1, "newplatform.png"),
			new Platform(280, 100, 400, 10, 1, "newplatform.png")
		};
		background = new GameImage("IceBackground.png", 0, 0, Main.WIDTH, Main.HEIGHT, false);
	}
	
	public Platform[] getPlatforms() {
		return platforms;
	}
	
	public GameImage getBackground() {
		return background;
	}
}

