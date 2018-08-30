
import netgame.common.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collector;

public class ChainReactionServer extends Hub {
    private final static int PORT = 37829;
    private static ChainReactionServer server;
    private int playerCount = 0;
    private ArrayList <Player> playerList = new ArrayList<>();
    private Integer currentPlayer = 1;
    private Ball[][] board;
    private LinkedBlockingQueue<ExplodeEvent> explodeQueue;
    private static String handshake;
    private LinkedBlockingQueue<BallEvent> ballQueue  = new LinkedBlockingQueue<>();
    private boolean everyoneGone = false;

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
            ballPlacer();
            if (currentPlayer == playerCount) {
                currentPlayer = 1;
                everyoneGone = true;
            } else {
                currentPlayer++;
            }
            board[0][0].setBoardColor(playerList.get(playerCount).getPlayerColor());
            gameLoop();
        }
    }

    private void gameLoop() {
        sendToAll(board);
        sendToOne(currentPlayer, currentPlayer.toString());
        ballQueue.clear();
    }


    protected void playerConnected(int playerID) {
        try {
            super.extraHandshake(playerID, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Player player = new Player( playerCount, playerID);
        playerList.add(player);
        playerCount++;

        if (getPlayerList().length == 2) {
            server.shutdownServerSocket();
            startNewGame();
            sendToAll(board);
            sendToOne(playerList.get(currentPlayer).getPlayerID(), currentPlayer.toString());

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
        else gameLoop();
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
        ballQueue.add(new BallEvent(a, b, board[a][b].getMaxValue(),playerList.get(playerCount).getPlayerColor()));
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
        if(!everyoneGone){return false;}
        //Go through the board and look for all instances of a players balls
        //if 0 remove from playing
        ArrayList<Integer> gameEnd = new ArrayList<>();
        for(int x = 0;  x < playerCount; x++){
            gameEnd.add(0);
        }
        ArrayList<Color> colors = playerList.get(0).getColors();
        for (int a = 0; a < gameEnd.size(); a++){
            for (int x = 0; x < board.length; x++){
                for (int y = 0 ; y < board[0].length; y++){
                    if(board[x][y].getBallColor() != null){
                        if (board[x][y].getBallColor().equals(colors.get(x))){
                            gameEnd.set(a, gameEnd.get(a) + 1);
                        }
                    }
                }
            }
        }
        for (int a  = 0; a < gameEnd.size(); a++){
            if(gameEnd.get(a) == 0){
                playerList.remove(a);
                playerCount--;
            }
        }
        if(gameEnd.size() == 1){
            return true;
        }
        return false;
    }
}