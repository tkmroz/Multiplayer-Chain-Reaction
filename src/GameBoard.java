import java.awt.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.Serializable;

public class GameBoard {
    private LinkedBlockingQueue<ExplodeEvent> explodeQueue;
    private Ball[][] board;
    private Color boardColor;
    private final static long serialVersionUID = 123456234;
    GameBoard(Ball[][] balls,Color color){
        board = balls;
        explodeQueue = new LinkedBlockingQueue<>();
        boardColor = color;
    }

    public void setExplodeQueue(LinkedBlockingQueue q){
        explodeQueue = q;
    }
    public LinkedBlockingQueue<ExplodeEvent> getExplodeQueue(){
        return explodeQueue;
    }
    public void setBoard(Ball[][] b){
        board = b;
    }
    public Ball[][] getBoard(){
        return board;
    }
    public void setBoardColor(Color c){
        boardColor = c;
    }
    public Color getBoardColor(){
        return boardColor;
    }
}
