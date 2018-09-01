

import java.awt.*;
import java.util.ArrayList;

/**
 * Defined variables for Players
 */
public class Player {
    //private final String playerName;
    private final Color playerColor;
    private ArrayList<Color> colors = new ArrayList<>(9);
    private int playerID;
    private int i;

    Player(Integer number, int id){
        colors.add(Color.RED);
        colors.add(new Color(0, 120, 0));
        colors.add(Color.BLUE);
        colors.add(Color.YELLOW);
        colors.add(Color.WHITE);
        colors.add(Color.ORANGE);
        colors.add(Color.MAGENTA);
        colors.add(new Color(128,214,255));
        colors.add(Color.BLACK);
        //playerName = name;
        playerColor = colors.get(number);
        //hasPlayed = false;
        playerID = id;
    }
    //public String getPlayerName(){
        //return playerName;
    //}

    public Color getPlayerColor() {
        return playerColor;
    }

    public Integer getPlayerID() {
        return playerID;
    }

    public ArrayList<Color> getColors() {
        return colors;
    }
}
