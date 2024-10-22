/*
 * Student ID: 1394392
 * Name: Renfei Yu (Klaus)
 */

import java.io.Serializable;
import java.util.*;

public class Game implements Serializable{
    private static final long serialVersionUID = 1L;
    private Player currentPlayer;
    private Player p1;
    private Player p2;
    private HashMap<String, Player> playerDictionary;
    private int gameId;
    private GameState currentGameState;

    public Game(Player p1, Player p2, int gameId){
        this.gameId = gameId;
        currentPlayer = p1;
        this.p1 = p1;
        this.p2 = p2;
        this.currentGameState = new GameState();
        playerDictionary = new HashMap<>();
        playerDictionary.put(p1.getUsername(), p1);
        playerDictionary.put(p2.getUsername(), p2);
    }
    public Player getCurrentPlayer(){
        return this.currentPlayer;
    }

    public Player getPlayerByName(String username){
        if(p1.getUsername().equals(username)){
            System.out.println();
            return p1;
        }else{
            return p2;
        }
    }

    public Player getThePlayerNotCurrent(){
        if(currentPlayer.equals(p1)){
            return p2;
        }else if(currentPlayer.equals(p2)){
            return p1;
        }
        return null;
    }

    public HashMap<String, Player> getplayerDictionary(){
        return this.playerDictionary;
    }

    public void setCurrentPlayer(Player temp){
        this.currentPlayer = temp;
    }

    public int getGameId(){
        return this.gameId;
    }


    public GameState getGameState(){
        return this.currentGameState;
    }

    public boolean setGameState(int x_coordinate, int y_coordinate){
        boolean result = currentGameState.modifyBoard(x_coordinate, y_coordinate, currentPlayer.getSymbol());

        return result;

    }

    public Player getP1(){
        return this.p1;
    }

    public Player getP2(){
        return this.p2;
    }

    public void switchPlayer(){
        if(currentPlayer == p1){
            currentPlayer = p2;
        }else{
            currentPlayer = p1;
        }
    }

    public boolean checkWinning(){
        char[][] gameBoard = currentGameState.getGameBoard();
        char player = currentPlayer.getSymbol();

        // check whether there is a row that conduct winning
        for(int i = 0; i < 3; i++) {
            if(gameBoard[i][0] == player && gameBoard[i][1] == player && gameBoard[i][2] == player) {
                currentGameState.setWinningSituation("winner is: " + currentPlayer.getUsername() + " (" + currentPlayer.getSymbol()+") ");
                return true;
            }
        }

        // check whether there is a column that conduct winning
        for(int i = 0; i < 3; i++) {
            if(gameBoard[0][i] == player && gameBoard[1][i] == player && gameBoard[2][i] == player) {
                currentGameState.setWinningSituation("winner is: " + currentPlayer.getUsername() + " (" + currentPlayer.getSymbol()+") ");
                return true;
            }
        }

        // check whether there is a diagonal that conduct winning
        if(gameBoard[0][0] == player && gameBoard[1][1] == player && gameBoard[2][2] == player) {
            currentGameState.setWinningSituation("winner is: " + currentPlayer.getUsername() + " (" + currentPlayer.getSymbol()+") ");
            return true;
        }

        if(gameBoard[0][2] == player && gameBoard[1][1] == player && gameBoard[2][0] == player) {
            currentGameState.setWinningSituation("winner is: " + currentPlayer.getUsername() + " (" + currentPlayer.getSymbol()+") ");
            return true;
        }

        return false;
    }

    public boolean checkDraw() {
        char[][] gameBoard = currentGameState.getGameBoard();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (gameBoard[i][j] == '\0') {
                    
                    return false;
                }
            }
        }
        
        currentGameState.setWinningSituation("draw");
        return !checkWinning();
    }

    public void setChatMessage(String content){
        currentGameState.addChat(content);
    }
        
    


}
