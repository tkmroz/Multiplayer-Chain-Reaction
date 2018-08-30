import java.util.ArrayList;

public class MovingBall extends Ball {
    private int xLocation;
    private int yLocation;
    int ballAmount;
    public MovingBall(String type){
        super(type);
    }

    public int getyLocation() {
        return yLocation;
    }
    public void setyLocation(int yLocation) {
        this.yLocation = yLocation;
    }
    public int getxLocation() {
        return xLocation;
    }
    public void setxLocation(int xLocation) {
        this.xLocation = xLocation;
    }
}
