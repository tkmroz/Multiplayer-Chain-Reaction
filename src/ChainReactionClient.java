import netgame.common.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

//End goal is to have 5x8, 10x11 and HD all in one game, which only depends on which prefs are selected

/**
 * @author Tomasz Mroz
 * @version 0.1
 * Client program for Multiplayer Chain Reaction
 * Needs a valid server ip as an command line argument
 * Allows a user to choose a preferred game size, which needs to be the same as the server
 */
public class ChainReactionClient extends Client {
    private final static int PORT = 37829;
    static final Object monitor = new Object();
    private static int width = 625;
    private static int height = 700;
    private static Frame frame;
    private volatile static DrawingLoop loop;
    private static ArrayList<Integer> horizontalLines = new ArrayList<>();
    private static ArrayList<Integer> verticalLines = new ArrayList<>();
    private static int rowNumber;
    private static int columnNumber;
    private static String handshake;
    private static boolean isBroken = false;
    private static LinkedBlockingQueue<BallEvent> ballQueue = new LinkedBlockingQueue<>();
    private MovingBall[][] board;
    private Ball[][] newBoard;
    static StartupScreen startupScreen;

    private ChainReactionClient(String hubHostName) throws Exception {
        super(hubHostName, PORT);
        horizontalLines = startupScreen.getHorizontalLines();
        verticalLines = startupScreen.getVerticalLines();
        if (isBroken) {
            disconnect();
            throw new Exception("No Size Selected");
        }
        loop = new DrawingLoop();
    }

    public static void main(String[] args) {
        startupScreen = new StartupScreen();
        try {
            synchronized (monitor) {
                monitor.wait();
            }
            new ChainReactionClient(args[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    protected void messageReceived(Object message) {
        ArrayList<Ball[][]> list = new ArrayList<>();
        if(message.getClass() == list.getClass()) {
            if (frame != null) {
                frame.dispose();
            }
            list = (ArrayList<Ball[][]>) message;
            board = (MovingBall[][]) list.get(0);
            for (int a = 0; a < board.length; a++){
                for (int b = 0; b < board[0].length; b++){
                    board[a][b].setxLocation(horizontalLines.get(a));
                    board[a][b].setyLocation(verticalLines.get(b));
                }
            }
        }
            newBoard = list.get(1);
            ballQueue = newBoard[0][0].getBallQueue();
            createWindow();
            loop.redraw();


        if (message instanceof String) {
            loop.mousing();
        }

    }

    private void createWindow() {
        frame = new Frame("Chain Reaction");
        JPanel content = new JPanel();
        frame.setContentPane(content);
        DrawingLoop displayPanel = new DrawingLoop();
        content.setLayout(new BorderLayout());
        content.add(displayPanel, BorderLayout.CENTER);
        content.setBackground(Color.BLACK);
        JLabel message = new JLabel("CHAIN REACTION", JLabel.CENTER);
        message.setForeground(Color.WHITE);
        message.setFont(new Font("Monospaced", Font.BOLD, 36));
        message.setOpaque(false);
        content.add(message, BorderLayout.NORTH);
        displayPanel.setBackground(Color.BLACK);
        displayPanel.setForeground(Color.WHITE);
        content.setPreferredSize(new Dimension(width, height));
        displayPanel.setPreferredSize(new Dimension(625, 700));
        frame.pack();
        MouseLoop mouseLoop = new MouseLoop();
        displayPanel.addMouseListener(mouseLoop);
    }

    private boolean isMoveLegal(int x, int y, Color color) {
        //use less specific values
        if ((x <= verticalLines.get(0)) || (x >= verticalLines.get(verticalLines.size() - 1)) || (y <= horizontalLines.get(0)) || (y >= horizontalLines.get(horizontalLines.size() - 1))) {
            return false;
        } else if ((color == null)) {
            return true;
        } else {
            return true;
        }
    }

    private void ballPlacer(int x, int y, Color color) {
        boolean moveLegal = isMoveLegal(x, y, color);
        int xBallPosition = 0, yBallPosition = 0;
        if (!moveLegal) {
            return;
        }
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
        if ((y >= (horizontalLines.size() - 1)) && y <= horizontalLines.size()) {
            yBallPosition = rowNumber;
        }
        if ((x >= (verticalLines.size() - 1)) && x <= verticalLines.size()) {
            xBallPosition = columnNumber;
        }
        //Returns if another players ball occupies this space
        if (!(color.equals(board[xBallPosition][yBallPosition].getBallColor()) || (board[xBallPosition][yBallPosition].getBallColor() == null))) {
            return;
        }
        board[xBallPosition][yBallPosition].setValue((board[xBallPosition][yBallPosition].getValue()) + 1);
        board[xBallPosition][yBallPosition].setBallColor(color);
        send(board);
    }

    @Override
    protected void extraHandshake(ObjectInputStream in, ObjectOutputStream out) throws IOException {
        out.writeObject(handshake);
        out.flush();
    }
    private class DrawingLoop extends JPanel {
        int[] xblock = new int[25];
        int[] yblock = new int[25];
        int temp = 2;
        GradientPaint gradientPaint;

        private DrawingLoop() {
            setLayout(new BorderLayout(3, 3));
            setPreferredSize(new Dimension(625, 700));
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(board[0][0].getBoardColor());
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //vertical lines
            for (int x = 0; x < rowNumber; x++) {
                g.drawLine(verticalLines.get(x), horizontalLines.get(0), verticalLines.get(x), horizontalLines.get(horizontalLines.size() - 1));
            }
            //horizontal lines
            for (int x = 0; x < columnNumber; x++) {
                g.drawLine(verticalLines.get(0), horizontalLines.get(x), verticalLines.get(verticalLines.size() - 1), horizontalLines.get(x));
            }
            g.drawLine(verticalLines.get(verticalLines.size() - 1), horizontalLines.get(0), verticalLines.get(verticalLines.size() - 1), horizontalLines.get(horizontalLines.size() - 1));
            for (int foo = 0; foo < 25; foo++) {
                xblock[foo] = verticalLines.get(ballQueue.peek().getStartX());
                yblock[foo] = horizontalLines.get(ballQueue.peek().getStartY());
            }
            int x = ballQueue.peek().getStartX();
            int y = ballQueue.peek().getStartY();
            if (ballQueue.peek() != null && ballQueue.size() != 0) {
                ArrayList<Ball> ballList = new ArrayList<>(ballQueue.peek().getBallValue());
                for(int a = 0; a < ballList.size(); a++) {
                    ballList.set(a,new Ball(ballQueue.peek().getType()));
                }
                while (ballQueue.size() !=0 ){
                    animateLoop(x, y, g2, ballList);
                }
                try{
                    if(!(Arrays.deepEquals(board, newBoard))){
                        throw new Exception("Something went wrong with the animations");
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                finally {
                    for (int a = 0; a < board.length; a++){
                        for (int b = 0; b < board[0].length; b++){
                            board[a][b].setxLocation(horizontalLines.get(a));
                            board[a][b].setyLocation(verticalLines.get(b));
                        }
                    }
                }
            }
            else {
                for (x = 0; x < board.length; x++) {
                    for (y = 0; y < board[0].length; y++) {
                        if (board[x][y].getValue() != 0) {
                            gradientPaint = gradientPaint(verticalLines.get(x) + 5, (horizontalLines.get(y)) + 5, board[x][y].getBallColor(), (float) (verticalLines.get(x)) + 45, (float) horizontalLines.get(y) + 45);
                            g2.setPaint(gradientPaint);
                            g2.fill(new Ellipse2D.Double(verticalLines.get(x) + 5, (horizontalLines.get(y)) + 5, 40, 40));
                            g2.drawString(String.valueOf(board[x][y].getValue()), ((verticalLines.get(x)) + 5), (horizontalLines.get(y)) + 5);

                        }

                    }
                }
            }
        }

        private void animateLoop(int x, int y, Graphics2D g2, ArrayList<Ball> ballList){
                if (board[x][y].getMaxValue() == 2) {
                    if ((x == 0 && y == 0)) {
                        ballList.set(0, new Ball(ballList.get(0).getValue() + 1));
                        ballList.set(1, new Ball(ballList.get(1).getValue() + 1));
                    }
                    else if (x == 0 && y == (board[0].length - 1)) {
                    }
                    else if (x == (board.length - 1) && y == 0) {
                    }
                    else {
                    }
                } else if (board[x][y].getMaxValue() == 3) {
                    if (x > 0 && y == 0) {
                    }
                    else if (x > 0 && y == board[0].length - 1) {
                    }
                    else if (x == 0 && y > 0) {
                    }
                    else if (x == board.length - 1 && y > 0) {
                    }
                } else if (board[x][y].getMaxValue() == 4) {
                }
                temp++;
                /*for (int a = 0; a < 24; a++) {
                    if () {
                        ballQueue.poll();
                        temp = 0;
                        break;
                    }
                }*/
                for (x = 0; x < board.length; x++) {
                    for (y = 0; y < board[0].length; y++) {
                        if (board[x][y].getValue() != 0) {
                            int xLoc = board[x][y].getxLocation();
                            int yLoc = board[x][y].getyLocation();
                            gradientPaint = gradientPaint(xLoc + 5, yLoc + 5, board[x][y].getBallColor(), xLoc + 45, yLoc + 45);
                            g2.setPaint(gradientPaint);
                            g2.fill(new Ellipse2D.Double(xLoc + 5, yLoc + 5, 40, 40));
                            g2.drawString(String.valueOf(board[x][y].getValue()), xLoc + 5, yLoc + 5);
                        }

                    }
                }

        }
        private void redraw() {
            repaint();
        }

        private void mousing() {
            new MouseLoop();
        }
        private GradientPaint gradientPaint(float x1, float y1, Color c, float x2, float y2) {
            return new GradientPaint(x1, y1, c, x2, y2, Color.BLACK);
        }

    }

    private class MouseLoop implements MouseListener {
        MouseLoop() {
        }
        public void mousePressed(MouseEvent e) {
            ballPlacer(e.getX(), e.getY(), board[0][0].getBoardColor());
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseClicked(MouseEvent e) {
        }
    }

    public static void setHandshake(String handshake) {
        ChainReactionClient.handshake = handshake;
    }

    public static void setColumnNumber(int columnNumber) {
        ChainReactionClient.columnNumber = columnNumber;
    }

    public static void setIsBroken(boolean isBroken) {

        ChainReactionClient.isBroken = isBroken;
    }

    public static void setRowNumber(int rowNumber) {

        ChainReactionClient.rowNumber = rowNumber;
    }
}