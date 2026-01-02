import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;

public class Game extends JPanel implements KeyListener {
    private static final int MENU = 0;
    private static final int SELECT_P1 = 1;
    private static final int SELECT_P2 = 2;
    private static final int SELECT_MAP = 3;
    private static final int SELECT_BOSS = 4;
    private static final int PLAYING_PVP = 5;
    private static final int PLAYING_BOSS = 6;
    private static final int GAME_OVER = 7;
    private static final int TUTORIAL = 8;

    private int state = MENU;
    private int menuSelection = 0;
    private int gameMode = 0;
    private String[] menuOptions = {"Player vs Player", "Player vs Boss", "Tutorial", "Exit"};
    private String[] characterOptions = {"Swordsman", "Brawler", "Archer"};
    private String[] mapOptions = {"Default", "Ice", "Fire", "Water", "Lightning", "Earth"};
    private String[] bossOptions = {"Ice Boss", "Fire Boss", "Water Boss", "Lightning Boss", "Earth Boss"};

    private int p1CharSelect = 0;
    private int p2CharSelect = 0;
    private int mapSelect = 0;
    private int bossSelect = 0;

    private Player player1;
    private Player player2;
    private Boss boss;
    private Map currentMap;
    private Platform[] platforms;
    private GameImage background;

    private ArrayList<Characters> pvpTargets;
    private ArrayList<Characters> bossTargets;

    private Timer timer;
    private int burnTimer;
    private int stunTimer;
    private String winner = "";

    public Game() {
        setPreferredSize(new Dimension(Main.WIDTH, Main.HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        pvpTargets = new ArrayList<>();
        bossTargets = new ArrayList<>();

        timer = new Timer(16, e -> {
            update();
            repaint();
        });
        timer.start();
    }

    private void update() {
        if (state == PLAYING_PVP) {
            updatePVP();
        } else if (state == PLAYING_BOSS) {
            updateBoss();
        }
    }

    private void updatePVP() {
        player1.update(platforms, currentMap.getGravityMod(), currentMap.getSpeedMod());
        player2.update(platforms, currentMap.getGravityMod(), currentMap.getSpeedMod());

        applyMapEffects(player1);
        applyMapEffects(player2);

        player1.checkAttackCollisions(pvpTargets);
        player2.checkAttackCollisions(pvpTargets);

        if (player1.isDead()) {
            winner = "Player 2 Wins!";
            state = GAME_OVER;
        } else if (player2.isDead()) {
            winner = "Player 1 Wins!";
            state = GAME_OVER;
        }
    }

    private void updateBoss() {
        player1.update(platforms, currentMap.getGravityMod(), currentMap.getSpeedMod());
        boss.setTarget(player1);
        boss.updateAI(platforms, currentMap.getGravityMod(), currentMap.getSpeedMod());

        applyMapEffects(player1);

        player1.checkAttackCollisions(bossTargets);
        boss.checkAttackCollisions(bossTargets);

        if (player1.isDead()) {
            winner = "Boss Wins!";
            state = GAME_OVER;
        } else if (boss.isDead()) {
            winner = "You Win!";
            state = GAME_OVER;
        }
    }

    private void applyMapEffects(Characters c) {
        if (currentMap.getBurnDamage() > 0) {
            burnTimer++;
            if (burnTimer >= 60) {
                if (!c.isStunned() && c.isOnGround()) {
                    c.takeDamage(currentMap.getBurnDamage());
                }
                burnTimer = 0;
            }
        }

        if (currentMap.getStunInterval() > 0) {
            stunTimer++;
            if (stunTimer >= currentMap.getStunInterval()) {
                c.stun(60);
                stunTimer = 0;
            }
        }
    }

    private void startPVP() {
        currentMap = getMap(mapSelect);
        platforms = currentMap.getPlatforms();
        background = currentMap.getBackground();
        player1 = createPlayer(p1CharSelect, 100, 340, 1);
        player2 = createPlayer(p2CharSelect, 800, 340, 2);

        pvpTargets.clear();
        pvpTargets.add(player1);
        pvpTargets.add(player2);

        player1.setTargets(pvpTargets);
        player2.setTargets(pvpTargets);

        burnTimer = 0;
        stunTimer = 0;
        state = PLAYING_PVP;
    }

    private void startBoss() {
        currentMap = getMap(mapSelect);
        platforms = currentMap.getPlatforms();
        background = currentMap.getBackground();
        player1 = createPlayer(p1CharSelect, 100, 340, 1);

        int bossType = mapSelect == 0 ? 0 : mapSelect - 1;
        boss = createBoss(bossType);

        bossTargets.clear();
        bossTargets.add(player1);
        bossTargets.add(boss);

        player1.setTargets(bossTargets);

        burnTimer = 0;
        stunTimer = 0;
        state = PLAYING_BOSS;
    }

    private Player createPlayer(int type, int x, int y, int num) {
        switch (type) {
            case 0: return new Swordsman(x, y, num);
            case 1: return new Brawler(x, y, num);
            case 2: return new Archer(x, y, num);
            default: return new Swordsman(x, y, num);
        }
    }

    private Boss createBoss(int type) {
        switch (type) {
            case 0: return new IceBoss(800, 250);
            case 1: return new FireBoss(800, 250);
            case 2: return new WaterBoss(800, 250);
            case 3: return new LightningBoss(800, 250);
            case 4: return new EarthBoss(800, 200);
            default: return new IceBoss(800, 250);
        }
    }

    private Map getMap(int type) {
        switch (type) {
            case 0: return new DefaultMap();
            case 1: return new IceMap();
            case 2: return new FireMap();
            case 3: return new WaterMap();
            case 4: return new LightningMap();
            case 5: return new EarthMap();
            default: return new DefaultMap();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (state) {
            case MENU: drawMenu(g2d); break;
            case SELECT_P1: drawCharSelect(g2d, 1); break;
            case SELECT_P2: drawCharSelect(g2d, 2); break;
            case SELECT_MAP: drawMapSelect(g2d); break;
            case SELECT_BOSS: drawBossSelect(g2d); break;
            case PLAYING_PVP: drawGame(g2d); break;
            case PLAYING_BOSS: drawBossGame(g2d); break;
            case GAME_OVER: drawGameOver(g2d); break;
            case TUTORIAL: drawTutorial(g2d); break;
        }
    }

    private void drawMenu(Graphics2D g) {
        g.setColor(new Color(30, 30, 50));
        g.fillRect(0, 0, Main.WIDTH, Main.HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        g.drawString("DUEL BOSS", Main.WIDTH / 2 - 130, 100);

        g.setFont(new Font("Arial", Font.PLAIN, 24));
        for (int i = 0; i < menuOptions.length; i++) {
            if (i == menuSelection) {
                g.setColor(Color.YELLOW);
                g.drawString("> " + menuOptions[i], Main.WIDTH / 2 - 80, 200 + i * 50);
            } else {
                g.setColor(Color.WHITE);
                g.drawString("  " + menuOptions[i], Main.WIDTH / 2 - 80, 200 + i * 50);
            }
        }
    }

    private void drawCharSelect(Graphics2D g, int playerNum) {
        g.setColor(new Color(30, 30, 50));
        g.fillRect(0, 0, Main.WIDTH, Main.HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Player " + playerNum + " - Select Character", Main.WIDTH / 2 - 200, 80);

        int sel = playerNum == 1 ? p1CharSelect : p2CharSelect;
        g.setFont(new Font("Arial", Font.PLAIN, 20));

        String[] stats = {
            "Slow shot, Medium damage, Strong melee, Slow move",
            "Medium shot, Weak damage, Fast attack, Very fast move",
            "Fast shot, Strong damage, Weak melee, Medium move"
        };

        for (int i = 0; i < characterOptions.length; i++) {
            if (i == sel) {
                g.setColor(Color.YELLOW);
                g.drawString("> " + characterOptions[i], Main.WIDTH / 2 - 100, 180 + i * 80);
                g.setColor(Color.GRAY);
                g.setFont(new Font("Arial", Font.PLAIN, 14));
                g.drawString(stats[i], Main.WIDTH / 2 - 180, 205 + i * 80);
                g.setFont(new Font("Arial", Font.PLAIN, 20));
            } else {
                g.setColor(Color.WHITE);
                g.drawString("  " + characterOptions[i], Main.WIDTH / 2 - 100, 180 + i * 80);
            }
        }

        g.setColor(Color.GRAY);
        g.drawString("Press ENTER to confirm", Main.WIDTH / 2 - 100, 450);
    }

    private void drawMapSelect(Graphics2D g) {
        g.setColor(new Color(30, 30, 50));
        g.fillRect(0, 0, Main.WIDTH, Main.HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Select Map", Main.WIDTH / 2 - 80, 60);

        String[] effects = {
            "No special effects",
            "Faster movement speed",
            "Standing still causes burn damage",
            "Lower gravity (higher jumps)",
            "Everyone stunned every 7 seconds",
            "Overall movement speed reduced"
        };

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        for (int i = 0; i < mapOptions.length; i++) {
            if (i == mapSelect) {
                g.setColor(Color.YELLOW);
                g.drawString("> " + mapOptions[i], 350, 120 + i * 55);
                g.setColor(Color.GRAY);
                g.setFont(new Font("Arial", Font.PLAIN, 12));
                g.drawString(effects[i], 370, 138 + i * 55);
                g.setFont(new Font("Arial", Font.PLAIN, 18));
            } else {
                g.setColor(Color.WHITE);
                g.drawString("  " + mapOptions[i], 350, 120 + i * 55);
            }
        }
    }

    private void drawBossSelect(Graphics2D g) {
        g.setColor(new Color(30, 30, 50));
        g.fillRect(0, 0, Main.WIDTH, Main.HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Select Boss", Main.WIDTH / 2 - 80, 60);

        String[] desc = {
            "Medium HP, freezes player",
            "Medium HP, homing projectiles",
            "Medium HP, whirlpool attack",
            "Low HP, very fast, lightning strikes",
            "Very high HP, earthquake attacks"
        };

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        for (int i = 0; i < bossOptions.length; i++) {
            if (i == bossSelect) {
                g.setColor(Color.YELLOW);
                g.drawString("> " + bossOptions[i], 350, 130 + i * 60);
                g.setColor(Color.GRAY);
                g.setFont(new Font("Arial", Font.PLAIN, 12));
                g.drawString(desc[i], 370, 150 + i * 60);
                g.setFont(new Font("Arial", Font.PLAIN, 18));
            } else {
                g.setColor(Color.WHITE);
                g.drawString("  " + bossOptions[i], 350, 130 + i * 60);
            }
        }
    }

    private void drawGame(Graphics2D g) {
        if (background != null) background.draw(g);

        for (Platform p : platforms) {
            if (p.isActive()) p.draw(g);
        }

        player1.draw(g);
        player2.draw(g);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Map: " + currentMap.getName(), 10, 20);
        g.drawString("ESC to quit", Main.WIDTH - 100, 20);
    }

    private void drawBossGame(Graphics2D g) {
        if (background != null) background.draw(g);

        for (Platform p : platforms) {
            if (p.isActive()) p.draw(g);
        }

        player1.draw(g);
        boss.draw(g);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Map: " + currentMap.getName(), 10, 20);
        g.drawString("ESC to quit", Main.WIDTH - 100, 20);
    }

    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, Main.WIDTH, Main.HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(winner);
        g.drawString(winner, (Main.WIDTH - textWidth) / 2, Main.HEIGHT / 2);

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Press ENTER to return to menu", Main.WIDTH / 2 - 130, Main.HEIGHT / 2 + 60);
    }

    private void drawTutorial(Graphics2D g) {
        g.setColor(new Color(30, 30, 50));
        g.fillRect(0, 0, Main.WIDTH, Main.HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Tutorial", Main.WIDTH / 2 - 60, 60);

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Player 1 Controls:", 100, 130);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("W/A/D - Jump / Move Left / Move Right", 120, 160);
        g.drawString("S - Drop through platform", 120, 185);
        g.drawString("Q - Ranged Attack", 120, 210);
        g.drawString("E - Melee Attack", 120, 235);

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Player 2 Controls:", 500, 130);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("I/J/L - Jump / Move Left / Move Right", 520, 160);
        g.drawString("K - Drop through platform", 520, 185);
        g.drawString("U - Ranged Attack", 520, 210);
        g.drawString("O - Melee Attack", 520, 235);

        g.setColor(Color.CYAN);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("TIP: Press S/K while on a platform to drop down!", Main.WIDTH / 2 - 180, 320);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(Color.GRAY);
        g.drawString("Press ESC to return to menu", Main.WIDTH / 2 - 110, 420);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (state == MENU) {
            if (key == KeyEvent.VK_UP) menuSelection = (menuSelection - 1 + menuOptions.length) % menuOptions.length;
            if (key == KeyEvent.VK_DOWN) menuSelection = (menuSelection + 1) % menuOptions.length;
            if (key == KeyEvent.VK_ENTER) {
                switch (menuSelection) {
                    case 0: gameMode = 0; state = SELECT_P1; break;
                    case 1: gameMode = 1; state = SELECT_P1; break;
                    case 2: state = TUTORIAL; break;
                    case 3: System.exit(0); break;
                }
            }
        } else if (state == SELECT_P1) {
            if (key == KeyEvent.VK_UP) p1CharSelect = (p1CharSelect - 1 + 3) % 3;
            if (key == KeyEvent.VK_DOWN) p1CharSelect = (p1CharSelect + 1) % 3;
            if (key == KeyEvent.VK_ENTER) {
                if (gameMode == 0) state = SELECT_P2;
                else state = SELECT_MAP;
            }
            if (key == KeyEvent.VK_ESCAPE) state = MENU;
        } else if (state == SELECT_P2) {
            if (key == KeyEvent.VK_UP) p2CharSelect = (p2CharSelect - 1 + 3) % 3;
            if (key == KeyEvent.VK_DOWN) p2CharSelect = (p2CharSelect + 1) % 3;
            if (key == KeyEvent.VK_ENTER) state = SELECT_MAP;
            if (key == KeyEvent.VK_ESCAPE) state = SELECT_P1;
        } else if (state == SELECT_MAP) {
            if (key == KeyEvent.VK_UP) mapSelect = (mapSelect - 1 + 6) % 6;
            if (key == KeyEvent.VK_DOWN) mapSelect = (mapSelect + 1) % 6;
            if (key == KeyEvent.VK_ENTER) {
                if (gameMode == 0) startPVP();
                else startBoss();
            }
            if (key == KeyEvent.VK_ESCAPE) {
                if (gameMode == 0) state = SELECT_P2;
                else state = SELECT_P1;
            }
        } else if (state == SELECT_BOSS) {
            if (key == KeyEvent.VK_UP) bossSelect = (bossSelect - 1 + 5) % 5;
            if (key == KeyEvent.VK_DOWN) bossSelect = (bossSelect + 1) % 5;
            if (key == KeyEvent.VK_ENTER) startBoss();
            if (key == KeyEvent.VK_ESCAPE) state = SELECT_MAP;
        } else if (state == PLAYING_PVP) {
            player1.handleKeyPress(key);
            player2.handleKeyPress(key);
            if (key == KeyEvent.VK_ESCAPE) state = MENU;
        } else if (state == PLAYING_BOSS) {
            player1.handleKeyPress(key);
            if (key == KeyEvent.VK_ESCAPE) state = MENU;
        } else if (state == GAME_OVER) {
            if (key == KeyEvent.VK_ENTER) state = MENU;
        } else if (state == TUTORIAL) {
            if (key == KeyEvent.VK_ESCAPE) state = MENU;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (state == PLAYING_PVP) {
            player1.handleKeyRelease(e.getKeyCode());
            player2.handleKeyRelease(e.getKeyCode());
        } else if (state == PLAYING_BOSS) {
            player1.handleKeyRelease(e.getKeyCode());
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
