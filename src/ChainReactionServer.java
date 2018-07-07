
import netgame.common.*;

import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;


public class ChainReactionServer extends Hub {
    private final static int PORT = 37829;
    private static ChainReactionServer server;
    private int playerCount = 0;
    private HashMap<Integer, Integer> IDHashMap = new HashMap<>();
    private HashMap<Integer, Player> playerHashMap = new HashMap<>();
    private Integer currentPlayer = 1;
    private Ball[][] board;
    private LinkedBlockingQueue<ExplodeEvent> explodeQueue;

    private ChainReactionServer() throws IOException {
        super(PORT);
        setAutoreset(true);
    }

    public static void main(String[] args) {
        try {
            server = new ChainReactionServer();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void messageReceived(int playerID, Object message) {
        if (message instanceof Ball[][]) {
            board = (Ball[][]) message;
            /*int locationX = evt.getX();
            System.out.println("X: " + locationX);
            int locationY = evt.getY();
            System.out.println("Y: " + locationY);*/
            ballPlacer();
            if (currentPlayer == playerCount) {
                currentPlayer = 1;
            } else {
                currentPlayer++;
            }
            board[0][0].setBoardColor(playerHashMap.get(IDHashMap.get(currentPlayer)).getPlayerColor());
            gameLoop();
        }

    }

    private void gameLoop() {
        sendToAll(board);
        sendToOne(currentPlayer, currentPlayer.toString());
    }


    protected void playerConnected(int playerID) {
        try {
            super.extraHandshake(playerID,null, null);
        }
        catch (Exception e){

        }

        Player player = new Player("TOM", playerCount);
        IDHashMap.put(playerCount + 1, playerID);
        playerHashMap.put(playerID, player);
        playerCount++;

        if (getPlayerList().length == 2) {
            server.shutdownServerSocket();
            startNewGame();
            sendToAll(board);
            sendToOne(IDHashMap.get(currentPlayer), currentPlayer.toString());

        }
    }

    private void startNewGame() {
        board = new Ball[11][11];
        for (int x = 0; x < 11; x++) {
            for (int y = 0; y < 11; y++) {
                if ((x == 0 && y == 0) || (x == 0 && y == (board[0].length - 1)) || (x == (board.length - 1) && y == 0) || (x == (board.length - 1) && y == (board[0].length - 1))) {
                    board[x][y] = new Ball("Corner");

                } else if ((x != 0 && x != (board.length - 1) && y == 0) || (x != 0 && x != (board.length - 1) && y == (board[0].length - 1)) || (x == 0 && y != 0 && y != (board[0].length - 1)) || (x == (board.length - 1) && y != 0 && y != (board[0].length - 1))) {
                    board[x][y] = new Ball("Edge");
                } else {
                    board[x][y] = new Ball("Middle");
                }
            }
        }
        board[0][0].setBoardColor(Color.RED);
    }

    private void ballPlacer() {
        explodeQueue = new LinkedBlockingQueue<>();
        for (int a = 0; a < 11; a++) {
            for (int b = 0; b < 11; b++) {
                if ((board[a][b].getValue() >= board[a][b].getMaxValue())) {
                    explodeQueue.add(new ExplodeEvent(a, b));
                }
            }
        }
        while (explodeQueue.size() != 0) {
            explode(explodeQueue.peek().getX(), explodeQueue.peek().getY());
            explodeQueue.poll();
        }
    }

    /**
     * Tests if a square is about to explsoe
     *
     * @param a a place in the array of the explosion
     * @param b b place in the array of the explosion
     */
    private void doesExplode(int a, int b) {
        if ((board[a][b].getValue() >= board[a][b].getMaxValue())) {
            explodeQueue.add(new ExplodeEvent(a, b));
        }

    }

    /**
     * Does all the math to see if a square will explode
     *
     * @param a a place in the array of the explosion
     * @param b b place in the array of the explosion
     */
    private void explode(int a, int b) {
        board[a][b].setValue(0);
        if (board[a][b].getMaxValue() == 2) {
            if ((a == 0 && b == 0)) {
                board[a + 1][b].setValue(board[a + 1][b].getValue() + 1);
                board[a][b + 1].setValue(board[a][b + 1].getValue() + 1);

                board[a + 1][b].setBallColor(board[a][b].getBallColor());
                board[a][b + 1].setBallColor(board[a][b].getBallColor());

                doesExplode(a + 1, b);
                doesExplode(a, b + 1);

            } else if (a == 0 && b == (board[0].length - 1)) {
                board[a + 1][b].setValue(board[a + 1][b].getValue() + 1);
                board[a][b - 1].setValue(board[a][b - 1].getValue() + 1);

                board[a + 1][b].setBallColor(board[a][b].getBallColor());
                board[a][b - 1].setBallColor(board[a][b].getBallColor());

                doesExplode(a + 1, b);
                doesExplode(a, b - 1);

            } else if (a == (board.length - 1) && b == 0) {
                board[a - 1][b].setValue(board[a - 1][b].getValue() + 1);
                board[a][b + 1].setValue(board[a][b + 1].getValue() + 1);

                board[a][b + 1].setBallColor(board[a][b].getBallColor());
                board[a - 1][b].setBallColor(board[a][b].getBallColor());

                doesExplode(a, b + 1);
                doesExplode(a - 1, b);
            } else {
                board[a - 1][b].setValue(board[a - 1][b].getValue() + 1);
                board[a][b - 1].setValue(board[a][b - 1].getValue() + 1);

                board[a - 1][b].setBallColor(board[a][b].getBallColor());
                board[a][b - 1].setBallColor(board[a][b].getBallColor());

                doesExplode(a - 1, b);
                doesExplode(a, b - 1);
            }
        } else if (board[a][b].getMaxValue() == 3) {
            if (a > 0 && b == 0) {
                board[a - 1][b].setValue(board[a - 1][b].getValue() + 1);
                board[a][b + 1].setValue(board[a][b + 1].getValue() + 1);
                board[a + 1][b].setValue(board[a + 1][b].getValue() + 1);

                board[a + 1][b].setBallColor(board[a][b].getBallColor());
                board[a][b + 1].setBallColor(board[a][b].getBallColor());
                board[a - 1][b].setBallColor(board[a][b].getBallColor());

                doesExplode(a + 1, b);
                doesExplode(a, b + 1);
                doesExplode(a - 1, b);
            } else if (a > 0 && b == board[0].length - 1) {
                board[a - 1][b].setValue(board[a - 1][b].getValue() + 1);
                board[a][b - 1].setValue(board[a][b - 1].getValue() + 1);
                board[a + 1][b].setValue(board[a + 1][b].getValue() + 1);

                board[a + 1][b].setBallColor(board[a][b].getBallColor());
                board[a - 1][b].setBallColor(board[a][b].getBallColor());
                board[a][b - 1].setBallColor(board[a][b].getBallColor());

                doesExplode(a + 1, b);
                doesExplode(a - 1, b);
                doesExplode(a, b - 1);

            } else if (a == 0 && b > 0) {
                board[a][b - 1].setValue(board[a][b - 1].getValue() + 1);
                board[a + 1][b].setValue(board[a + 1][b].getValue() + 1);
                board[a][b + 1].setValue(board[a][b + 1].getValue() + 1);

                board[a][b + 1].setBallColor(board[a][b].getBallColor());
                board[a][b - 1].setBallColor(board[a][b].getBallColor());
                board[a + 1][b].setBallColor(board[a][b].getBallColor());

                doesExplode(a + 1, b);
                doesExplode(a, b + 1);
                doesExplode(a, b - 1);
            } else if (a == board.length - 1 && b > 0) {
                board[a][b - 1].setValue(board[a][b - 1].getValue() + 1);
                board[a - 1][b].setValue(board[a - 1][b].getValue() + 1);
                board[a][b + 1].setValue(board[a][b + 1].getValue() + 1);

                board[a][b + 1].setBallColor(board[a][b].getBallColor());
                board[a - 1][b].setBallColor(board[a][b].getBallColor());
                board[a][b - 1].setBallColor(board[a][b].getBallColor());

                doesExplode(a, b + 1);
                doesExplode(a - 1, b);
                doesExplode(a, b - 1);
            }
        } else if (board[a][b].getMaxValue() == 4) {
            board[a + 1][b].setValue(board[a + 1][b].getValue() + 1);
            board[a - 1][b].setValue(board[a - 1][b].getValue() + 1);
            board[a][b + 1].setValue(board[a][b + 1].getValue() + 1);
            board[a][b - 1].setValue(board[a][b - 1].getValue() + 1);

            board[a + 1][b].setBallColor(board[a][b].getBallColor());
            board[a - 1][b].setBallColor(board[a][b].getBallColor());
            board[a][b + 1].setBallColor(board[a][b].getBallColor());
            board[a][b - 1].setBallColor(board[a][b].getBallColor());

            doesExplode(a + 1, b);
            doesExplode(a, b + 1);
            doesExplode(a - 1, b);
            doesExplode(a, b - 1);
        }

        board[a][b].setBallColor(null);
        /*if (isGameOver()) {
            endGame();
        } */
    }

    /**
     * subclass defining some essential variables
     */
    private class ExplodeEvent {
        private int x;
        private int y;

        private ExplodeEvent(int a, int b) {
            x = a;
            y = b;
        }

        int getX() {
            return x;
        }

        int getY() {
            return y;
        }
    }

    @Override
    protected void extraHandshake(int playerID, ObjectInputStream in, ObjectOutputStream out) throws IOException {
        try {
            if (!(in.readObject().equals("Normal"))){
                throw new IOException();
            }
        }
        catch (ClassNotFoundException c){
            c.printStackTrace();
        }
    }
}