

import java.awt.*;
import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Defines important ball variables used in the array
 */
public class Ball implements Serializable{
    private Color ballColor;
    private final int maxValue;
    private int value;
    private final static long serialVersionUID = 123456789;
    private Color boardColor;
    private LinkedBlockingQueue<BallEvent> ballQueue;

    Ball(String type) {
        value = 0;
        switch (type) {
            case "Corner":
                maxValue = 2;
                break;
            case "Edge":
                maxValue = 3;
                break;
            default:
                maxValue = 4;
                break;
        }
        ballQueue = new LinkedBlockingQueue<>();
    }

    public int getValue() {
        return value;
    }

    public Color getBallColor() {
        return ballColor;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public Color getBoardColor(){ return boardColor; }

    public void setValue(int v) {
        value = v;
    }

    public void setBallColor(Color c) {
        ballColor = c;
    }

    public void setBoardColor(Color c){ boardColor = c; }

    public LinkedBlockingQueue<BallEvent> getBallQueue() { return ballQueue;}

    public void setBallQueue(LinkedBlockingQueue<BallEvent> ballQueue) {this.ballQueue = ballQueue;}

    @Override
    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(obj instanceof Ball){
            Ball ball = (Ball) obj;
            return (value == ball.value) && (ballColor == ball.ballColor) && (maxValue == ball.maxValue);
        }
        return false;
    }
}

