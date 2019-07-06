import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class GameScreen extends Application {
    private static ArrayList<Integer> horizontalLines = new ArrayList<>();
    private static ArrayList<Integer> verticalLines = new ArrayList<>();
    private static int width = 625;
    private static int height = 700;
    private Ball[][] board;
    private static GameBoard gameBoard;
    private static int rowNumber = 11;
    private static int columnNumber = 11;
    private static int down = 65;
    private static int length = (int) (width * .06);
    LinkedBlockingQueue<ExplodeEvent> explodeQueue = new LinkedBlockingQueue<>();
    private boolean isFinished;
    private GraphicsContext graph;
    private GraphicsContext animationGraph;
    private static boolean turn;


    public static void main(String[] args, GameBoard gBoard){
        gameBoard = gBoard;
        launch(args);

    }
    public GameScreen(){
        super();
        turn = false;
    }
    public void start(Stage stage) {
        board = gameBoard.getBoard();
        Text message = new Text("CHAIN REACTION!");
        Font font = Font.loadFont(GameScreen.class.getResource("resources/Font/Sigmar_One/SigmarOne-Regular.ttf").toExternalForm(), 120);
        message.setFont( font );
        message.setX(300);
        message.setY(50);
        message.setTextAlignment(TextAlignment.CENTER);
        message.setFill(Color.WHITE);

        Canvas canvas = new Canvas(625,700);
        Canvas animationCanvas = new Canvas(625,700);
        animationCanvas.setOnMousePressed(evt -> {
            System.out.println("FUCK NO");
            Color color = ColorUtil.awtToFx(gameBoard.getBoardColor());
            if (isMoveLegal(evt.getX(), evt.getY(),color)) {
                turn = false;
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
                if (!(board[xBallPosition][yBallPosition].getBallColor() == null || color.equals(ColorUtil.awtToFx(board[xBallPosition][yBallPosition].getBallColor())))) {
                    turn = true;
                    return;
                }
                System.out.println("ET LIT");
                board[xBallPosition][yBallPosition].setValue((board[xBallPosition][yBallPosition].getValue()) + 1);
                board[xBallPosition][yBallPosition].setBallColor( ColorUtil.fxToAwt(color));
                gameBoard.setBoard(board);
                ChainReactionClientFX.client.sender(gameBoard);
            }
        });

        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        root.setCenter(canvas);
        root.setTop(message);

        StackPane stack = new StackPane(root,animationCanvas);
        Scene scene = new Scene(stack, 625, 700);
        stage.setScene(scene);
        stage.setTitle("Chain Reaction");
        graph = canvas.getGraphicsContext2D();
        animationGraph = animationCanvas.getGraphicsContext2D();
        boardDrawer(graph,animationGraph );
        stage.show();
    }
    @Override
    public void init(){
        for (int x = 0; x < rowNumber; x++) {
            verticalLines.add(length);
            length = length + (int) (width * .08);
        }
        for(int x = 0; x < columnNumber + 1; x++){
            horizontalLines.add(down);
            down = down + (int) (width * .08);
        }
        verticalLines.add(length);
    }
    private void boardDrawer(GraphicsContext g, GraphicsContext g2){
        drawLines(g);
        g.setStroke(ColorUtil.awtToFx( gameBoard.getBoardColor()));
        System.out.println(board[0][0].getValue());
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[0].length; y++) {
                if (board[x][y].getValue() != 0) {
                    ballDrawer(x, y, g, null, null, 0);
                    g.strokeText(String.valueOf(board[x][y].getValue()), ((verticalLines.get(x)))+ 3, (horizontalLines.get(y))+ 10);
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
        int deltaX = 3;
        int deltaY = 3;
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
                new Stop(1,ColorUtil.awtToFx(board[x][y].getBallColor())), new Stop(1,Color.BLACK));
        g.setFill(gradient);
        g.fillOval(verticalLines.get(x) + deltaX,horizontalLines.get(y) + deltaY,45,45);
    }
    private void drawLines(GraphicsContext g){
        Color gameColor = ColorUtil.awtToFx(gameBoard.getBoardColor());
        g.setStroke(gameColor);
        System.out.println(gameColor.getOpacity());
        for (int x = 0; x < rowNumber; x++) {
            g.strokeLine(verticalLines.get(x) , horizontalLines.get(0) , verticalLines.get(x), horizontalLines.get(horizontalLines.size() - 1));
        }
        //horizontal lines
        for (int x = 0; x < columnNumber + 1; x++) {
            g.strokeLine(verticalLines.get(0) ,  horizontalLines.get(x) , verticalLines.get(verticalLines.size() - 1) , horizontalLines.get(x) );
        }
        g.strokeLine(verticalLines.get(verticalLines.size() - 1) , horizontalLines.get(0),
                verticalLines.get(verticalLines.size() - 1) , horizontalLines.get(horizontalLines.size() - 1));
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
    public void redraw(GameBoard b){
        gameBoard = b;
        boardDrawer(graph,animationGraph);

        System.out.println("IT HAPPENED");
    }
    static void setTurn(boolean val){
        turn = val;
    }

}