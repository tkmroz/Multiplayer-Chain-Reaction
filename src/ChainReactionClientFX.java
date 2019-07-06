import netgame.common.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
/**
 * Creates Main Menu and initializes variables
 * Sends to GameScreen using launch commands
 */
public class ChainReactionClientFX extends Client{
    private final static int PORT = 37829;
    static ChainReactionClientFX client;
    private static GameBoard gameBoard;
    private Ball[][] board;

    private GameScreen screen;
    private boolean isFinished;
    //Potentially could break idek
    private boolean turn = false;

    @Override
    protected void messageReceived(Object message) {
        if (message instanceof GameBoard) {
            if(!turn){
                String[] args = new String[3];
                turn = true;
                GameScreen.main(args,(GameBoard)message);
            }
            System.out.println("TORINOR");

            gameBoard =(GameBoard) message;
            screen.redraw(gameBoard);
        }
        if (message instanceof String && message.equals("Your Turn")) {
            GameScreen.setTurn(true);
        }
        if(message instanceof String && message.equals("YOU WON!")){
            //endgame
        }
    }
    @Override
    protected void extraHandshake(ObjectInputStream in, ObjectOutputStream out) throws IOException {
        out.writeObject("normal");
        out.flush();
    }
    private ChainReactionClientFX(String hubHostName, String[] args) throws IOException  {
        super(hubHostName,PORT);
        screen = new GameScreen();

    }
    public static void main(String[] args) {
        try {
            client = new ChainReactionClientFX("192.168.56.1",args);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    void sender(GameBoard b){
        send(b);
    }

}
