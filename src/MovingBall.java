
public class MovingBall extends Ball {
    private int xLocation;
    private int yLocation;
    private int row;
    private int column;
    private final int ballID;
    MovingBall(String type, int x, int y, int r, int c){
        super(type);
        xLocation = x;
        yLocation = y;
        row = r;
        column = c;
        ballID = (87 * row) + (423 * column);
    }

    public int getyLocation() {
        return yLocation;
    }
    public void setyLocation(int y) {
        yLocation = y;
    }
    public int getxLocation() {
        return xLocation;
    }
    public void setxLocation(int xLocation) {
        this.xLocation = xLocation;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public int getBallID() {
        return ballID;
    }
}
