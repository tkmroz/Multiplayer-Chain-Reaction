
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
    private static String handshake;

    private ChainReactionServer() throws IOException {
        super(PORT);
        setAutoreset(true);
    }

    public static void main(String[] args) {
        handshake = args[0];
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
            playerHashMap.get(IDHashMap.get(currentPlayer)).setHasPlayed(true);
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
            super.extraHandshake(playerID, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Player player = new Player( playerCount);
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
        switch (handshake) {
            case "classic":
                board = new Ball[8][5];
                break;
            case "normal":
                board = new Ball[11][11];
                break;
            case "HD":
                board = new Ball[15][10];
                break;
        }
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[0].length; y++) {
                if ((x == 0 && y == 0) || (x == 0 && y == (board[0].length - 1)) || (x == (board.length - 1) && y == 0) || (x == (board.length - 1) && y == (board[0].length - 1))) {
                    board[x][y] = new Ball("Corner");

                } else if ((x != 0 && x != (board.length - 1) && y == 0) || (x != 0 && x != (board.length - 1) && y == (board[0].length - 1)) || (x == 0 && y != (board[0].length - 1)) || (x == (board.length - 1) && y != 0 && y != (board[0].length - 1))) {
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
        for (int a = 0; a < board.length; a++) {
            for (int b = 0; b < board[0].length; b++) {
                if ((board[a][b].getValue() >= board[a][b].getMaxValue())) {
                    explodeQueue.add(new ExplodeEvent(a, b));
                }
            }
        }
        while (explodeQueue.size() != 0) {
            explode(explodeQueue.peek().getX(), explodeQueue.peek().getY());
            explodeQueue.poll();
        }
        if (isGameOver()) {

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
            if (!in.readObject().equals(handshake)) {
                throw new IOException();
            }
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
        }
    }

    private boolean isGameOver() {
        /*HashMap<Player, Integer> playerEnd = new HashMap<>();
        for(int a = 0; a < playerCount; a++){
            playerEnd.put( playerHashMap.get(IDHashMap.get(a)), 0);
        }
        for (int x = 0; x < playerHashMap.get(0).getColors().size() - 1; x++) {
            for (int a = 0; a < board.length; a++) {
                for (int b = 0; b < board[0].length; b++) {
                    if (board[a][b].getBallColor() != null) {
                        if ((board[a][b].getBallColor().equals(new Player( -1).getColors().get(x)))) {
                            int y = playerEnd.get(new Player(x));
                            playerEnd.remove(new Player(x));
                            playerEnd.put( playerHashMap.get(IDHashMap.get(a + 1)), y++);
                        }
                    }
                }
            }
        }
        for (int x = 0; x < playerCount; x++){
            //Player player = playerEnd.get(playerHashMap.get(IDHashMap.get(x)));
            //if(playerEnd)
        }
        if () {
        } else {
            return false;
        }
    */
        return true;
    }
}