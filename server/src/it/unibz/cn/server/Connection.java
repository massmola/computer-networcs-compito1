package it.unibz.cn.server;

import it.unibz.cn.auction.Item;

import java.net.*;
import java.io.*;

/**
 * Handles a single connection with a single client.
 * Connection extends thread. That means that multiple connections can be handled in parallel by the server.
 */
class Connection extends Thread {

    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;

    public Connection (Socket aClientSocket) {
        try {
            clientSocket = aClientSocket;

            // Initialize streams of input and output
            in = new DataInputStream( clientSocket.getInputStream());
            out = new DataOutputStream( clientSocket.getOutputStream());

            // Start
            this.start();

        } catch(IOException e) {
            System.out.println("Connection: "+e.getMessage());
        }
    }

    public void run(){

        try { // an echo server
            //Read the arriving message - blocking
            String data = in.readUTF();

            // Write the message to send
            out.writeUTF(data);

        } catch(EOFException e) {
            System.out.println("EOF: "+e.getMessage());
        } catch(IOException e) {
            System.out.println("IO:s a"+e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                /*close failed*/
            }
        }
    }
}