import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Handles the graphical rendering of the game state using Swing.
 */
public class GamePanel extends JPanel implements ActionListener {
    private final GameEngine engine;
    private final Timer gameLoopTimer;
    private static final int TILE_SIZE = 30; // Size of each grid cell in pixels
    private static final int DELAY = 150; // Game loop delay in milliseconds (affects speed)

    public GamePanel(GameEngine engine) {
        this.engine = engine;
        this.setPreferredSize(new Dimension(engine.getMap().getCols() * TILE_SIZE,
                                            engine.getMap().getRows() * TILE_SIZE));
        this.setBackground(Color.BLACK);

        // Setup game timer for continuous updates
        gameLoopTimer = new Timer(DELAY, this);

        // Add Key Listener for Pac-ManCharacter control
        setFocusable(true);
        addKeyListener(new GameKeyAdapter(engine.getPacMan()));
    }

    // --- Public Methods for External Control ---

    public boolean isTimerRunning() {
        return gameLoopTimer.isRunning();
    }

    public void startTimer() {
        gameLoopTimer.start();
    }

    public void stopGame() {
        gameLoopTimer.stop();
    }

    // Called automatically by the Timer (ActionListener interface)
    @Override
    public void actionPerformed(ActionEvent e) {
        engine.update();
        if (engine.isGameOver() || engine.isVictory()) {
            gameLoopTimer.stop();
            // Trigger a final repaint to show the game over/victory state
            repaint();

            // Display result message
            String message = engine.isVictory() ? "VICTORY! Press N for New Game." : "GAME OVER! Press N for New Game.";
            JOptionPane.showMessageDialog(this, message, "Game Ended", JOptionPane.INFORMATION_MESSAGE);
        } else {
            repaint();
        }
    }

    // --- Drawing Logic ---

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Cast to Graphics2D for anti-aliasing (smoother drawing)
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Draw Map/Walls
        drawMap(g);

        // 2. Draw Ghosts
        drawGhosts(g);

        // 3. Draw Pac-Man
        drawPacMan(g);

        // 4. Draw Status Overlay
        drawStatus(g);

        // 5. Draw Game Over/Victory message if needed
        if (engine.isGameOver()) {
            drawEndScreen(g, "GAME OVER", Color.RED);
        } else if (engine.isVictory()) {
            drawEndScreen(g, "VICTORY", Color.CYAN);
        }

        Toolkit.getDefaultToolkit().sync();
    }

    /**
     * Draws the main map, including walls, pellets, and power-ups.
     */
    private void drawMap(Graphics g) {
        Map map = engine.getMap();
        int rows = map.getRows();
        int cols = map.getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = c * TILE_SIZE;
                int y = r * TILE_SIZE;

                char content = map.getCell(r, c);

                if (content == Map.WALL) {
                    g.setColor(Color.BLUE.darker().darker());
                    g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    // Add a small border effect
                    g.setColor(Color.BLUE);
                    g.drawRect(x, y, TILE_SIZE, TILE_SIZE);
                } else {
                    // Draw Pellets
                    if (map.hasPellet(r, c)) {
                        if (content == Map.PELLET) {
                            g.setColor(Color.WHITE);
                            g.fillOval(x + TILE_SIZE / 2 - 2, y + TILE_SIZE / 2 - 2, 4, 4);
                        }
                        // Draw Power Ups (Bigger dot)
                        else if (content == Map.POWER_UP) {
                            g.setColor(Color.ORANGE);
                            g.fillOval(x + TILE_SIZE / 2 - 6, y + TILE_SIZE / 2 - 6, 12, 12);
                        }
                    }
                }
            }
        }
    }

    /**
     * Draws the ghosts based on their position and current state.
     */
    private void drawGhosts(Graphics g) {
        PacManCharacter pacMan = engine.getPacMan();

        for (Ghost ghost : engine.getGhosts()) {
            int ghostX = ghost.getCol() * TILE_SIZE;
            int ghostY = ghost.getRow() * TILE_SIZE;

            // Determine ghost color
            if (pacMan.isPowerUpActive()) {
                // Frightened state
                g.setColor(Color.BLUE);
            } else {
                // Regular state (using different colors for distinction)
                if (engine.getGhosts().indexOf(ghost) == 0) g.setColor(Color.RED);
                else if (engine.getGhosts().indexOf(ghost) == 1) g.setColor(Color.PINK);
                else if (engine.getGhosts().indexOf(ghost) == 2) g.setColor(Color.CYAN);
                else g.setColor(Color.MAGENTA);
            }

            // Draw the ghost body as a rounded square with a "skirt"
            g.fillRoundRect(ghostX + 2, ghostY + 2, TILE_SIZE - 4, TILE_SIZE - 4, 10, 10);
            
            // Draw eyes (white for sclera)
            g.setColor(Color.WHITE);
            g.fillOval(ghostX + 6, ghostY + 8, 8, 8);
            g.fillOval(ghostX + TILE_SIZE - 14, ghostY + 8, 8, 8);
            
            // Draw pupils (black)
            g.setColor(Color.BLACK);
            g.fillOval(ghostX + 8, ghostY + 10, 4, 4);
            g.fillOval(ghostX + TILE_SIZE - 12, ghostY + 10, 4, 4);
        }
    }

    /**
     * Draws the Pac-Man character.
     */
    private void drawPacMan(Graphics g) {
        PacManCharacter pacMan = engine.getPacMan();
        boolean pacManInvincible = pacMan.isPowerUpActive();

        int pacX = pacMan.getCol() * TILE_SIZE;
        int pacY = pacMan.getRow() * TILE_SIZE;

        // Pac-Man color: Yellow, or Green if invincible
        g.setColor(pacManInvincible ? Color.GREEN : Color.YELLOW);
        // Draw Pac-Man as a simple arc/circle
        g.fillArc(pacX + 2, pacY + 2, TILE_SIZE - 4, TILE_SIZE - 4, 45, 270);

        // 4. Draw Status Overlay
        drawStatus(g);

        Toolkit.getDefaultToolkit().sync();
    }

    /**
     * Draws the score, level, and power-up status.
     */
    private void drawStatus(Graphics g) {
        PacManCharacter pacMan = engine.getPacMan();

        // REMOVED CODE:
        // int boxHeight = 50;
        // g.setColor(new Color(0, 0, 0, 150)); // Semi-transparent black
        // g.fillRect(0, 0, this.getWidth(), boxHeight);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 18));

        String scoreText = String.format("Score: %d", pacMan.getScore());
        String levelText = String.format("Level %d / %d", engine.getLevel(), engine.getMaxLevels());

        g.drawString(scoreText, 10, 20);
        g.drawString(levelText, 10, 40);

        if (pacMan.isPowerUpActive()) {
            g.setColor(Color.GREEN);
            String powerUpTime = String.format("POWER UP: %d sec", pacMan.getPowerUpTimeLeft());
            g.drawString(powerUpTime, this.getWidth() - 200, 40);
        }
    }

    /**
     * Draws the final game message (GAME OVER or VICTORY).
     */
    private void drawEndScreen(Graphics g, String message, Color color) {
        g.setColor(new Color(0, 0, 0, 200)); // Dark overlay
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        g.setColor(color);
        g.setFont(new Font("Monospaced", Font.BOLD, 48));
        
        FontMetrics fm = g.getFontMetrics();
        int x = (this.getWidth() - fm.stringWidth(message)) / 2;
        int y = (this.getHeight() - fm.getHeight()) / 2 + fm.getAscent();

        g.drawString(message, x, y);

        g.setFont(new Font("Monospaced", Font.PLAIN, 24));
        String instruction = "Press Ctrl+N to start a New Game.";
        fm = g.getFontMetrics();
        x = (this.getWidth() - fm.stringWidth(instruction)) / 2;
        y += fm.getHeight() + 20;

        g.drawString(instruction, x, y);
    }
}