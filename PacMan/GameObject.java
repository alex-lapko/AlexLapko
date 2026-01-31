/**
 * Abstract base class for movable entities like PacManCharacter and Ghost.
 */
public abstract class GameObject {
    protected int row;
    protected int col;
    protected int directionRow; // -1 (up), 0 (none), 1 (down)
    protected int directionCol; // -1 (left), 0 (none), 1 (right)

    public GameObject(int initialRow, int initialCol) {
        this.row = initialRow;
        this.col = initialCol;
        this.directionRow = 0;
        this.directionCol = 0;
    }

    // Abstract method for core movement logic (must be implemented by subclasses)
    public abstract void move(Map map);

    public int getRow() { return row; }
    public int getCol() { return col; }

    public void setPosition(int r, int c) {
        this.row = r;
        this.col = c;
    }
}