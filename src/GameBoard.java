import java.awt.*;
import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;


public class GameBoard implements Serializable{
    private LinkedBlockingQueue<ExplodeEvent> explodeQueue;
    private Ball[][] board;
    private Ball[][] oldBoard;
    private Color boardColor;
    private final static long serialVersionUID = 123456234;
    GameBoard(Ball[][] balls,Color color){
        board = balls;
        oldBoard = balls;
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
    public Ball[][] getOldBoard(){ return oldBoard; }
    public void setOldBoard(Ball[][] balls){ oldBoard = balls;}
}
