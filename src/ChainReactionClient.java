import netgame.common.Client;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.prefs.*;
//End goal is to have 5x8, 10x11 and HD all in one game, which only depends on which pref is selected
public class ChainReactionClient extends Client {
    private final static int PORT = 37829;
    private Ball[][] board;
    private static int[] bounds = new int[4];
    private static int width = 625;
    private static int height = 700;
    private static Frame frame;
    private DrawingLoop loop;

    private ChainReactionClient(String hubHostName) throws IOException {
        super(hubHostName, PORT);
        loop = new DrawingLoop();
        super.extraHandshake(null, null);
    }

    public static void main(String[] args) {
        try {
            new ChainReactionClient("10.0.0.50");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void messageReceived(Object message) {
        if (message instanceof Ball[][]) {
            if (frame != null){ frame.dispose(); }
            board = (Ball[][]) message;
            createWindow();
            loop = new DrawingLoop();
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
        content.setLayout( new BorderLayout());
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
        private DrawingLoop() {
            setLayout(new BorderLayout(3, 3));
            setPreferredSize(new Dimension(625, 700));

        }
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(board[0][0].getBoardColor());
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int length = (int) (width * .07);
            int a = length;
            int down = 65;
            bounds[0] = length;
            g.drawLine(length - 1, down - 1, length - 1, 614);
            //vertical lines
            for (int x = 0; x < 11; x++) {
                board[0][x].setHorizontalLine(length);
                length = length + (int) (width * .08);
                g.drawLine(length - 1, down - 1, length - 1, 614);
            }
            bounds[1] = length + 2;
            bounds[2] = down;
            //horizontal lines
            for (int x = 0; x < 11; x++) {
                g.drawLine(a - 1, down - 1, length - 1, down - 1);
                board[x][0].setVerticalLine(down);
                down = down + (int) (width * .08);
            }
            g.drawLine(a, down, length, down);

            for (int x = 0; x < board.length; x++) {
                for (int y = 0; y < board[0].length; y++) {
                    if (board[x][y].getValue() != 0) {
                        GradientPaint gradientPaint = new GradientPaint(board[0][x].getHorizontalLine() + 5, (board[y][0].getVerticalLine()) + 5, board[x][y].getBallColor(), (float) (board[0][x].getHorizontalLine()) + 45, (float) board[y][0].getVerticalLine() + 45, Color.BLACK);
                        g2.setPaint(gradientPaint);
                        g2.fill(new Ellipse2D.Double(board[0][x].getHorizontalLine() + 5, (board[y][0].getVerticalLine()) + 5, 40, 40));
                        g.drawString(String.valueOf(board[x][y].getValue()), ((board[0][x].getHorizontalLine()) + 5), (board[y][0].getVerticalLine()) + 5);

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
    }

    private static class Frame extends JFrame {
        Frame(String name) {
            this.setName(name);
            this.setLocation(150, 0);
            this.setVisible(true);
            this.setResizable(false);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
    }

    private class MouseLoop implements MouseListener{
        MouseLoop() {
        }
        public void mousePressed(MouseEvent e) {
           ballPlacer(e.getX(), e.getY(),board[0][0].getBoardColor() );

        }
        public void mouseReleased(MouseEvent e) {}

        public void mouseEntered(MouseEvent e) {}

        public void mouseExited(MouseEvent e) {}

        public void mouseClicked(MouseEvent e) {}
    }

    private boolean isMoveLegal(int x, int y, Color color) {
        //use less specific values
        if ((x <= 43) || (x >= 590) || (y <= 60) || (y >= 613)) {
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
        for (int a = 0; a < board.length - 1; a++) {
            if ((((board[0][a].getHorizontalLine()) <= x)) && ((board[0][a + 1].getHorizontalLine()) >= x)) {
                xBallPosition = a;
            }
        }
        for (int a = 0; a < board[0].length - 1; a++) {
            if ((((board[a][0].getVerticalLine()) <= y)) && (board[a + 1][0].getVerticalLine()) >= y) {
                yBallPosition = a;
            }
        }
        if ((y >= 510) && (y < 560)) {
            yBallPosition = 9;
        }
        if ((y >= 561) && (y <= 612)) {
            yBallPosition = 10;
        }
        if (x >= 542 && x <= 595) {
            xBallPosition = 10;
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
        out.writeObject("Normal");
        out.flush();
    }
}