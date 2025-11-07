package server;

import client.ClientInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    void registerClient(ClientInterface clientInterface) throws RemoteException;
    void removeClient(ClientInterface clientInterface) throws RemoteException;
    boolean registerUser(String username) throws RemoteException;
    String getStatus() throws RemoteException;
    void sendMessage(String username, String messageContent) throws RemoteException;
    boolean placeBid(String username, double bid) throws RemoteException;
}
