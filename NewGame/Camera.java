public class Camera {

    private double x;
    private double y;

    private final int screenWidth;
    private final int screenHeight;
    private final int mapWidth;
    private final int mapHeight;

    public Camera(double x, double y, int screenWidth, int screenHeight, int mapWidth, int mapHeight) {
        this.x = x;
        this.y = y;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        clamp();
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
        clamp();
    }

    public void move(double dx, double dy) {
        x += dx;
        y += dy;
        clamp();
    }

    private void clamp() {
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > mapWidth - screenWidth) x = mapWidth - screenWidth;
        if (y > mapHeight - screenHeight) y = mapHeight - screenHeight;
    }

    public int getX() {
        return (int) x;
    }

    public int getY() {
        return (int) y;
    }
}


