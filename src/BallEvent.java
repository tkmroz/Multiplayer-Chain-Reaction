import java.awt.*;

public class BallEvent {
    private int startX;
    private int startY;
    private int ballValue;
    private Color ballColor;
    private final String type;
    /*
    private int mouseX;
    private int mouseY;*/
    BallEvent(int x, int y, int value, Color color){
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
        //mouseX = x2;
       // mouseY = y2;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

   /* public int getMouseY() {
        return mouseY;
    }

    public int getMouseX() {
        return mouseX;
    }*/

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
    private void bs(){}
}
