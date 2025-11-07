package server;

import client.ClientInterface;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class RMIServer extends UnicastRemoteObject implements ServerInterface {

    // Fixed port for the remote object (NOT the registry)

    private static final ArrayList<ClientInterface> clients = new ArrayList<>();
    private static final int SERVER_PORT = 1100;
    static ServerLogic serverLogic = new ServerLogic();

    public static void main(String[] args) {
        try {
            // Start the RMI registry on port 1099 (default)
            LocateRegistry.createRegistry(1099);
            System.out.println("RMI registry started.");

            // Create an instance of the server and bind it
            RMIServer server = new RMIServer();
            Naming.rebind("rmi://localhost:1099/RMI", server);

            System.out.println("Server is ready.");
            serverLogic.createAllAuctions();
            serverLogic.nextAuction();

        } catch (Exception e) {
            System.err.println("Server exception: " + e);
            e.printStackTrace();
        }
    }

    protected RMIServer() throws RemoteException {
        super(SERVER_PORT);
    }

    public static void broadcast (String message) throws RemoteException {
        for(ClientInterface c : clients){
            c.receiveMessage(message);
        }
    }


    // ------- RMI Server Methods -------

    @Override
    public void registerClient(ClientInterface clientInterface) throws RemoteException {
        if(!clients.contains(clientInterface)){
            clients.add(clientInterface);
        } else {
        }
    }

    @Override
    public void removeClient(ClientInterface clientInterface) throws RemoteException {
        clients.remove(clientInterface);
    }

    @Override
    public boolean registerUser(String username) throws RemoteException {
        return serverLogic.registerUser(username);
    }

    @Override
    public String getStatus() throws RemoteException {
        return serverLogic.getAuctionStatus();
    }

    @Override
    public void sendMessage(String username, String messageContent) throws RemoteException {
        broadcast("-> " + username + ": " + messageContent);
    }

    @Override
    public boolean placeBid(String username, double bid) throws RemoteException {
        boolean bidSuccessful = serverLogic.placeBid(username, bid);
        if(bidSuccessful) broadcast("-> " + username + " places a bid of €" + bid);
        return bidSuccessful;
    }


}
