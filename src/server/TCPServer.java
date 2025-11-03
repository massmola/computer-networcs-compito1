package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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

    static ServerLogic serverLogic = new ServerLogic();

    public static void main (String args[]) {

        try{

            // Port Creation - this is the port where the server listens
            int serverPort = 7896;

            // Server Socket Creation
            ServerSocket listenSocket = new ServerSocket(serverPort);

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
}