
public class MovingBall extends Ball {
    private int xLocation;
    private int yLocation;
    private int row;
    private int column;
    int ballAmount;
    MovingBall(String type, int x, int y, int r, int c){
        super(type);
        xLocation = x;
        yLocation = y;
        row = r;
        column = c;
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
}
