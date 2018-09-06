
import java.awt.*;

public class MovingBall extends Ball implements Cloneable{
    private int xLocation;
    private int yLocation;
    private int row;
    private int column;
    private final int ballID;
    private int value;
    MovingBall(String type, int x, int y, int r, int c, int v, Color color){
        super(type, v, color);
        xLocation = x;
        yLocation = y;
        row = r;
        column = c;
        ballID = (87 * row) + (423 * column);
        value = v;
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
    @Override
    protected Object clone() throws CloneNotSupportedException{
        return super.clone();
    }
}
