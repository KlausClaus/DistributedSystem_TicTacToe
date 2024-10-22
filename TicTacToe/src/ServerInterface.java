/*
 * Student ID: 1394392
 * Name: Renfei Yu (Klaus)
 */

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface ServerInterface extends Remote {
    public void move(int gameId, String username, int i, int j) throws RemoteException;
    public void initPlayer(String username, ClientInterface client)throws RemoteException ;
    public void quitGame(int gameId, String username) throws RemoteException;
    public void handleMessageSending(int gameId, String username, String text)throws RemoteException;
    public void quitWaiting(String username)throws RemoteException;
    public char[][] getGameBoardByid(int gameId)throws RemoteException;
}
