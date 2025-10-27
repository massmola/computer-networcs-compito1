package it.unibz.cn.server;

import java.net.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class TCPServer {
    final static int SERVER_PORT = 7896;
    
    // --- Shared Server State ---

    // Thread-safe list for all connected clients
    static List<Connection> allClients = new CopyOnWriteArrayList<>();
    
    // List of items to be sold.
    static List<Item> itemsToSell = new ArrayList<>();
    
    // The item currently up for bidding.
    // Must be volatile to be visible across threads.
    static volatile Item currentItem = null;

    // A dedicated lock for handling bids and changing the currentItem.
    // This prevents race conditions 
    static final Object auctionLock = new Object();

    public static void main(String args[]) {
        
        // Initialize the items for auction
        initializeItems();

        // Start the "Auctioneer" thread
        // This thread controls the flow of the auction (e.g., "Item 1 starts... sold!")
        new Thread(TCPServer::runAuctionLogic).start();

        // Start the main server loop to accept new clients
        try (
            ServerSocket listenSocket = new ServerSocket(SERVER_PORT);
        ){
            System.out.println("Auction Server started on port " + SERVER_PORT);
            System.out.println("Waiting for clients...");
            
            while (true) {
                Socket clientSocket = listenSocket.accept();
                // A new connection is created and it adds itself to the allClients list
                new Connection(clientSocket); 
            }
        } catch (IOException e) {
            System.out.println("Listen Socket error: " + e.getMessage());
        }
    }

    /**
     * Fills the list of items to be auctioned.
     */
    private static void initializeItems() {
        itemsToSell.add(new Item("Vintage Monet Painting", 1000.0, 50.0));
        itemsToSell.add(new Item("Antique Roman Coin", 150.0, 10.0));
        itemsToSell.add(new Item("Signed 1st Edition 'Ulysses'", 800.0, 40.0));
        System.out.println("Initialized " + itemsToSell.size() + " items for auction.");
    }

    /**
     * The main logic for the auctioneer. This runs in its own thread
     * and cycles through the items, applying timers.
     */
    private static void runAuctionLogic() {
        try {
            // Wait 10 seconds on startup for people to join
            System.out.println("Auctioneer started. Waiting 10s for clients...");
            Thread.sleep(10000); 
            
            for (Item item : itemsToSell) {
                // Set the new current item and announce it
                synchronized (auctionLock) {
                    currentItem = item;
                }
                
                broadcast("--- NEW ITEM FOR AUCTION ---");
                broadcast(item.toString());
                broadcast("Bidding starts in 15 seconds. Type /bid <amount>");
                Thread.sleep(15000); // 15s preview
                
                broadcast(">>> AUCTION OPEN for '" + item.description + "'! <<<");
                broadcast("You have 45 seconds to bid.");
                Thread.sleep(45000); // 45s bidding time
                
                // Close bidding for this item
                synchronized (auctionLock) {
                    broadcast(">>> AUCTION CLOSED for '" + item.description + "'! <<<");
                    
                    if (item.currentHighestBidder != null) {
                        broadcast(String.format(
                            "SOLD to %s for $%.2f",
                            item.currentHighestBidder, item.currentHighestBid
                        ));
                    } else {
                        broadcast("Item was not sold (no bids).");
                    }
                    
                    currentItem = null; // No item is active
                }
                
                broadcast("Next item in 10 seconds...");
                Thread.sleep(10000); // 10s pause
            }
            
            broadcast("--- THE AUCTION IS NOW OVER ---");
            broadcast("Thank you for participating!");
            System.out.println("Auctioneer finished.");
            // Server will continue running, but no new items will be up.

        } catch (InterruptedException e) {
            System.err.println("Auctioneer thread interrupted.");
            Thread.currentThread().interrupt();
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

    /**
     * Attempts to place a bid. This is the only way to modify the auction state
     * and is synchronized to be thread-safe.
     * @param bidder The nickname of the user bidding
     * @param amount The amount they are bidding
     * @param conn The connection object of the bidder (to send private replies)
     */
    public static void placeBid(String bidder, double amount, Connection conn) {
        // 10. Synchronize on the lock to check/update the item
        synchronized (auctionLock) {
            if (currentItem == null) {
                conn.sendMessage("[Error] No auction is currently active. Cannot bid.");
                return;
            }
            
            double minBid = currentItem.getMinimumNextBid();
            
            if (amount < minBid) {
                conn.sendMessage(String.format(
                    "[Error] Bid too low. Minimum bid is $%.2f", minBid
                ));
            } else {
                // We have a new high bid!
                currentItem.currentHighestBid = amount;
                currentItem.currentHighestBidder = bidder;
                
                // Announce the new high bid to everyone
                broadcast(String.format(
                    "--- NEW HIGH BID: %s bids $%.2f for '%s' ---",
                    bidder, amount, currentItem.description
                ));
            }
        }
    }
}