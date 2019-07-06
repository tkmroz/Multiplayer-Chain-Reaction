

import java.awt.*;
import java.io.Serializable;

/**
 * Defines important ball variables used in the array
 */
class Ball implements Serializable{
    private Color ballColor;
    private final int maxValue;
    private int value;
    private final static long serialVersionUID = 123456788;

    Ball(int type) {
        value = 0;
        maxValue = type;
    }

     int getValue() { return value; }

     Color getBallColor() { return ballColor; }

     int getMaxValue() { return maxValue; }

     void setValue(int v) { value = v; }

     void setBallColor(Color c) { ballColor = c; }

}
