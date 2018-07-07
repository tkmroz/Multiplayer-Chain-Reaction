

import java.awt.*;
import java.util.ArrayList;

/**
 * Defined variables for Players
 */
public class Player {
    private final String playerName;
    private final Color playerColor;
    private final Integer playerNumber;
    private static ArrayList<Color> colors = new ArrayList<>(9);


    public Player(String name,Integer number){
        colors.add(Color.RED);
        colors.add(new Color(0, 120, 0));
        colors.add(Color.BLUE);
        colors.add(Color.YELLOW);
        colors.add(Color.WHITE);
        colors.add(Color.ORANGE);
        colors.add(Color.MAGENTA);
        colors.add(new Color(128,214,255));
        colors.add(Color.BLACK);
        playerName = name;
        playerColor = colors.get(number);
        playerNumber = number;
    }
    public String getPlayerName(){
        return playerName;
    }

    public Color getPlayerColor() {
        return playerColor;
    }

    public Integer getPlayerNumber(){ return  playerNumber;}

}
