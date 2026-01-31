import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages the game grid, including walls, pellets, and power-ups.
 */
public class Map {
    // Map dimensions (19x19)
    private static final int ROWS = 19;
    private static final int COLS = 19;
    public static final char WALL = '#';
    public static final char EMPTY = ' ';
    public static final char PELLET = '.';
    public static final char POWER_UP = 'I';

    private final char[][] grid;
    private final boolean[][] hasPellet;
    private int pelletsRemaining;
    private final Random random = new Random();

    // --- INTERMEDIATE Maze Layout (More walls, still fully reachable) ---
    private final int[][] obstacleLayout = {
        // --- 1. Ghost House Structure (Center) ---
        {8, 8}, {8, 10},
        {9, 8}, {9, 10}, 
        {10, 8}, {10, 10},

        // --- 2. Horizontal Corridors (creating T-shapes and defined paths) ---
        // Top half
        {4, 2}, {4, 3}, {4, 4}, {4, 5}, {4, 6}, {4, 7}, 
        {4, 11}, {4, 12}, {4, 13}, {4, 14}, {4, 15}, {4, 16},
        {6, 4}, {6, 5}, {6, 13}, {6, 14},

        // Bottom half
        {14, 2}, {14, 3}, {14, 4}, {14, 5}, {14, 6}, {14, 7}, 
        {14, 11}, {14, 12}, {14, 13}, {14, 14}, {14, 15}, {14, 16},
        {12, 4}, {12, 5}, {12, 13}, {12, 14},
        
        // --- 3. Vertical Barriers (connecting horizontal segments) ---
        // Outer segments
        {2, 2}, {3, 2}, {5, 2},
        {2, 16}, {3, 16}, {5, 16},
        {13, 2}, {15, 2}, {16, 2},
        {13, 16}, {15, 16}, {16, 16},

        // Inner segments
        {2, 7}, {3, 7}, {5, 7},
        {2, 11}, {3, 11}, {5, 11},
        {13, 7}, {15, 7}, {16, 7},
        {13, 11}, {15, 11}, {16, 11},

        // --- 4. Mid-level Blocks (blocking direct center cross) ---
        {9, 4}, {9, 5},
        {9, 13}, {9, 14},
    };

    public Map() {
        this.grid = new char[ROWS][COLS];
        this.hasPellet = new boolean[ROWS][COLS];
        this.pelletsRemaining = 0;
    }

    public int getRows() { return ROWS; }
    public int getCols() { return COLS; }
    public int getPelletsRemaining() { return pelletsRemaining; }

    /**
     * Resets the map grid for a new level/game, placing walls and pellets.
     * @param initialPacManRow Pac-Man's starting row to avoid placing items there.
     * @param initialPacManCol Pac-Man's starting column to avoid placing items there.
     */
    public void reset(int initialPacManRow, int initialPacManCol) {
        // --- 1. Fill the grid with pellets and reset pellet status ---
        pelletsRemaining = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                // Start by assuming every non-wall spot will be a pellet
                grid[r][c] = PELLET;
                hasPellet[r][c] = true;
            }
        }

        // --- 2. Place outer walls ---
        for (int c = 0; c < COLS; c++) {
            grid[0][c] = WALL;
            grid[ROWS - 1][c] = WALL;
        }
        for (int r = 0; r < ROWS; r++) {
            grid[r][0] = WALL;
            grid[r][COLS - 1] = WALL;
        }

        // Add a gap in the outer walls for a classic Pac-Man warp/hallway effect
        grid[ROWS / 2][0] = EMPTY;
        grid[ROWS / 2][COLS - 1] = EMPTY;

        // --- 3. Place internal obstacles (maze walls) ---
        for (int[] obs : obstacleLayout) {
            int r = obs[0];
            int c = obs[1];
            if (r >= 0 && r < ROWS && c >= 0 && c < COLS) {
                grid[r][c] = WALL;
                hasPellet[r][c] = false; // Walls don't have pellets
            }
        }

        // --- 4. Clear the space where Pac-Man starts and the Ghost house floor ---
        if (initialPacManRow >= 0 && initialPacManRow < ROWS && initialPacManCol >= 0 && initialPacManCol < COLS) {
            grid[initialPacManRow][initialPacManCol] = EMPTY;
            hasPellet[initialPacManRow][initialPacManCol] = false;
        }
        
        // Clear space for ghost start area (Ghost house floor at center 9, 9)
        grid[ROWS / 2 - 1][COLS / 2] = EMPTY; // (8, 9)
        hasPellet[ROWS / 2 - 1][COLS / 2] = false;
        grid[ROWS / 2][COLS / 2] = EMPTY; // (9, 9)
        hasPellet[ROWS / 2][COLS / 2] = false;
        grid[ROWS / 2 + 1][COLS / 2] = EMPTY; // (10, 9)
        hasPellet[ROWS / 2 + 1][COLS / 2] = false;


        // --- 5. Calculate remaining pellets ---
        pelletsRemaining = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] == PELLET) {
                    pelletsRemaining++;
                }
            }
        }

        // --- 6. Place Power Ups (four corners of the playable maze) ---
        placePowerUp(3, 1); // Top Left
        placePowerUp(3, COLS - 2); // Top Right
        placePowerUp(ROWS - 4, 1); // Bottom Left (15, 1)
        placePowerUp(ROWS - 4, COLS - 2); // Bottom Right (15, 17)
    }
    
    // Modified placePowerUp to place at a specific coordinate
    private void placePowerUp(int r, int c) {
        if (r >= 0 && r < ROWS && c >= 0 && c < COLS) {
             // Only place if it's currently a pellet
            if (grid[r][c] == PELLET) {
                grid[r][c] = POWER_UP;
            }
        }
    }

    public char getCell(int r, int c) {
        if (r < 0 || r >= ROWS || c < 0 || c >= COLS) {
             // Check if it's the warp tunnel (center-left/right edge)
             if (r == ROWS / 2 && (c == -1 || c == COLS)) return EMPTY;
             return WALL; // Treat outside as a wall otherwise
        }
        return grid[r][c];
    }

    public void setCell(int r, int c, char symbol) {
        if (r >= 0 && r < ROWS && c >= 0 && c < COLS) {
            grid[r][c] = symbol;
        }
    }

    public boolean isWall(int r, int c) {
        // Special check for the warp tunnel: center rows outside the map are not walls
        if (r == ROWS / 2 && (c == -1 || c == COLS)) return false; 
        return getCell(r, c) == WALL;
    }

    public boolean hasPellet(int r, int c) {
        if (r < 0 || r >= ROWS || c < 0 || c >= COLS) return false;
        return hasPellet[r][c];
    }

    public void consumePellet(int r, int c) {
        if (r >= 0 && r < ROWS && c >= 0 && c < COLS) {
            if (hasPellet[r][c]) {
                hasPellet[r][c] = false;
                pelletsRemaining--;
            }
        }
    }
}