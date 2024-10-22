/*
 * Student ID: 1394392
 * Name: Renfei Yu (Klaus)
 */

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Player extends UnicastRemoteObject{
    private String playerName;
    private char symbol;
    private ClientInterface connectedClient;
    //private Client connectedClient;
    private int point = 0;
    private int ranking;
    private MatchStatus matchingStatus;

    public Player(String name, ClientInterface client) throws RemoteException{
        //ClientInterface client
        this.playerName = name;
        this.connectedClient = client;
        this.matchingStatus=new MatchStatus(playerName);


    }

    public void setClient(ClientInterface targetClient){
        this.connectedClient = targetClient;
    }

    public String getUsername(){
        return this.playerName;
    }

    public void setSymbol(char temp){
        this.symbol = temp;
    }

    public char getSymbol() {
        return this.symbol;
    }

    public void setPoint(int po){
        this.point = po;
    }

    public void setRanking (int ra){
        this.ranking = ra;
    }

    public int getPoint(){
        return this.point;
    }

    public int getRanking(){
        return this.ranking;
    }

    public String geStatus(){
        return this.matchingStatus.getStatus();
    }

    public void setStatus(String temp){
        this.matchingStatus.setStatus(temp);
    }

    public ClientInterface getClient() {
        return this.connectedClient;
    }

    public void winAction() {
        setPoint(point+5);

        try {
            getClient().announceWinning("You win, winner is: " + playerName + " (" + symbol+") ");
        } catch (RemoteException e) {
            
            e.printStackTrace();
        }
    }

    public void loseAction(String winnerName, String winnerSymbol) {
        setPoint(point-5);

        try {
            getClient().announceWinning("You lose, winner is: " + winnerName + " (" + winnerSymbol+") ");
        } catch (RemoteException e) {
            
            e.printStackTrace();
        }
    }

    public void enemyQuitAction(){
        setPoint(point+5);
        try {
            getClient().announceWinning("Your enemy quit, you win, winner is: " + playerName + " (" + symbol+") ");
        } catch (RemoteException e) {
            
            e.printStackTrace();
        }
    }


    public String playerFormat(){
        return "Rank#" + this.ranking+" " + this.playerName + "(" + this.symbol +")";
    }

    public void drawAction() {
        setPoint(point+2);

        try {
            getClient().announceWinning("game is draw");
        } catch (RemoteException e) {
            
            e.printStackTrace();
        }
    }

    public void simpleAnnouncement(String announce){
        try {
            getClient().announceTurn(announce);
        } catch (RemoteException e) {
            
            e.printStackTrace();
        }
    }
}
