import java.awt.*;

public class BallEvent {
    private int startX;
    private int startY;
    private int ballValue;
    private Color ballColor;
    private final String type;
    private final int ballID;
    private boolean isUsed;

    BallEvent(int x, int y, int value, Color color) {
        startX = x;
        startY = y;
        ballValue = value;
        ballColor = color;
        switch (value) {
            case 2:
                type = "Corner";
                break;
            case 3:
                type = "Edge";
                break;
            default:
                type = "Middle";
                break;
        }
        ballID = (87 * startX) + (423 * startY);
        isUsed = false;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }
    public Color getBallColor() {
        return ballColor;
    }

    public void setBallColor(Color ballColor) {
        this.ballColor = ballColor;
    }

    public int getBallValue() {
        return ballValue;
    }

    public void setBallValue(int ballValue) {
        this.ballValue = ballValue;
    }
    public String getType(){
        return type;
    }

    public int getBallID() {
        return ballID;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public BallEvent setUsed(boolean used) {
        isUsed = used;
        return this;
    }
}
