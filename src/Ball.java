

import java.awt.*;
import java.io.Serializable;

/**
 * Defines important ball variables used in the array
 */
public class Ball implements Serializable{
    private Color ballColor;
    private final int maxValue;
    private int value;
    private final static long serialVersionUID = 123456789;
    private Color boardColor;

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
}