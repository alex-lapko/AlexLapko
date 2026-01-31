import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Sets up the main JFrame and Menu Bar for the Pac-Man application.
 */
public class GameFrame extends JFrame {
    private final GameEngine engine;
    private GamePanel gamePanel;

    public GameFrame(String title, GameEngine engine) {
        super(title);
        this.engine = engine;
        initUI();
    }

    private void initUI() {
        // --- Setup Panel and Frame ---\n
        gamePanel = new GamePanel(engine);
        add(gamePanel);
        setJMenuBar(createMenuBar());

        // --- Frame Settings ---\n
        pack(); // Sizes the frame to fit the preferred size of the GamePanel
        setTitle("Pac-Man (OOP & GUI)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null); // Center the window
        setVisible(true);

        // --- Start Game ---\n
        // engine.startNewGame() is called by the GamePanel's timer/constructor, but starting it here 
        // ensures the engine state is correct before the UI fully renders.
        engine.startNewGame();
        gamePanel.startTimer();
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");

        // 1. New Game Item
        JMenuItem newGameItem = new JMenuItem("New Game (N)");
        newGameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        newGameItem.addActionListener(e -> {
            // Stop the game, restart the engine, and restart the timer
            // USE PUBLIC ACCESSORS HERE TO FIX THE ERROR
            if (gamePanel.isTimerRunning()) {
                gamePanel.stopGame();
            }
            engine.startNewGame();
            gamePanel.startTimer();
            gamePanel.requestFocusInWindow();
        });

        // 2. Quit Item
        JMenuItem quitItem = new JMenuItem("Quit (Q)");
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        quitItem.addActionListener(e -> {
            engine.quitGame();
            System.exit(0);
        });

        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(quitItem);
        menuBar.add(gameMenu);
        return menuBar;
    }
}