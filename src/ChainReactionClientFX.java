import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Border;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import netgame.common.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;


public class ChainReactionClientFX extends Client{
    private final static int PORT = 37829;
    private static int width = 625;
    private static int height = 700;
    private static int down = 65;
    private static int length = (int) (width * .06);
    private static int rowNumber = 11;
    private static int columnNumber = 11;
    private static ArrayList<Integer> horizontalLines = new ArrayList<>();
    private static ArrayList<Integer> verticalLines = new ArrayList<>();
    private GameBoard gameBoard;
    private Ball[][] board;
    LinkedBlockingQueue<ExplodeEvent> explodeQueue = new LinkedBlockingQueue<>();
    private GameScreen screen;
    private boolean isFinished;
    //Potentially could break idek
    private boolean turn;

    @Override
    protected void messageReceived(Object message) {
        if (message instanceof GameBoard) {
            turn = false;
            gameBoard =(GameBoard) message;
            board = gameBoard.getOldBoard();
            explodeQueue = gameBoard.getExplodeQueue();
        }
        if (message instanceof String && message.equals("Your Turn")) {
            turn = true;
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
        screen.launcher(args);
        for (int x = 0; x < rowNumber; x++) {
            verticalLines.add(length);
            length = length + (int) (width * .08);
        }
        for(int x = 0; x < columnNumber; x++){
            horizontalLines.add(down);
            down = down + (int) (width * .08);
        }
        verticalLines.add(length);
    }
    public static void main(String[] args) {
        try {
            new ChainReactionClientFX("192.168.56.1",args);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private class GameScreen extends Application {

        public void start(Stage stage) {
            Label message = new Label("CHAIN REACTION!");
            message.setFont( new Font(40) );

            Canvas canvas = new Canvas();
            Canvas animationCanvas = new Canvas();
            canvas.setOnMousePressed(evt -> {
                Color color = gameBoard.getBoardColor();
                if (isMoveLegal(evt.getX(), evt.getY(),color)) {
                    double x = evt.getX();
                    double y = evt.getY();
                    int xBallPosition = 0, yBallPosition = 0;
                    for (int a = 0; a < verticalLines.size() - 1; a++) {
                        if ((verticalLines.get(a) <= x) && (verticalLines.get(a + 1) >= x)) {
                            xBallPosition = a;
                        }
                    }
                    for (int a = 0; a < horizontalLines.size() - 1; a++) {
                        if ((horizontalLines.get(a) <= y) && (horizontalLines.get(a + 1)) >= y) {
                            yBallPosition = a;
                        }
                    }
                    if ((y >= (horizontalLines.size()-1)) && y <= horizontalLines.size()){
                        yBallPosition = rowNumber;
                    }
                    if ((x >= (verticalLines.size()-1)) && x <= verticalLines.size()){
                        xBallPosition = columnNumber;
                    }
                    if (!(color.equals(board[xBallPosition][yBallPosition].getBallColor()) || (board[xBallPosition][yBallPosition].getBallColor() == null))) {
                        return;
                    }
                    board[xBallPosition][yBallPosition].setValue((board[xBallPosition][yBallPosition].getValue()) + 1);
                    board[xBallPosition][yBallPosition].setBallColor(color);
                    gameBoard.setBoard(board);
                    send(gameBoard);
                }
            });

            BorderPane root = new BorderPane();
            root.setCenter(canvas);
            root.setTop(message);
            StackPane stack = new StackPane(root,animationCanvas);
            Scene scene = new Scene(stack, 625, 700);
            stage.setScene(scene);
            stage.setTitle("Chain Reaction");
            boardDrawer(canvas.getGraphicsContext2D(), animationCanvas.getGraphicsContext2D());
            stage.show();
        }
        private void launcher(String[] args){
            launch(args);
        }
        private void boardDrawer(GraphicsContext g,GraphicsContext g2){
            drawLines(g);
            for (int x = 0; x < board.length; x++) {
                for (int y = 0; y < board[0].length; y++) {
                    if (board[x][y].getValue() != 0) {
                        ballDrawer(x,y,g,null,null,0);
                        g.strokeText(String.valueOf(board[x][y].getValue()), ((verticalLines.get(x)) + 5), (horizontalLines.get(y)) + 5);
                    }
                }
            }
            if(explodeQueue.peek() != null){
                ExplodeEvent explode = explodeQueue.peek();
                int counter = 0;
                isFinished = false;
                while(!isFinished){
                    drawBall(explode.getX(), explode.getY(),g2,counter);
                    counter++;
                    g2.clearRect(0,0,width,height);
                }
                updateBoard(explode.getX(), explode.getY());
                explodeQueue.poll();
            }
            //replaces client-side calculated board with server calculated board
            //Should be identical REPORT IF NOT
            board = gameBoard.getBoard();
        }
        private void drawBall(int x, int y, GraphicsContext g2,int counter){
            if (board[x][y].getMaxValue() == 2) {
                if ((x == 0 && y == 0)) {
                    ballDrawer(x,y,g2,"+","",counter);
                    ballDrawer(x,y,g2,"","+",counter);
                    if(counter + 5 + verticalLines.get(x) >= verticalLines.get(x+1)){
                        isFinished = true;
                    }
                    
                } else if (x == 0 && y == (board[0].length - 1)) {
                    ballDrawer(x,y,g2,"+","",counter);
                    ballDrawer(x,y,g2,"","-",counter);
                    if(counter + 5 + verticalLines.get(x) >= verticalLines.get(x+1)){
                        isFinished = true;
                    }

                } else if (x == (board.length - 1) && y == 0) {
                    ballDrawer(x,y,g2,"-","",counter);
                    ballDrawer(x,y,g2,"","+",counter);
                    if(5 - counter + verticalLines.get(x) <= verticalLines.get(x-1)){
                        isFinished = true;
                    }
                    
                } else {
                    ballDrawer(x,y,g2,"-","",counter);
                    ballDrawer(x,y,g2,"","-",counter);
                    if(5 - counter + verticalLines.get(x) <= verticalLines.get(x-1)){
                        isFinished = true;
                    }
                    
                }
            } else if (board[x][y].getMaxValue() == 3) {
                if (x > 0 && y == 0) {
                    ballDrawer(x,y,g2,"-","",counter);
                    ballDrawer(x,y,g2,"","+",counter);
                    ballDrawer(x,y,g2,"+","",counter);
                    if(counter + 5 + verticalLines.get(x) >= verticalLines.get(x+1)){
                        isFinished = true;
                    }
                    
                } else if (x > 0 && y == board[0].length - 1) {
                    ballDrawer(x,y,g2,"-","",counter);
                    ballDrawer(x,y,g2,"","-",counter);
                    ballDrawer(x,y,g2,"+","",counter);
                    if(counter + 5 + verticalLines.get(x) >= verticalLines.get(x+1)){
                        isFinished = true;
                    }
                    

                } else if (x == 0 && y > 0) {
                    ballDrawer(x,y,g2,"","-",counter);
                    ballDrawer(x,y,g2,"+","",counter);
                    ballDrawer(x,y,g2,"","+",counter);
                    if(counter + 5 + verticalLines.get(x) >= verticalLines.get(x+1)){
                        isFinished = true;
                    }
                    
                } else if (x == board.length - 1 && y > 0) {
                    ballDrawer(x,y,g2,"","-",counter);
                    ballDrawer(x,y,g2,"-","",counter);
                    ballDrawer(x,y,g2,"","+",counter);
                    if(5 - counter + verticalLines.get(x) <= verticalLines.get(x-1)){
                        isFinished = true;
                    }

                }
            } else if (board[x][y].getMaxValue() == 4) {
                ballDrawer(x,y,g2,"+","",counter);
                ballDrawer(x,y,g2,"-","",counter);
                ballDrawer(x,y,g2,"","+",counter);
                ballDrawer(x,y,g2,"","-",counter);
                if(counter + 5 + verticalLines.get(x) >= verticalLines.get(x+1)){
                    isFinished = true;
                }
                
            }
        }
        private void updateBoard(int x, int y){
            board[x][y].setValue(0);
            if (board[x][y].getMaxValue() == 2) {
                if ((x == 0 && y == 0)) {
                    board[x + 1][y].setValue(board[x + 1][y].getValue() + 1);
                    board[x][y + 1].setValue(board[x][y + 1].getValue() + 1);

                } else if (x == 0 && y == (board[0].length - 1)) {
                    board[x + 1][y].setValue(board[x + 1][y].getValue() + 1);
                    board[x][y - 1].setValue(board[x][y - 1].getValue() + 1);

                } else if (x == (board.length - 1) && y == 0) {
                    board[x - 1][y].setValue(board[x - 1][y].getValue() + 1);
                    board[x][y + 1].setValue(board[x][y + 1].getValue() + 1);

                } else {
                    board[x - 1][y].setValue(board[x - 1][y].getValue() + 1);
                    board[x][y - 1].setValue(board[x][y - 1].getValue() + 1);
                }
            } else if (board[x][y].getMaxValue() == 3) {
                if (x > 0 && y == 0) {
                    board[x - 1][y].setValue(board[x - 1][y].getValue() + 1);
                    board[x][y + 1].setValue(board[x][y + 1].getValue() + 1);
                    board[x + 1][y].setValue(board[x + 1][y].getValue() + 1);
                } else if (x > 0 && y == board[0].length - 1) {
                    board[x - 1][y].setValue(board[x - 1][y].getValue() + 1);
                    board[x][y - 1].setValue(board[x][y - 1].getValue() + 1);
                    board[x + 1][y].setValue(board[x + 1][y].getValue() + 1);

                } else if (x == 0 && y > 0) {
                    board[x][y - 1].setValue(board[x][y - 1].getValue() + 1);
                    board[x + 1][y].setValue(board[x + 1][y].getValue() + 1);
                    board[x][y + 1].setValue(board[x][y + 1].getValue() + 1);

                } else if (x == board.length - 1 && y > 0) {
                    board[x][y - 1].setValue(board[x][y - 1].getValue() + 1);
                    board[x - 1][y].setValue(board[x - 1][y].getValue() + 1);
                    board[x][y + 1].setValue(board[x][y + 1].getValue() + 1);
                }
            } else if (board[x][y].getMaxValue() == 4) {
                board[x + 1][y].setValue(board[x + 1][y].getValue() + 1);
                board[x - 1][y].setValue(board[x - 1][y].getValue() + 1);
                board[x][y + 1].setValue(board[x][y + 1].getValue() + 1);
                board[x][y - 1].setValue(board[x][y - 1].getValue() + 1);
            }
        }

        private void ballDrawer(int x, int y, GraphicsContext g,String a, String b,int counter){
            int deltaX = 5;
            int deltaY = 5;
            if(a != null && b != null){
                if(a.equals("+")){
                    deltaX += counter;
                }
                else if(a.equals("-")){
                    deltaX -= counter;
                }
                else if(b.equals("+")){
                    deltaY += counter;
                }
                else if(b.equals("-")){
                    deltaY -= counter;
                }
            }
            RadialGradient gradient = new RadialGradient(0,0,verticalLines.get(x) + deltaX,
                    horizontalLines.get(y) + deltaY,45,true, CycleMethod.NO_CYCLE,
                    new Stop(0,board[x][y].getBallColor()), new Stop(1,Color.BLACK));
            g.setFill(gradient);
            g.strokeOval(verticalLines.get(x) + deltaX,horizontalLines.get(y) + deltaY,45,45);
        }
        private void drawLines(GraphicsContext g){
            Color gameColor = gameBoard.getBoardColor();
            g.setFill(gameColor);
            for (int x = 0; x < rowNumber; x++) {
                g.strokeLine(verticalLines.get(x) , horizontalLines.get(0) , verticalLines.get(x), horizontalLines.get(horizontalLines.size() - 1));
            }
            //horizontal lines
            for (int x = 0; x < columnNumber; x++) {
                g.strokeLine(verticalLines.get(0) ,  horizontalLines.get(x) , verticalLines.get(verticalLines.size() - 1) , horizontalLines.get(x) );
            }
            g.strokeLine(verticalLines.get(verticalLines.size() - 1) , horizontalLines.get(0),
                    verticalLines.get(verticalLines.size() - 1) , horizontalLines.get(horizontalLines.size() - 1));
        }

    }
    private boolean isMoveLegal(double x, double y, Color color){
        if ((x <= 0) || ( x >= 625) || (y <= 0) || (y >= 700)) {
            return false;
        }
        else if(color == null){
            return true;
        }
        return turn;
    }
}
