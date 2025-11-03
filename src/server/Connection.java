package server;

import common.Commands;
import common.EServerToClientCommands;
import common.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

import static common.EClientToServerCommands.*;
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
            System.out.println("IO:s a" + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                /*close failed*/
            }
        }
    }

    public String processClientInput(String inString){

        Message inMessage = new Message();
        inMessage.decode(inString);
        Message outMessage = new Message();

        if(inMessage.type.equals(Commands.getClientCommand(REGISTER))){

            // attempt registration
            if(serverLogic.registerUser(inMessage.content)){
                outMessage.type = Commands.getServerCommand(USER_REGISTER_SUCCESS);
                username = inMessage.content;
                userSet = true;
            } else {
                outMessage.type = Commands.getServerCommand(USER_REGISTER_FAIL);
            }

        } else if (inMessage.type.equals(Commands.getClientCommand(EXIT))){

            // User interrupts connection with the server
            outMessage.type = Commands.getServerCommand(PRINT_MESSAGE);
            outMessage.content = "Server received: EXIT";
            serverLogic.removeUser(username);
            interruptConnection = true;

        } else if (inMessage.type.equals(Commands.getClientCommand(STATUS))) {

            // TODO: User asks status
            outMessage.type = Commands.getServerCommand(PRINT_MESSAGE);
            outMessage.content = "Server received: " + username + " ASKS STATUS";
            /*
             *
             *
             *
             */

        } else if (inMessage.type.equals(Commands.getClientCommand(MESSAGE))) {

            // TODO: User sends message to chat
            outMessage.type = Commands.getServerCommand(PRINT_MESSAGE);
            outMessage.content = "Server received: " + username + " SENDS MESSAGE: " + inMessage.content;
            /*
             *
             *
             *
             */

        } else if (inMessage.type.equals(Commands.getClientCommand(BID))) {

            // TODO: User places a bid
            outMessage.type = Commands.getServerCommand(PRINT_MESSAGE);
            outMessage.content = "Server received: " + username + " PLACES A BID OF " + inMessage.content;
            /*
             *
             *
             *
             */

        } else {

            // Ignore server message
            outMessage.type = Commands.getServerCommand(IGNORE);
            outMessage.content = "";
        }

        return outMessage.encode();
    }
}