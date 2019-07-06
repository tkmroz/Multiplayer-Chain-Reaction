import javafx.scene.paint.Color;
import netgame.common.Hub;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;


public class ChainReactionServer extends Hub {
    private final static int PORT = 37829;
    private static ChainReactionServer server;
    private int playerCount = 0;
    private HashMap<Integer, Integer> IDHashMap = new HashMap<>();
    private Integer currentPlayer = 0;
    private GameBoard gameBoard;
    private Ball[][] board;
    private LinkedBlockingQueue<ExplodeEvent> explodeQueue;
    private static String handshake;
    private boolean allPlayed = false;
    private PlayerList playerList = new PlayerList();


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
        if (message instanceof GameBoard) {
            gameBoard = ((GameBoard) message);
            board = gameBoard.getBoard();
            gameBoard.setOldBoard(board);
            ballPlacer();
            if (currentPlayer == playerCount - 1) {
                if(!(allPlayed)){
                    allPlayed = true;
                }
                currentPlayer = 0;
            } else {
                currentPlayer++;
            }
            gameBoard.setBoardColor( ColorUtil.fxToAwt(playerList.get().getPlayerColor()));
            gameLoop();
        }
    }

    private void gameLoop() {
        sendToAll(gameBoard);
        sendToOne(playerList.getList().get(currentPlayer).getPlayerID(), "Your Turn");
    }


    protected void playerConnected(int playerID) {
        try {
            super.extraHandshake(playerID, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Player player = new Player(playerCount,playerID);
        IDHashMap.put(playerCount, playerID);
        playerList.add(player);
        playerCount++;

        if (getPlayerList().length == 1) {
            server.shutdownServerSocket();
            startNewGame();
            sendToAll(gameBoard);
            sendToOne(IDHashMap.get(0), "Your Turn");

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
                if ((x == 0 && y == 0) || (x == 0 && y == (board[0].length - 1)) || (x == (board.length - 1) && y == 0)
                        || (x == (board.length - 1) && y == (board[0].length - 1))) {
                    board[x][y] = new Ball(2);

                } else if ((x != 0 && x != (board.length - 1) && y == 0) || (x != 0 && x != (board.length - 1) && y == (board[0].length - 1))
                        || (x == 0 && y != (board[0].length - 1)) || (x == (board.length - 1) && y != 0 && y != (board[0].length - 1))) {
                    board[x][y] = new Ball(3);
                } else {
                    board[x][y] = new Ball(4);
                }
                board[x][y].setValue(1);
                board[x][y].setBallColor(java.awt.Color.RED);
            }
        }
        gameBoard = new GameBoard(board,ColorUtil.fxToAwt(Color.RED));
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
        gameBoard.setExplodeQueue(explodeQueue);
        while (explodeQueue.size() != 0) {
            explode(explodeQueue.peek().getX(), explodeQueue.peek().getY());
            explodeQueue.poll();
        }
        if(isGameOver()){
            endGame();
        }
        gameBoard.setBoard(board);
    }

    /**
     * Tests if a square is about to explode
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
        ArrayList<Integer> scheduledForRemoval = new ArrayList<>();
        for(int x = 0; x < playerList.getList().size(); x++){
            int count = 0;
            for(int a = 0; a < gameBoard.getBoard().length; a++){
                for(int b =0; b <gameBoard.getBoard()[0].length; b++){
                    if(gameBoard.getBoard()[a][b].getBallColor() != null &&
                            gameBoard.getBoard()[a][b].getBallColor().equals(ColorUtil.fxToAwt( playerList.getList().get(x).getPlayerColor()))){
                        count++;
                    }
                }
            }
            if(count == 0){
                scheduledForRemoval.add(x);
                //playerList.getList().remove(p);
            }
        }
        for(Integer x:scheduledForRemoval){
            playerList.getList().remove(x);
        }
        return playerList.getList().size() <= 1;
    }
    private class PlayerList{
        ArrayList<Player> playerList;
        int currentValue = 0;
        private PlayerList(){
            playerList = new ArrayList<>();
        }
        public Player peekNext(){
            if(currentValue + 1 != playerList.size()-1){
                return playerList.get(currentValue + 1);
            }
            else{
                return playerList.get(0);
            }
        }
        public Player next(){
            if(currentValue + 1 != playerList.size()-1){
                currentValue++;
                return playerList.get(currentValue);
            }
            else{
                currentValue = 0;
                return playerList.get(0);
            }
        }
        void add(Player p){
            playerList.add(p);
        }
        Player get(){
            return playerList.get(currentValue);
        }
        ArrayList<Player> getList(){ return playerList; }
    }
    private void endGame(){
        sendToAll("YOU WON!");
    }
}
