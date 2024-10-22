/*
 * Student ID: 1394392
 * Name: Renfei Yu (Klaus)
 */

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ClientInterface extends Remote {
    public void updateBoard(GameState currGameState) throws RemoteException;
    public void setmyTurn(boolean temp)throws RemoteException;
    public boolean myTurn() throws RemoteException;
    public void announceTurn(String string) throws RemoteException;
    public void announceWinning(String string) throws RemoteException;
    public void getChatHistory(ArrayList<String> chat) throws RemoteException;
    public void setGameId(int tempGameId)throws RemoteException;
    public void startOrWait(String decision) throws RemoteException;

}
