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

//End goal is to have 5x8, 10x11 and HD all in one game, which only depends on which prefs are selected
public class ChainReactionClient extends Client{
    private final static int PORT = 37829;
    private Ball[][] board;
    private static int width = 625;
    private static int height = 700;
    private static int down = 35;
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

    private ChainReactionClient(String hubHostName) throws IOException, ButtonError {
        super(hubHostName, PORT);
        if(isBroken){
            disconnect();
            throw new ButtonError("No Size Selected");
        }
        loop = new DrawingLoop();
    }

    public static void main(String[] args) {
        selector();
        try {
            synchronized (monitor){
                monitor.wait();
            }
            new ChainReactionClient("10.0.0.50");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private static void startUp(double rowSpacing, double columnSpacing){
        for (int x = 0; x < rowNumber; x++) {
            verticalLines.add(length);
            length = length + (int) (width * rowSpacing);
        }
        for(int x = 0; x < columnNumber; x++){
            horizontalLines.add(down);
            down = down + (int) (width * columnSpacing);
        }
        verticalLines.add(length);
    }
    private static void selector(){
        Frame frame = new Frame("Chain Reaction");
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
        classicRadio.setBounds(0,0,100,25);
        normalRadio.setBounds(100,0,100,25);
        HDRadio.setBounds(200,0,100,25);
        content.setPreferredSize(new Dimension(300, 50));
        content.setBackground(Color.BLACK);
        JButton button = new JButton("OK");
        content.add(button);
        ButtonHandler b = new ButtonHandler();
        button.addActionListener(b);
        button.setBounds(100,25,100,25);
        frame.pack();
    }
    private static void setValues(){
        if(classicRadio.isSelected()){
            handshake = "classic";
        }
        else if(normalRadio.isSelected()){
            handshake = "normal";
            rowNumber = 11;
            columnNumber = 11;
            startUp(.08,.08);
        }
        else if(HDRadio.isSelected()){
            handshake = "HD";
            rowNumber = 10;
            columnNumber = 15;
            startUp(.09, .07);
        }
        else{
            isBroken = true;
        }
    }


    @Override
    protected void messageReceived(Object message) {
        /*if(!isChosen){
            synchronized (monitor){
                try {
                    monitor.wait();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }*/
        if (message instanceof Ball[][]) {
            if (frame != null){ frame.dispose(); }
            board = (Ball[][]) message;
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
            //vertical lines
            for (int x = 0; x < rowNumber; x++) {
                g.drawLine(verticalLines.get(x) , horizontalLines.get(0) , verticalLines.get(x), horizontalLines.get(horizontalLines.size() - 1));
            }
            //horizontal lines
            for (int x = 0; x < columnNumber; x++) {
                g.drawLine(verticalLines.get(0) ,  horizontalLines.get(x) , verticalLines.get(verticalLines.size() - 1) , horizontalLines.get(x) );
            }
            g.drawLine(verticalLines.get(0), down, verticalLines.get(verticalLines.size() - 1), down);
            g.drawLine(verticalLines.get(verticalLines.size() - 1) , horizontalLines.get(0) , verticalLines.get(verticalLines.size() - 1) , horizontalLines.get(horizontalLines.size() - 1));
            for (int x = 0; x < board.length; x++) {
                for (int y = 0; y < board[0].length; y++) {
                    if (board[x][y].getValue() != 0) {
                        GradientPaint gradientPaint = new GradientPaint(verticalLines.get(x) + 5, (horizontalLines.get(y)) + 5, board[x][y].getBallColor(), (float) (verticalLines.get(x)) + 45, (float) horizontalLines.get(y) + 45, Color.BLACK);
                        g2.setPaint(gradientPaint);
                        g2.fill(new Ellipse2D.Double(verticalLines.get(x) + 5, (horizontalLines.get(y)) + 5, 40, 40));
                        g.drawString(String.valueOf(board[x][y].getValue()), ((verticalLines.get(x)) + 5), (horizontalLines.get(y)) + 5);

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
        if ((x <= verticalLines.get(0)) || ( x >= verticalLines.get(verticalLines.size() - 1)) || (y <= horizontalLines.get(0)) || (y >= horizontalLines.get(horizontalLines.size() - 1))){
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
        if ((y >= (horizontalLines.size()-1)) && y <= horizontalLines.size()){
            yBallPosition = rowNumber;
        }
        if ((x >= (verticalLines.size()-1)) && x <= verticalLines.size()){
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
    private class ButtonError extends Exception{
        ButtonError(String message){
            super(message);
        }
    }
    private static class ButtonHandler  implements ActionListener{
        public void actionPerformed(ActionEvent evt){
            synchronized (monitor) {
                monitor.notify();
            }
            setValues();
        }
    }
}