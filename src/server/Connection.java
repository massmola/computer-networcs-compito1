package server;

import common.EClientToServerCommands;
import common.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

import static common.EServerToClientCommands.*;

/**
 * Handles a single connection with a single client.
 * Connection extends thread. That means that multiple connections can be handled in parallel by the server.
 */
class Connection extends Thread {

    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    ServerLogic serverLogic;
    String username = "";
    boolean userSet = false;
    boolean interruptConnection = false;

    public Connection (Socket aClientSocket, ServerLogic serverLogic) {
        try {
            this.clientSocket = aClientSocket;
            this.serverLogic = serverLogic;

            // Initialize streams of input and output
            in = new DataInputStream( clientSocket.getInputStream());
            out = new DataOutputStream( clientSocket.getOutputStream());

            // Start
            this.start();

        } catch(IOException e) {
            System.out.println("Connection: " + e.getMessage());
        }
    }

    public void run(){

        TCPServer.allClients.add(this);
        String outEncodedMessage = "";

        try { // an echo server

            while(true) {
                //Read the arriving message - blocking
                String inEncodedMessage = in.readUTF();

                // Process message from client
                outEncodedMessage = processClientInput(inEncodedMessage);

                // Close the connection if the user sends the exit
                if(interruptConnection){
                    break;
                }

                // Write the message to send
                out.writeUTF(outEncodedMessage);
            }

        } catch(EOFException e) {
            System.out.println("EOF: " + e.getMessage());
        } catch(IOException e) {
            System.out.println("IO:" + e.getMessage());
        } finally {
            TCPServer.allClients.remove(this);
            System.out.println("Client removed. Total clients: " + TCPServer.allClients.size());
            try {
                clientSocket.close();
            } catch (IOException e) {
                // ignore
            }

        }
    }

    public String processClientInput(String inString){

        Message inMessage = new Message();
        inMessage.decode(inString);
        Message outMessage = new Message();

        boolean broadcastMessage;

        // TODO: to switch-case

        switch (EClientToServerCommands.valueOf(inMessage.type)) {
            case REGISTER: {

                // Attempts user's registration
                broadcastMessage = false;
                if(serverLogic.registerUser(inMessage.content)){
                    outMessage.type = USER_REGISTER_SUCCESS.name();
                    username = inMessage.content;
                    userSet = true;
                } else {
                    outMessage.type = USER_REGISTER_FAIL.name();
                }

                break;
            }
            case QUIT: {

                // User interrupts connection with the server
                broadcastMessage = false;
                outMessage.type = PRINT_MESSAGE.name();
                outMessage.content = "Server received: EXIT";
                serverLogic.removeUser(username);
                interruptConnection = true;

                break;
            }
            case STATUS: {

                // Returns to the user the status of the current auction
                broadcastMessage = false;
                outMessage.type = PRINT_MESSAGE.name();
                outMessage.content = serverLogic.getAuctionStatus();

                break;
            }
            case MESSAGE: {

                // Broadcasts the message entered by the user
                broadcastMessage = true;
                outMessage.type = PRINT_MESSAGE.name();
                outMessage.content = "-> " + username + ": " + inMessage.content;

                break;
            }
            case BID: {

                // Attempts to place a bid. If the placement is successful, broadcasts to all users
                outMessage.type = PRINT_MESSAGE.name();
                double bidAmount = Double.parseDouble(inMessage.getContent());

                if(serverLogic.getActiveAuction().isBidValid(bidAmount)){
                    broadcastMessage = true;
                    serverLogic.placeBid(username, bidAmount);
                    outMessage.content = "-> " + username + " places a bid of: €" + inMessage.content;
                } else {
                    broadcastMessage = false;
                    outMessage.content = "The input bid was refused.\n" +
                            "The bid must be a value higher than the current bid's value\n" +
                            "and a valid multiple of the item's minimum increment.";
                }

                break;
            }
            default: {
                broadcastMessage = false;
                outMessage.type = IGNORE.name();
                outMessage.content = "";
            }
        }

        if(broadcastMessage){
            TCPServer.broadcast(outMessage.encode());
            outMessage.type = IGNORE.name();
            outMessage.content = "";
        }

        return outMessage.encode();
    }


    /**
     * Sends a message to just this client.
     * Synchronized to prevent multiple threads (e.g., a broadcast and a
     * private reply) from writing at the exact same time.
     */
    public synchronized void sendMessage(String msg) {
        try {
            if (!clientSocket.isClosed()) {
                this.out.writeUTF(msg);
                this.out.flush();
            }
        } catch (IOException e) {
            System.out.println("Failed to send to " + clientSocket.getRemoteSocketAddress() + ": " + e.getMessage());
        }
    }
}