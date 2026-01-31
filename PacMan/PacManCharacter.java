/**
 * Represents the player-controlled Pac-Man. Extends GameObject.
 */
public class PacManCharacter extends GameObject {
    private static final int POWER_UP_DURATION = 10; // seconds
    private boolean powerUpActive = false;
    private long powerUpStartTime = 0;
    private int score = 0;

    public PacManCharacter(int initialRow, int initialCol) {
        super(initialRow, initialCol);
    }

    /**
     * Sets the desired movement direction (W, A, S, D).
     */
    public void setDirection(char move) {
        directionRow = 0;
        directionCol = 0;
        switch (Character.toUpperCase(move)) {
            case 'W': directionRow = -1; break;
            case 'S': directionRow = 1; break;
            case 'A': directionCol = -1; break;
            case 'D': directionCol = 1; break;
        }
    }

    @Override
    public void move(Map map) {
        int newRow = row + directionRow;
        int newCol = col + directionCol;

        // Handle Warp Tunnel
        if (newCol < 0 && newRow == map.getRows() / 2) {
            newCol = map.getCols() - 1; // Warp left to right
        } else if (newCol >= map.getCols() && newRow == map.getRows() / 2) {
            newCol = 0; // Warp right to left
        }

        if (!map.isWall(newRow, newCol)) {
            // Check for Power Up
            if (map.getCell(newRow, newCol) == Map.POWER_UP) {
                activatePowerUp();
                map.setCell(newRow, newCol, Map.EMPTY); // Consume power up
            }

            // Check for Pellet
            if (map.hasPellet(newRow, newCol)) {
                score++;
                map.consumePellet(newRow, newCol);
            }

            // Update position
            row = newRow;
            col = newCol;
        }
        // Reset direction after attempting to move
        directionRow = 0;
        directionCol = 0;
    }

    public void activatePowerUp() {
        // Logic for power up activation is intentionally commented out/disabled
        this.powerUpActive = true; 
        this.powerUpStartTime = System.currentTimeMillis(); 
    }

    public void checkPowerUpTimer() {
        if (powerUpActive) {
            long elapsedTime = (System.currentTimeMillis() - powerUpStartTime) / 1000; // time in seconds
            if (elapsedTime >= POWER_UP_DURATION) {
                powerUpActive = false;
                powerUpStartTime = 0;
            }
        }
    }


    public boolean isPowerUpActive() {
        return this.powerUpActive;
    }

    public int getPowerUpTimeLeft() {
        if (!powerUpActive) {
            return 0;
        }
        long elapsedTime = (System.currentTimeMillis() - powerUpStartTime) / 1000;
        int timeLeft = POWER_UP_DURATION - (int) elapsedTime;
        return Math.max(0, timeLeft);
    }

    public void increaseScore(int points) { score += points; }
    public int getScore() { return score; }
    
    // FIX: Added method to resolve compilation error in GameEngine.java
    public void resetScore() { this.score = 0; }
}