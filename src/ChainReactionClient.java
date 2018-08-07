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
    private Ball[][] board;
    private Ball[][] newBoard;
    private static int width = 625;
    private static int height = 700;
    private static int down;
    private static int length = (int) (width * .06);
    private static Frame frame;
    private volatile static DrawingLoop loop;
    private static ArrayList<Integer> horizontalLines = new ArrayList<>();
    private static ArrayList<Integer> verticalLines = new ArrayList<>();
    private static JRadioButton classicRadio, normalRadio, HDRadio;
    private static int rowNumber;
    private static int columnNumber;
    private static final Object monitor = new Object();
    private static String handshake;
    private static boolean isBroken = false;
    private static LinkedBlockingQueue<BallEvent> ballQueue = new LinkedBlockingQueue<>();

    private ChainReactionClient(String hubHostName) throws IOException, ButtonError {
        super(hubHostName, PORT);
        if (isBroken) {
            disconnect();
            throw new ButtonError("No Size Selected");
        }
        loop = new DrawingLoop();
    }

    public static void main(String[] args) {
        selector();
        try {
            synchronized (monitor) {
                monitor.wait();
            }
            new ChainReactionClient(args[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void startUp(double rowSpacing, double columnSpacing) {
        for (int x = 0; x < rowNumber; x++) {
            verticalLines.add(length);
            length = length + (int) (width * rowSpacing);
        }
        for (int x = 0; x < columnNumber; x++) {
            horizontalLines.add(down);
            down = down + (int) (width * columnSpacing);
        }
        verticalLines.add(length);
    }

    private static void selector() {
        Frame frame = new Frame();
        JPanel content = new JPanel();
        frame.setContentPane(content);
        content.setLayout(null);
        ButtonGroup selectorGroup = new ButtonGroup();
        classicRadio = new JRadioButton("Classic(5x8)");
        normalRadio = new JRadioButton("Normal(11x11)");
        HDRadio = new JRadioButton("HD Grid(15x10)");
        selectorGroup.add(classicRadio);
        selectorGroup.add(normalRadio);
        selectorGroup.add(HDRadio);
        content.add(classicRadio);
        content.add(normalRadio);
        content.add(HDRadio);
        classicRadio.setBounds(0, 0, 100, 25);
        normalRadio.setBounds(100, 0, 100, 25);
        HDRadio.setBounds(200, 0, 100, 25);
        content.setPreferredSize(new Dimension(300, 50));
        content.setBackground(Color.BLACK);
        JButton button = new JButton("OK");
        content.add(button);
        ButtonHandler b = new ButtonHandler();
        button.addActionListener(b);
        button.setBounds(100, 25, 100, 25);
        frame.pack();
    }

    private static void setValues() {
        if (classicRadio.isSelected()) {
            handshake = "classic";
        } else if (normalRadio.isSelected()) {
            handshake = "normal";
            rowNumber = 11;
            columnNumber = 11;
            down = 65;
            startUp(.08, .08);
        } else if (HDRadio.isSelected()) {
            handshake = "HD";
            rowNumber = 10;
            columnNumber = 15;
            down = 35;
            startUp(.09, .07);
        } else {
            isBroken = true;
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
            board = list.get(0);
            newBoard = list.get(1);
            ballQueue = newBoard[0][0].getBallQueue();
            createWindow();
            loop.redraw();
        }

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

    private class DrawingLoop extends JPanel {
        int[] xblock = new int[25];
        int[] yblock = new int[25];
        int temp = 2;
        Timer timer;

        private DrawingLoop() {
            setLayout(new BorderLayout(3, 3));
            setPreferredSize(new Dimension(625, 700));
            timer = new Timer(50, new RepaintAction());
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
            Color ballColor =  ballQueue.peek().getBallColor();
            if (ballQueue.peek() != null && ballQueue.size() != 0) {
                while (ballQueue.size() !=0 ){
                    animateLoop(x, y, ballColor, g2);
                }
            }
            if(ballQueue.size() == 0){
                try{
                    if(!(Arrays.deepEquals(board, newBoard))){
                        throw new Exception("Something went wrong with the animations");
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        private void animateLoop(int x, int y, Color ballColor, Graphics2D g2){
            GradientPaint gradientPaint;
                if (board[x][y].getMaxValue() == 2) {
                    if ((x == 0 && y == 0)) {
                        gradientPaint = gradientPaint((float) xblock[0], (float) yblock[0], ballColor, (float) xblock[0] + 40, (float) yblock[0] + 40);
                        g2.setPaint(gradientPaint);
                        g2.fill(new Ellipse2D.Double(xblock[0] + temp, yblock[0], 40, 40));

                        gradientPaint = gradientPaint((float) xblock[1], (float) yblock[1], ballColor, (float) xblock[1] + 40, (float) yblock[1] + 40);
                        g2.setPaint(gradientPaint);
                        g2.fill(new Ellipse2D.Double(xblock[1], yblock[1] + temp, 40, 40));

                        xblock[0] = xblock[0] + temp;
                        yblock[1] = yblock[1] + temp;

                    } else if (x == 0 && y == (board[0].length - 1)) {
                        gradientPaint = gradientPaint((float) xblock[2], (float) yblock[2], ballColor, (float) xblock[2] + 40, (float) yblock[2] + 40);
                        g2.setPaint(gradientPaint);
                        g2.fill(new Ellipse2D.Double(xblock[2] + temp, yblock[2], 40, 40));

                        gradientPaint = gradientPaint((float) xblock[3], (float) yblock[3], ballColor, (float) xblock[3] + 40, (float) yblock[3] + 40);
                        g2.setPaint(gradientPaint);
                        g2.fill(new Ellipse2D.Double(xblock[3], yblock[3] - temp, 40, 40));

                        xblock[2] = xblock[2] + temp;
                        yblock[3] = yblock[3] - temp;

                    } else if (x == (board.length - 1) && y == 0) {
                        gradientPaint = gradientPaint((float) xblock[4], (float) yblock[4], ballColor, (float) xblock[4] + 40, (float) yblock[4] + 40);
                        g2.setPaint(gradientPaint);
                        g2.fill(new Ellipse2D.Double(xblock[4] - temp, yblock[4], 40, 40));

                        gradientPaint = gradientPaint((float) xblock[5], (float) yblock[5], ballColor, (float) xblock[5] + 40, (float) yblock[5] + 40);
                        g2.setPaint(gradientPaint);
                        g2.fill(new Ellipse2D.Double(xblock[5], yblock[5] + temp, 40, 40));

                        xblock[4] = xblock[4] - temp;
                        yblock[5] = yblock[5] + temp;

                    } else {
                        gradientPaint = gradientPaint((float) xblock[6], (float) yblock[6], ballColor, (float) xblock[6] + 40, (float) yblock[6] + 40);
                        g2.setPaint(gradientPaint);
                        g2.fill(new Ellipse2D.Double(xblock[6] - temp, yblock[6], 40, 40));

                        gradientPaint = gradientPaint((float) xblock[7], (float) yblock[7], ballColor, (float) xblock[7] + 40, (float) yblock[7] + 40);
                        g2.setPaint(gradientPaint);
                        g2.fill(new Ellipse2D.Double(xblock[7], yblock[7] - temp, 40, 40));

                        xblock[6] = xblock[6] - temp;
                        yblock[7] = yblock[7] - temp;

                    }
                } else if (board[x][y].getMaxValue() == 3) {
                    if (x > 0 && y == 0) {
                        gradientPaint = gradientPaint((float) xblock[8], (float) yblock[8], ballColor, (float) xblock[8] + 40, (float) yblock[8] + 40);
                        g2.setPaint(gradientPaint);
                        g2.fill(new Ellipse2D.Double(xblock[8] - temp, yblock[8], 40, 40));

                        gradientPaint = gradientPaint((float) xblock[9], (float) yblock[9], ballColor, (float) xblock[9] + 40, (float) yblock[9] + 40);
                        g2.setPaint(gradientPaint);
                        g2.fill(new Ellipse2D.Double(xblock[9], yblock[9] + temp, 40, 40));

                        gradientPaint = gradientPaint((float) xblock[10], (float) yblock[10], ballColor, (float) xblock[10] + 40, (float) yblock[10] + 40);
                        g2.setPaint(gradientPaint);
                        g2.fill(new Ellipse2D.Double(xblock[10] + temp, yblock[10], 40, 40));


                        xblock[8] = xblock[8] - temp;
                        yblock[9] = yblock[9] + temp;
                        xblock[10] = xblock[10] + temp;

                    } else if (x > 0 && y == board[0].length - 1) {
                        gradientPaint = gradientPaint((float) xblock[11], (float) yblock[11], ballColor, (float) xblock[11] + 40, (float) yblock[11] + 40);
                        g2.setPaint(gradientPaint);
                        g2.fill(new Ellipse2D.Double(xblock[11], yblock[11] - temp, 40, 40));

                        gradientPaint = gradientPaint((float) xblock[12], (float) yblock[12], ballColor, (float) xblock[12] + 40, (float) yblock[12] + 40);
                        g2.setPaint(gradientPaint);
                        g2.fill(new Ellipse2D.Double(xblock[12] - temp, yblock[12], 40, 40));

                        gradientPaint = gradientPaint((float) xblock[13], (float) yblock[13], ballColor, (float) xblock[13] + 40, (float) yblock[13] + 40);
                        g2.setPaint(gradientPaint);
                        g2.fill(new Ellipse2D.Double(xblock[13], yblock[13] + temp, 40, 40));


                        yblock[11] = yblock[11] - temp;
                        xblock[12] = xblock[12] - temp;
                        yblock[13] = yblock[13] + temp;


                    } else if (x == 0 && y > 0) {
                        gradientPaint = gradientPaint((float) xblock[14], (float) yblock[14], ballColor, (float) xblock[14] + 40, (float) yblock[14] + 40);
                        g2.setPaint(gradientPaint);
                        g2.fill(new Ellipse2D.Double(xblock[14], yblock[14] - temp, 40, 40));

                        gradientPaint = gradientPaint((float) xblock[15], (float) yblock[15], ballColor, (float) xblock[15] + 40, (float) yblock[15] + 40);
                        g2.setPaint(gradientPaint);
                        g2.fill(new Ellipse2D.Double(xblock[15] + temp, yblock[15], 40, 40));

                        gradientPaint = gradientPaint((float) xblock[16], (float) yblock[16], ballColor, (float) xblock[16] + 40, (float) yblock[16] + 40);
                        g2.setPaint(gradientPaint);
                        g2.fill(new Ellipse2D.Double(xblock[16], yblock[16] + temp, 40, 40));

                        yblock[14] = yblock[14] - temp;
                        xblock[15] = xblock[15] + temp;
                        yblock[16] = yblock[16] + temp;

                    } else if (x == board.length - 1 && y > 0) {
                        gradientPaint = gradientPaint((float) xblock[17], (float) yblock[17], ballColor, (float) xblock[17] + 40, (float) yblock[17] + 40);
                        g2.setPaint(gradientPaint);
                        g2.fill(new Ellipse2D.Double(xblock[17] - temp, yblock[17], 40, 40));

                        gradientPaint = gradientPaint((float) xblock[18], (float) yblock[18], ballColor, (float) xblock[18] + 40, (float) yblock[18] + 40);
                        g2.setPaint(gradientPaint);
                        g2.fill(new Ellipse2D.Double(xblock[18], yblock[18] - temp, 40, 40));

                        gradientPaint = gradientPaint((float) xblock[19], (float) yblock[19], ballColor, (float) xblock[19] + 40, (float) yblock[19] + 40);
                        g2.setPaint(gradientPaint);
                        g2.fill(new Ellipse2D.Double(xblock[19], yblock[19] + temp, 40, 40));

                        xblock[17] = xblock[17] - temp;
                        xblock[18] = xblock[18] - temp;
                        yblock[19] = yblock[19] + temp;

                    }
                } else if (board[x][y].getMaxValue() == 4) {

                    gradientPaint = gradientPaint((float) xblock[20], (float) yblock[20], ballColor, (float) xblock[20] + 40, (float) yblock[20] + 40);
                    g2.setPaint(gradientPaint);
                    g2.fill(new Ellipse2D.Double(xblock[20] - temp, yblock[20], 40, 40));

                    gradientPaint = gradientPaint((float) xblock[21], (float) yblock[21], ballColor, (float) xblock[21] + 40, (float) yblock[21] + 40);
                    g2.setPaint(gradientPaint);
                    g2.fill(new Ellipse2D.Double(xblock[21] + temp, yblock[21], 40, 40));

                    gradientPaint = gradientPaint((float) xblock[22], (float) yblock[22], ballColor, (float) xblock[22] + 40, (float) yblock[22] + 40);
                    g2.setPaint(gradientPaint);
                    g2.fill(new Ellipse2D.Double(xblock[22], yblock[22] - temp, 40, 40));

                    gradientPaint = gradientPaint((float) xblock[23], (float) yblock[23], ballColor, (float) xblock[23] + 40, (float) yblock[23] + 40);
                    g2.setPaint(gradientPaint);
                    g2.fill(new Ellipse2D.Double(xblock[23], yblock[23] + temp, 40, 40));

                    xblock[20] = xblock[20] - temp;
                    xblock[21] = xblock[21] + temp;
                    yblock[22] = yblock[22] - temp;
                    yblock[23] = yblock[23] + temp;
                }
                temp++;
                for (int a = 0; a < 24; a++) {
                    if (Arrays.equals(board,newBoard)) {
                        ballQueue.poll();
                        temp = 0;
                        timer.stop();
                        break;
                    }
                }
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

    private static class Frame extends JFrame {
        Frame(String name) {
            this.setName(name);
            this.setLocation(150, 0);
            this.setVisible(true);
            this.setResizable(false);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        Frame() {
            this.setLocation(150, 0);
            this.setVisible(true);
            this.setResizable(false);
            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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

    private class ButtonError extends Exception {
        ButtonError(String message) {
            super(message);
        }
    }

    private static class ButtonHandler implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            synchronized (monitor) {
                monitor.notify();
            }
            setValues();
        }
    }
    private class RepaintAction implements ActionListener{
        public void actionPerformed(ActionEvent evt){        }
    }
}