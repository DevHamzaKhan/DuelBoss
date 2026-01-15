import javax.swing.JFrame;

public class GameFrame extends JFrame {

    public static final int SCREEN_WIDTH = 1440;
    public static final int SCREEN_HEIGHT = 810;

    public GameFrame() {
        super("DuelBoss - New Game");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        setLocationRelativeTo(null);

        GamePanel gamePanel = new GamePanel(SCREEN_WIDTH, SCREEN_HEIGHT);
        setContentPane(gamePanel);
    }
}


