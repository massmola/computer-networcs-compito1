package it.unibz.cn.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

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

        String outEncodedMessage = "";

        try { // an echo server

            while(true) {
                //Read the arriving message - blocking
                String inEncodedMessage = in.readUTF();

                // Close the connection if the user sends the exit
                if(inEncodedMessage.equals("exit")){
                    break;
                }

                if(!userSet) {
                    if(!serverLogic.getUsers().contains(inEncodedMessage)){
                        serverLogic.addUser(inEncodedMessage);
                        outEncodedMessage = "User set. Place a bid: ";
                        userSet = true;
                    } else {
                        outEncodedMessage = "UserUnavailable.\nEnter a valid username: ";
                    }
                } else {
                    outEncodedMessage = "Enter a valid command: ";
                }

                // Write the message to send
                out.writeUTF(outEncodedMessage);
            }

        } catch(EOFException e) {
            System.out.println("EOF: " + e.getMessage());
        } catch(IOException e) {
            System.out.println("IO:s a" + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                /*close failed*/
            }
        }
    }
}