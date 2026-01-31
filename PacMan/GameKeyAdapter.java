import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Handles keyboard input for controlling Pac-ManCharacter.
 */
public class GameKeyAdapter extends KeyAdapter {
    private final PacManCharacter pacMan;

    public GameKeyAdapter(PacManCharacter pacMan) {
        this.pacMan = pacMan;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        char key = Character.toUpperCase(e.getKeyChar());
        // Set the desired direction for Pac-ManCharacter on the next game tick
        switch (key) {
            case 'W': // Up
            case 'A': // Left
            case 'S': // Down
            case 'D': // Right
                pacMan.setDirection(key);
                break;
        }
    }
}