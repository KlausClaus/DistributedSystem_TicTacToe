/*
 * Student ID: 1394392
 * Name: Renfei Yu (Klaus)
 */

 
import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JButton;



public class Client extends UnicastRemoteObject implements ClientInterface{
    private boolean myTurn;
    private GameGUI gui;
    private String username;
    private static ServerInterface server;
    private int playingGameId = -1;

    public Client(String username) throws RemoteException {
        this.username = username;
        this.myTurn = false;
        this.playingGameId = -1;

        this.gui = new GameGUI(username, Client.this);
        

        try {
            server.initPlayer(username, this);
            
        } catch (RemoteException e) {
            e.printStackTrace();
        }




    }



    @Override
    public void startOrWait(String decision) throws RemoteException {
        System.out.println(decision);
        if(this.gui != null){
            System.out.println(decision);
            if(decision.equals("waiting")){
                this.gui.resetTime();

                JButton[][] currentButtons = this.gui.geButtons();

                boolean Enabled = false;

                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if(currentButtons[i][j].isEnabled()){
                            Enabled = true;
                        }
                        
                    }
                }
                if(Enabled){
                    this.gui.banButton();
                }
                

            }else if(decision.equals("playing")){
                this.gui.startTime();

                JButton[][] currentButtons = this.gui.geButtons();

                boolean notEnabled = false;

                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if(!currentButtons[i][j].isEnabled()){
                            notEnabled = true;
                        }
                        
                    }
                }
                if(notEnabled){
                    this.gui.restartButton();
                }
                
            }
        }

        
    }

    public String getUserName() {
        return this.username;

    }


    public void quitWaitingQueue(){
         try {
            getGameServer().quitWaiting(username);
        } catch (RemoteException e) {
            
            e.printStackTrace();
        } 
    }

    public int getGameId(){
        return this.playingGameId;
    }

    @Override
    public void setGameId(int gameid) throws RemoteException{
        this.playingGameId= gameid;
    }

    @Override
    public void setmyTurn(boolean temp)throws RemoteException{
        this.myTurn = temp;
        if (this.gui != null) {
            this.gui.startTime();
            if(temp){
                this.gui.restartButton();
            }else{
                this.gui.banButton();
            }
        }
    }

    @Override
    public boolean myTurn() throws RemoteException{
        return myTurn;
    }

    public void boardClick(int i, int j){

        try{
            server.move(playingGameId, username, i,j);
        }catch (RemoteException e){
            System.out.println(e);
        }
        

    }

    // force the client to make random move
    public void forceMove() {
        char[][] board = getGameBoard();
        List<Point> availableMoves = getAvailableMoves(board);

        Point forceMovePoint = getRandomMove(availableMoves);
        try {
            server.move(playingGameId, username, forceMovePoint.x, forceMovePoint.y);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        
    }


    @Override
    public void updateBoard(GameState currGameState) throws RemoteException{

        char[][] board = currGameState.getGameBoard();
        this.gui.updateBoard(board);

    }


    private Point getRandomMove(List<Point> availableMoves) {
        int randomIndex = ThreadLocalRandom.current().nextInt(availableMoves.size());
        return availableMoves.get(randomIndex);
    }
        


    private char[][] getGameBoard() {

        try {
            return server.getGameBoardByid(this.playingGameId);
        } catch (RemoteException e) {
            e.printStackTrace();
            // Return an empty board or handle the exception as needed
            return new char[3][3];
        }
        // return new char[3][3];
    }

    private List<Point> getAvailableMoves(char[][] board) {
        List<Point> availableMoves = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == '\0') {
                    availableMoves.add(new Point(i, j));
                }
            }
        }
        return availableMoves;
    }


    @Override
    public void announceTurn(String string) throws RemoteException{
        if(this.gui != null){
            this.gui.modifTurnyInformation(string);
        }
        
    }

    @Override
    public void announceWinning(String string) throws RemoteException{
        this.gui.modifyWinInformation(string);
    }


    public void askForReRegisterPlayer() {
        try {
            server.initPlayer(username, this);
            
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public ServerInterface getGameServer(){
        return Client.server;
    }


    public void quiGame() {
        try {
            getGameServer().quitGame(getGameId(), username);
        } catch (RemoteException e) {
            
            e.printStackTrace();
        } // Assuming this method is present in the server interface 
    }

   
    public void sendMessage(String text){
        if(getGameId()==-1){

        }else{
            try {
                server.handleMessageSending(getGameId(), username, text);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void getChatHistory(ArrayList<String> chat) throws RemoteException{
        StringBuilder Allchat = new StringBuilder();
        for(String content : chat){
            Allchat.append(content);
            Allchat.append("\n");
        }
        String finalChat = Allchat.toString();
        gui.setChatHistory(finalChat);
    }



    
    public static void main(String[] args){
        if(args.length != 3){
            System.out.println("Wrong format, input java -jar client.jar [username] [server_ip] [server_port]");
            System.exit(1);

        }

        String username = args[0];
        String sever_ip = args[1];
        int sever_port = Integer.parseInt(args[2]);

        try{
            Registry registry = LocateRegistry.getRegistry(sever_ip, sever_port);
            
            server = (ServerInterface) registry.lookup("GameServer");
            new Client(username);

            System.out.println("client is here ");
            
        }catch(Exception e){
            System.out.println("Server is not ready");
        //    e.printStackTrace();
        }

        

        
    }


    

}
