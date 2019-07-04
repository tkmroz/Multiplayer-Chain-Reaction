

import java.awt.*;
import java.util.ArrayList;

/**
 * Defined variables for Players
 */

/**
 * TODO: Implement player names
 */
public class Player {
    private final Color playerColor;
    private final Integer playerNumber;
    private static Color[]colors = new Color[9];
    private int playerID;

    Player(int number, int ID){
        colors[0] = Color.RED;
        colors[1] = new Color(0, 120, 0);
        colors[2] = Color.BLUE;
        colors[3] = Color.YELLOW;
        colors[4] = Color.WHITE;
        colors[5] = Color.ORANGE;
        colors[6] = Color.MAGENTA;
        colors[7] = new Color(128,214,255);
        colors[8] = Color.BLACK;
        playerColor = colors[number];
        playerNumber = number;
        playerID = ID;
    }
    public Color getPlayerColor() {
        return playerColor;
    }

    public Integer getPlayerNumber(){ return  playerNumber;}
    public Color[] getColors(){
        return colors;
    }
}
