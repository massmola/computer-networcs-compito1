package it.unibz.cn.server;

import it.unibz.cn.auction.Item;

import java.net.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Initializes the server, the port and listens for clients who want to connect
 */
public class TCPServer {

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
                Connection c = new Connection(clientSocket);
            }

        } catch (IOException e) {
            System.out.println("Listen: " + e.getMessage());
        }
    }
}