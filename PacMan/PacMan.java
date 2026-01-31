import javax.swing.SwingUtilities;

/**
 * It initializes the game logic (GameEngine) and the graphical interface (GameFrame).
 */
public class PacMan {

    public static void main(String[] args) {
        // Use SwingUtilities.invokeLater to ensure that all GUI-related code
        // is executed on the Event Dispatch Thread (EDT), which is mandatory for Swing applications.
        SwingUtilities.invokeLater(() -> {
            // 1. Initialize the core game logic
            GameEngine engine = new GameEngine();

            // 2. Create and display the main game window
            new GameFrame("Pac-Man (W, A, S, D Controls)", engine);
        });
    }
}