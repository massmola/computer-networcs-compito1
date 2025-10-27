package it.unibz.cn.server;

import java.net.*;
import java.io.*;

class Connection extends Thread {

    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;

    public Connection(Socket aClientSocket) {
        try {
            this.clientSocket = aClientSocket;
            this.in = new DataInputStream(clientSocket.getInputStream());
            this.out = new DataOutputStream(clientSocket.getOutputStream());
            this.start();
        } catch (IOException e) {
            System.out.println("Connection: " + e.getMessage());
        }
    }

    public void run() {
        try {
            // Add this client to the global list
            TCPServer.allClients.add(this);
            System.out.println("Client added. Total clients: " + TCPServer.allClients.size());
            
            // Send welcome message and current status
            sendWelcomeMessage();
            
            String rawData;
            // Loop, reading messages from the client
            while ((rawData = this.in.readUTF()) != null) {
                
                // Parse the client's message
                // The client sends "nickname: message"
                int separatorIndex = rawData.indexOf(": ");
                if (separatorIndex == -1) {
                    System.out.println("Received malformed message: " + rawData);
                    continue; // Ignore
                }
                
                String nickname = rawData.substring(0, separatorIndex);
                String message = rawData.substring(separatorIndex + 2).trim();

                // Handle commands
                if (message.startsWith("/bid ")) {
                    handleBid(nickname, message);
                } 
                else if (message.equals("/list")) {
                    sendCurrentItemStatus();
                } 
                else if (message.equals("/help")) {
                    sendHelp();
                } 
                else if (message.equals("exit") || message.equals("/quit")) {
                    break; // Client requested disconnect
                }
                else {
                    // If not a command, it's a chat message. Broadcast it.
                    TCPServer.broadcast(rawData);
                }
            }
        } catch (EOFException e) {
            System.out.println("Client disconnected: " + clientSocket.getRemoteSocketAddress());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            // Remove client from list and close socket
            TCPServer.allClients.remove(this);
            System.out.println("Client removed. Total clients: " + TCPServer.allClients.size());
            try {
                clientSocket.close();
            } catch (IOException e) {
                /* close failed */
            }
        }
    }

    /**
     * Parses a bid command and passes it to the main server.
     */
    private void handleBid(String nickname, String message) {
        try {
            String[] parts = message.split(" ");
            if (parts.length != 2) {
                sendMessage("[Error] Invalid bid. Use: /bid <amount>");
                return;
            }
            double bidAmount = Double.parseDouble(parts[1]);
            TCPServer.placeBid(nickname, bidAmount, this);
            
        } catch (NumberFormatException e) {
            sendMessage("[Error] Invalid amount. Please enter a number.");
        }
    }

    /**
     * Sends a private message to this client with the current item's status.
     */
    private void sendCurrentItemStatus() {
        // Use the lock to safely read the currentItem
        synchronized (TCPServer.auctionLock) {
            if (TCPServer.currentItem != null) {
                Item item = TCPServer.currentItem;
                sendMessage("--- Current Item Status ---");
                sendMessage(item.toString());
                if (item.currentHighestBidder != null) {
                    sendMessage(String.format(
                        "Current Bid: $%.2f (by %s)",
                        item.currentHighestBid, item.currentHighestBidder
                    ));
                } else {
                    sendMessage(String.format(
                        "No bids yet. Minimum bid is $%.2f",
                        item.getMinimumNextBid()
                    ));
                }
            } else {
                sendMessage("No auction is currently active. Please wait.");
            }
        }
    }

    /**
     * Sends a welcome message to the client when they first join.
     */
    private void sendWelcomeMessage() {
        sendMessage("Welcome to the Synchronous Auction!");
        sendMessage("Type /help for a list of commands.");
        sendCurrentItemStatus();
    }
    
    /**
     * Sends the command list to the client.
     */
    private void sendHelp() {
        sendMessage("--- Auction Commands ---");
        sendMessage("/bid <amount>  - Place a bid on the current item.");
        sendMessage("/list          - Show info about the current item.");
        sendMessage("/help          - Show this help message.");
        sendMessage("exit or /quit  - Disconnect from the auction.");
        sendMessage("(Anything else)  - Send a chat message to everyone.");
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