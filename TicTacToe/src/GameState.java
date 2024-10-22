/*
 * Student ID: 1394392
 * Name: Renfei Yu (Klaus)
 */

import java.io.Serializable;
import java.util.*;

public class GameState implements Serializable{
    private static final long serialVersionUID = 1L;
    private char[][] gameBoard;
    private ArrayList<String> chatHistory;
    private String winningSituation = "gaming";

    public GameState(){

        gameBoard = new char[3][3];
        chatHistory = new ArrayList<>();

        // chatHistory.put("helloworld", p1);

    }

    // Modify the board at a given coordinate with a new value
    public boolean modifyBoard(int row, int col, char value) {
        if(this.gameBoard[row][col] == '\0'){
            this.gameBoard[row][col]= value;
            return true;
        }else{
            return false;
        }
         
    }

    // Get the current state of the board
    public char[][] getGameBoard() {
        return this.gameBoard;
    }

    public void addChat(String content){
        // the size of chat history should not beyond 10
        if(chatHistory.size() >= 10){
            chatHistory.remove(0);
        }
        chatHistory.add(content);
    }

    public ArrayList<String> getChatHistory(){
        return this.chatHistory;
    }

    public void setWinningSituation(String situation){
        this.winningSituation = situation;
    }

    public String getWinningSituation(){
        return this.winningSituation;
    }
    
}
