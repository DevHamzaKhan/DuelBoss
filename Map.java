public abstract class Map {
    protected double gravityMod = 1.0;
    protected double speedMod = 1.0;
    protected int burnDamage = 0;
    protected int stunInterval = 0;

    public abstract Platform[] getPlatforms();
    public abstract GameImage getBackground();
    public abstract String getName();

    public double getGravityMod() { return gravityMod; }
    public double getSpeedMod() { return speedMod; }
    public int getBurnDamage() { return burnDamage; }
    public int getStunInterval() { return stunInterval; }
}
