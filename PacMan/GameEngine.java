import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Contains the core game loop logic, state management, and level progression.
 */
public class GameEngine {
    // --- Game Constants ---
    private static final int MAX_LEVELS = 5;
    private static final int BASE_PELLETS = 50;
    private static final int BASE_GHOSTS = 2;
    private static final int GHOST_SPEED_DELAY = 2; // Move ghosts every X ticks

    // --- Core Game Components ---
    private final Map map;
    private final PacManCharacter pacMan;
    private final List<Ghost> ghosts;
    private final Random random = new Random();
    
    // --- Helper for Random Fleeing Movement (Directions) ---
    private static final int[][] DIRECTIONS = {
        {-1, 0}, // Up
        {1, 0},  // Down
        {0, -1}, // Left
        {0, 1}   // Right
    };


    // --- Game State Variables ---
    private int level = 1;
    private boolean isRunning = false;
    private boolean isGameOver = false;
    private boolean isVictory = false;
    private int ticks = 0; // Tracks game steps for delayed ghost movement/respawn
    private final Point initialPacManPos;

    public GameEngine() {
        this.map = new Map();
        
        // Determine initial position for Pac-Man (center of the map)
        int initialRow = map.getRows() / 2;
        int initialCol = map.getCols() / 2;
        // Fallback if center is a wall
        if (map.isWall(initialRow, initialCol)) {
             initialRow = 1; initialCol = 1;
        }
        initialPacManPos = new Point(initialCol, initialRow);

        // Initialize Pac-Man
        this.pacMan = new PacManCharacter(initialRow, initialCol);

        // Initialize Ghosts in a separate, safe area
        this.ghosts = new ArrayList<>();
        initializeGhosts(); // Call a new method to handle safe ghost spawning
        
        // Note: isGameOver is intentionally set to false here, but startNewGame() resets it.
    }

    private void initializeGhosts() {
        // Clear old ghosts for new game/level
        ghosts.clear();
        
        // Use fixed, safe starting positions for ghosts (e.g., corners or top center)
        // Assuming a 19x19 map, (1, 17) is a good top-right spot.
        int safeGhostRow = 1;
        int safeGhostCol = map.getCols() - 2; // Top right corner (or near it)

        // Add BASE_GHOSTS ghosts
        for (int i = 0; i < BASE_GHOSTS + level; i++) {
            // Stagger their initial positions slightly if possible
            int startRow = safeGhostRow + (i % 2);
            int startCol = safeGhostCol - (i / 2);
            
            // Ensure the ghost position is valid before adding
            if (!map.isWall(startRow, startCol)) {
                Ghost newGhost = new Ghost(startRow, startCol);
                newGhost.setContentUnderGhost(map.getCell(startRow, startCol));
                ghosts.add(newGhost);
            }
        }
        
        // Fallback: If no ghosts were created due to map issues, add at least one safe one
        if (ghosts.isEmpty()) {
             Ghost fallbackGhost = new Ghost(1, 1);
             fallbackGhost.setContentUnderGhost(map.getCell(1, 1));
             ghosts.add(fallbackGhost);
        }
    }


    /**
     * Resets the game state and advances the level if needed.
     */
    public void startNewGame() {
        // Reset level and score if starting from Game Over/Quit
        if (isGameOver || isVictory || !isRunning) {
            level = 1;
            pacMan.resetScore();
        }

        isRunning = true;
        isGameOver = false; // <--- Critical: Ensure this is false on start!
        isVictory = false;

        // 1. Reset Map and Pellets
        map.reset(initialPacManPos.y, initialPacManPos.x);

        // 2. Reset Pac-Man position
        pacMan.setPosition(initialPacManPos.y, initialPacManPos.x);

        // 3. Re-initialize Ghosts in safe positions
        initializeGhosts();
        
        ticks = 0;
    }

    /**
     * Advances the game state by one tick.
     */
    public void update() {
        if (isGameOver || isVictory) return;

        // 1. Update Pac-Man's state and movement
        pacMan.checkPowerUpTimer();
        pacMan.move(map);
        
        // Check for victory condition immediately after Pac-Man moves
        checkVictoryCondition(); 

        // 2. Update Ghost movement (delayed speed)
        ticks++;
        if (ticks % GHOST_SPEED_DELAY == 0) {
            for (Ghost ghost : ghosts) {
                updateGhostPosition(ghost);
            }
        }

        // 3. Check collisions (only after movement/updates)
        checkGhostPacManCollision();
    }
    
    // --- Collision Logic (assuming this part is correct but showing for context) ---
    private void checkCollision(Ghost ghost) {
        if (pacMan.getRow() == ghost.getRow() && pacMan.getCol() == ghost.getCol()) {
            if (pacMan.isPowerUpActive()) {
                // Pac-Man eats Ghost
                pacMan.increaseScore(200);
                respawnGhost(ghost);
            } else {
                // Ghost eats Pac-Man
                isGameOver = true;
                isRunning = false;
            }
        }
    }

    private void checkGhostPacManCollision() {
        for (Ghost ghost : ghosts) {
            checkCollision(ghost);
            if (isGameOver) return;
        }
    }
    
    private void updateGhostPosition(Ghost ghost) {
        int nextRow = ghost.getRow();
        int nextCol = ghost.getCol();
        
        if (pacMan.isPowerUpActive()) {
            // --- Fleeing/Frightened State: Use Random Valid Movement ---
            List<int[]> validMoves = new ArrayList<>();
            for (int[] dir : DIRECTIONS) {
                int r = ghost.getRow() + dir[0];
                int c = ghost.getCol() + dir[1];
                
                // Ensure the move doesn't go back into the wall
                if (!map.isWall(r, c)) {
                    validMoves.add(dir);
                }
            }
            
            if (!validMoves.isEmpty()) {
                // Pick a random valid direction
                int[] chosenDir = validMoves.get(random.nextInt(validMoves.size()));
                nextRow = ghost.getRow() + chosenDir[0];
                nextCol = ghost.getCol() + chosenDir[1];
            } else {
                 // No valid move, stay put
                 return; 
            }
            
        } else {
            // --- Chasing State (Original Logic) ---
            int targetRow = pacMan.getRow();
            int targetCol = pacMan.getCol();

            int rowDiff = Integer.compare(targetRow, ghost.getRow());
            int colDiff = Integer.compare(targetCol, ghost.getCol());
            
            // Try horizontal movement first if row/col differences are similar
            if (Math.abs(colDiff) >= Math.abs(rowDiff)) {
                nextCol += colDiff;
                if (map.isWall(nextRow, nextCol)) {
                    // If wall, try vertical movement
                    nextCol = ghost.getCol(); // Reset
                    nextRow += rowDiff;
                    if (map.isWall(nextRow, nextCol)) {
                        // If still wall, revert to current position
                        nextRow = ghost.getRow();
                        nextCol = ghost.getCol();
                    }
                }
            } else {
                // Attempt vertical movement first
                nextRow += rowDiff;
                if (map.isWall(nextRow, nextCol)) {
                    // If wall, try horizontal movement
                    nextRow = ghost.getRow(); // Reset
                    nextCol += colDiff;
                    if (map.isWall(nextRow, nextCol)) {
                        // If still wall, revert to current position
                        nextCol = ghost.getCol();
                        nextRow = ghost.getRow();
                    }
                }
            }
        }

        // 1. Restore map content before moving
        map.setCell(ghost.getRow(), ghost.getCol(), ghost.getContentUnderGhost());

        // 2. Update ghost position
        ghost.setPosition(nextRow, nextCol);

        // 3. Store the new content the ghost is covering
        ghost.setContentUnderGhost(map.getCell(nextRow, nextCol));
    }


    private void respawnGhost(Ghost ghost) {
        int attempts = 0;
        // Use a fixed safe zone for respawn, which is the Top-Right corner.
        int respawnRow = 1;
        int respawnCol = map.getCols() - 2;

        if (!map.isWall(respawnRow, respawnCol)) {
            // FIX: Restore map content at the ghost's old position *before* moving it
            map.setCell(ghost.getRow(), ghost.getCol(), ghost.getContentUnderGhost());
            
            // Set new position
            ghost.setPosition(respawnRow, respawnCol);
            
            // Get content under the ghost at the new position
            ghost.setContentUnderGhost(map.getCell(respawnRow, respawnCol));
            return;
        }
        
        // Fallback respawn (if the fixed spot is somehow a wall)
        while (attempts < 50) {
            int r = random.nextInt(map.getRows());
            int c = random.nextInt(map.getCols());

            // Check for empty spot, not Pac-Man, and not a wall
            if (!map.isWall(r, c) && !(r == pacMan.getRow() && c == pacMan.getCol())) {
                // FIX: Restore map content at the ghost's old position *before* moving it
                map.setCell(ghost.getRow(), ghost.getCol(), ghost.getContentUnderGhost());
                
                // Set new position
                ghost.setPosition(r, c);
                
                // The ghost respawns onto whatever is in the spot (pellet, empty)
                ghost.setContentUnderGhost(map.getCell(r, c));
                return;
            }
            attempts++;
        }
        // Final fallback: use Pac-Man's initial spawn point, risking collision
        map.setCell(ghost.getRow(), ghost.getCol(), ghost.getContentUnderGhost()); // Restore old spot
        ghost.setPosition(initialPacManPos.y, initialPacManPos.x);
        ghost.setContentUnderGhost(Map.EMPTY);
    }
    
    private void checkVictoryCondition() {
         if (map.getPelletsRemaining() == 0) {
            if (level < MAX_LEVELS) {
                // Advance to next level
                level++;
                startNewGame(); // Recursively starts the next level
            } else {
                // Ultimate victory
                isVictory = true;
                isRunning = false;
            }
        }
    }


    // --- Public Getters for GUI Rendering ---
    public boolean isRunning() { return isRunning; }
    public boolean isGameOver() { return isGameOver; }
    public boolean isVictory() { return isVictory; }
    public PacManCharacter getPacMan() { return pacMan; }
    public List<Ghost> getGhosts() { return ghosts; }
    public Map getMap() { return map; }
    public int getLevel() { return level; }
    public int getMaxLevels() { return MAX_LEVELS; }
    public void quitGame() { isRunning = false; }
}