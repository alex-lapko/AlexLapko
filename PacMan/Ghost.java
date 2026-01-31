import java.util.Random;

/**
 * Represents an enemy Ghost with chase and flee behavior. Extends GameObject.
 */
public class Ghost extends GameObject {
    private final Random random = new Random();
    private char contentUnderGhost = Map.EMPTY; // The map content this ghost is covering

    public Ghost(int initialRow, int initialCol) {
        super(initialRow, initialCol);
    }

    public char getContentUnderGhost() {
        return contentUnderGhost;
    }

    public void setContentUnderGhost(char content) {
        this.contentUnderGhost = content;
    }

    /**
     * Ghost's movement logic is implemented externally in GameEngine to simplify
     * central collision and state management.
     */
    @Override
    public void move(Map map) {
        // Main movement logic is handled in GameEngine.updateGhostPosition
    }
}