/*
 * Student ID: 1394392
 * Name: Renfei Yu (Klaus)
 */

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class GameServer extends UnicastRemoteObject implements ServerInterface {
    private ArrayList<Player> players;
    private ArrayList<Player> queuingPlayers;
    private HashMap<Integer, Game> currentGames;

    private Random random = new Random();

    protected GameServer() throws RemoteException {
        super();
        players = new ArrayList<>();
        queuingPlayers = new ArrayList<>();
        currentGames = new HashMap<>();
        
    }

    
    public ArrayList<Player> getAllPlayers(){
        return this.players;
    }

    public ArrayList<Player> getAllQueuingPlayers(){
        return this.queuingPlayers;
    }

    public Player getRandomPlayer() {
    
        int randomIndex = random.nextInt(queuingPlayers.size());
        return queuingPlayers.get(randomIndex);
    }

    public synchronized void assignRandomSymbols(Player tempPlayer, Player enemy) {
        Random random = new Random();
        boolean tempPlayerGetsX = random.nextBoolean();
    
        if (tempPlayerGetsX) {
            tempPlayer.setSymbol('X');
            enemy.setSymbol('O');
        } else {
            tempPlayer.setSymbol('O');
            enemy.setSymbol('X');
        }
    }

    public synchronized Player assignRandomStartTurn(Player tempPlayer, Player enemy) {

        Random random = new Random();
        boolean tempPlayerGetsMove = random.nextBoolean();
    
        if (tempPlayerGetsMove) {
            try {
                tempPlayer.getClient().setmyTurn(true);
                enemy.getClient().setmyTurn(false);
            } catch (RemoteException e) {
                
                e.printStackTrace();
            }

            return tempPlayer;
        } else {
            try {
                tempPlayer.getClient().setmyTurn(false);
                enemy.getClient().setmyTurn(true);
            } catch (RemoteException e) {
                
                e.printStackTrace();
            }

            return enemy;
        }

    }
    
    @Override
    public synchronized void initPlayer(String username, ClientInterface client) throws RemoteException {
        Player tempPlayer = null;
        boolean exist = false;
        // check whether the player exists in players list
        for(int i = 0; i < players.size(); i ++){
            if(players.get(i).getUsername().equals(username)){
                // if exists, reset the player's client 
                tempPlayer = players.get(i);
                players.get(i).setClient(client);


                exist = true;
            }
        }

        //if not exists, then register new player
        if(!exist){
            tempPlayer = new Player(username, client);
            players.add(tempPlayer);
        }

        tempPlayer.getClient().setGameId(-1);

        reRank();
        if(queuingPlayers.isEmpty()){
            tempPlayer.setStatus("waiting");
            queuingPlayers.add(tempPlayer);
            tempPlayer.getClient().startOrWait(tempPlayer.geStatus());
            

        }else{
            tempPlayer.setStatus("playing");
            Player enemy = getRandomPlayer();
            enemy.setStatus("playing");

            int tempGameId;

            while(true){
                tempGameId= currentGames.size()+1;
                if(currentGames.containsKey(tempGameId)){
                    tempGameId = tempGameId*2+1;
                }else{
                    break;
                }
            }
            
            Game currentGame = new Game(tempPlayer, enemy, tempGameId);
            assignRandomSymbols(tempPlayer, enemy);
            tempPlayer.getClient().startOrWait(tempPlayer.geStatus());
            enemy.getClient().startOrWait(tempPlayer.geStatus());

            currentGames.put(tempGameId, currentGame);

            Player startTurnPlayer = assignRandomStartTurn(tempPlayer, enemy);

            String annoucement = "Game Start, " + startTurnPlayer.playerFormat() + "'s turn to move ";

            tempPlayer.simpleAnnouncement(annoucement);
            enemy.simpleAnnouncement(annoucement);

            startTurnPlayer.getClient().setmyTurn(true);
            currentGame.setCurrentPlayer(startTurnPlayer);
            currentGame.getThePlayerNotCurrent().getClient().setmyTurn(false);

            tempPlayer.getClient().setGameId(tempGameId);
            enemy.getClient().setGameId(tempGameId);

            Iterator<Player> iterator = queuingPlayers.iterator();
            while (iterator.hasNext()) {
                Player checkPlayer = iterator.next();
                if (checkPlayer.equals(enemy)) {
                    iterator.remove();
                }
            }
            


        }



    }

    @Override
    public void quitWaiting(String username)throws RemoteException{
        Player targetPlayer = null;
        for(Player temPlayer : players){
            if(temPlayer.getUsername().equals(username)){
                targetPlayer = temPlayer;
            }
        }
        queuingPlayers.remove(targetPlayer);
    }


    public Game getGameByID(int id){
        return currentGames.get(id);
    }

    @Override
    public char[][] getGameBoardByid(int gameId)throws RemoteException{
        return getGameByID(gameId).getGameState().getGameBoard();
        
    }

    @Override
    public synchronized void move(int gameId, String username, int i, int j) throws RemoteException{
        Game currentGame = currentGames.get(gameId);

        boolean moveResult = currentGame.setGameState(i,j);
        
        // if the current user makes the movement successfully
        if(moveResult==true){
            currentGame.getP1().getClient().updateBoard(currentGame.getGameState());
            currentGame.getP2().getClient().updateBoard(currentGame.getGameState());


            boolean someOneWin = currentGame.checkWinning();
            boolean gameIsDraw = currentGame.checkDraw();
            if(gameIsDraw){
                String result = "draw";
                System.out.println(result);

                currentGame.getP1().drawAction();
                currentGame.getP2().drawAction();

                reRank();
                currentGames.remove(gameId);

            }

            // if someone wins the game, just update the information, no need to switch
            else if(someOneWin){
                Player winner = currentGame.getCurrentPlayer();
                String winnerName = winner.getUsername();
                char winnerSymbol = winner.getSymbol();

                String winnerOutput = "winner is: " + winnerName + " (" + winnerSymbol+") ";
                
                System.out.println(winnerOutput);

                winner.winAction();

                // update the rank of the players since someone wins
                reRank();
                // if winner is p1, then loser is p2
                Player loser;
                if (winner.equals(currentGame.getP1())){
                    loser = currentGame.getP2();
                }else{
                    loser = currentGame.getP1();
                }

                loser.loseAction(winnerName, String.valueOf(winner.getSymbol()));
                currentGames.remove(gameId);
            
                
            }else{
                // if there is no one winning the game and game is not draw, need to switch player, game continue
                currentGame.switchPlayer();

                String annouceMessage = currentGame.getCurrentPlayer().playerFormat() + "'s turn to move ";


                if(currentGame.getP1().getClient().myTurn() == true){
                    currentGame.getP1().getClient().setmyTurn(false);
                }else{
                    currentGame.getP1().getClient().setmyTurn(true);
                }
                
                if(currentGame.getP2().getClient().myTurn() == true){
                    currentGame.getP2().getClient().setmyTurn(false);
                }else{
                    currentGame.getP2().getClient().setmyTurn(true);
                }

                currentGame.getP1().simpleAnnouncement(annouceMessage);
                currentGame.getP2().simpleAnnouncement(annouceMessage);
            }

        }

        // if the movement isn't successful, it means it's not his turn or the grid is not empty
        return;

        
    }


    public void reRank(){
        // Step 1: Sort the currentClients based on Player's points
        Collections.sort(players, new Comparator<Player>() {
            @Override
            public int compare(Player p1, Player p2) {
                return Integer.compare(p2.getPoint(), p1.getPoint());
               
            }
        });

        // Step 2: Update the ranking
        int rank = 1;
        for (Player tempPlayer : players) {
            tempPlayer.setRanking(rank);
            rank++;
        }

    }

    @Override
    public synchronized void quitGame(int gameId, String username) throws RemoteException {
        Game targetGame = currentGames.get(gameId);
        if(targetGame!=null){
            Player targetPlayer = targetGame.getPlayerByName(username);
            if(targetPlayer.equals(targetGame.getP1())){
                targetGame.getP1().loseAction(targetGame.getP2().getUsername(), String.valueOf(targetGame.getP2().getSymbol()));
                targetGame.getP2().enemyQuitAction();
            }else{
                targetGame.getP2().loseAction(targetGame.getP1().getUsername(), String.valueOf(targetGame.getP1().getSymbol()));
                targetGame.getP1().enemyQuitAction();
            }
            reRank();
            currentGames.remove(gameId);

        }else{

            quitWaiting(username);
        }
        

    }

    @Override
    public synchronized void handleMessageSending(int gameId, String username, String text)throws RemoteException {
        Game currentGame = currentGames.get(gameId);
        Player messageSender = currentGame.getPlayerByName(username);
        String content = "Rank#" + messageSender.getRanking()+" " +username + ": " + text;

        currentGame.setChatMessage(content);

        currentGame.getP1().getClient().getChatHistory(currentGame.getGameState().getChatHistory());
        currentGame.getP2().getClient().getChatHistory(currentGame.getGameState().getChatHistory());
        
        

    }



    public static void main(String[] args){
        if(args.length != 2){
            System.out.println("Wrong format, input java -jar server.jar [ip] [port]");
            System.exit(1);

        }
        String ip = args[0];
        String port = args[1];

        int intPort = Integer.parseInt(port);

        System.out.println("Server is running");
        try {
            Registry registry = LocateRegistry.createRegistry(intPort);
    
            ServerInterface server = new GameServer(); 
            registry.rebind("GameServer", server);
    
            System.out.println("Server is ready at " + ip + ":" + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
