package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Initializes the server, the port and listens for clients who want to connect
 */
public class TCPServer {

    /**
     * 1) Begin Auction timers.
     * 2) Keep track of all users(clients), map all users to connections.
     * 3) Notify when a client places a bid (performs an action).
     * 4) Terminate current auction when the timer is elapsed, notify winner and start the new one.
     * 5) To decide: when all auctions are closed.
     *
     * X) Check username availability.
     */

    // Thread-safe list for all connected clients
    static List<Connection> allClients = new CopyOnWriteArrayList<>();
    static ServerLogic serverLogic = new ServerLogic();
    static final int  SERVER_PORT = 7896;

    public static void main (String args[]) {

        serverLogic.createAllAuctions();
        serverLogic.nextAuction();
        
        // Port Creation - this is the port where the server listens
        try (
    
            // Server Socket Creation
            ServerSocket listenSocket = new ServerSocket(SERVER_PORT);
        ){

            // The server listens for clients who want to connect
            while(true) {
                // accept() blocks the execution until a client connects
                Socket clientSocket = listenSocket.accept();

                // When a client connects, a new thread "Connection" is created to handle that connection
                Connection c = new Connection(clientSocket, serverLogic);
            }

        } catch (IOException e) {
            System.out.println("Listen: " + e.getMessage());
        }
    }

    /**
     * Sends a message to every single connected client.
     */
    public static void broadcast(String message) {
        System.out.println("BROADCAST: " + message); // Log to server console
        
        for (Connection c : allClients) {
            c.sendMessage(message);
        }

    }
}