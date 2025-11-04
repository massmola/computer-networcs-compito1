package server;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import auction.*;

/**
 * ServerLogic manages the business logic of the auction system,
 * independent of the communication protocol used.
 *
 * Responsibilities:
 * - Create and manage auctions
 * - Handle bids
 * - Close auctions and determine winners
 * - Provide auction status and information
 *
 * This class does NOT handle:
 * - Client connections (managed by Connection/TCPServer)
 * - Notifications (handled by the caller)
 * - Network protocol details
 */
public class ServerLogic {

    // Map of active auctions: auctionId -> Auction
    // used a ConcurrentHashMap to allow multiple threads to access the map concurrently
    private final Set<Auction> auctions;

    private final Set<String> users;

    private Auction activeAuction;

    public ServerLogic() {
        this.auctions = new HashSet<>();
        this.users = new HashSet<>();
    }

    // ========== AUCTION MANAGEMENT ==========

    /**
     * Creates a new auction
     *
     * @param item the Item object being auctioned
     * @param duration the duration of the auction in minutes
     * @return the auctionId if created successfully, null if already exists
     */
    public synchronized String createAuction(Item item, long duration) {

        try {
            auctions.add(new Auction(item, duration));
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (NullPointerException e) {
            return "Auction cannot be null";
        }

        // TODO call the startTimer un open Auction


//        if (auctions.containsKey(auctionId)) {
//            return null; // Auction already exists
//        }
//
////        Auction auction = new Auction(item);
//        auctions.put(auctionId, auction);
//
//        System.out.println("[ServerLogic] Auction created: " + auctionId);
//        return auctionId;
    }

    /**
     * Starts an Auction
     *
     * @param auctionId the auction identifier
     * @return a result message indicating that the auction started, or null if there is no auction for the input id
     */
    public synchronized String startAuction(String auctionId){
        if (!auctions.containsKey(auctionId)) {
            return null; // Auction id does not exist
        }

        if(activeAuction != null && !activeAuction.isOpen() ){
            auctions.get(auctionId).setOpen(true);
        } else {
            return "Auction " + auctionId + " can not be started, it is already open an other one";
        }

        String result = "Auction " + auctionId + " started";
        return result;
    }

    /**
     * Places a bid on an auction
     *
     * @param auctionId the auction identifier
     * @param bidderId the bidder's identifier (username)
     * @param amount the bid amount
     * @return true if the bid was accepted, false otherwise
     */
    public synchronized boolean placeBid(String auctionId, String bidderId, double amount) {
        Auction auction = auctions.get(auctionId);

        if (auction == null) {
            return false; // Auction not found
        }

        // TODO do we need to check if it is open? Cant be a check in place bid that will return a message or error?
        if (!auction.isOpen()) {
            return false; // Auction is closed
        }

        // TODO check if the bid was successful?
        boolean accepted = auction.addUserAndBid(bidderId, amount);

        if (accepted) {
            System.out.println("[ServerLogic] New bid: " + auctionId + " -> " + bidderId + " €" + amount);
        }

        return accepted;
    }

    /**
     * Closes an auction and determines the winner
     *
     * @param auctionId the auction identifier
     * @return a result message describing the outcome, or error message if auction not found
     */
    public synchronized String closeAuction(String auctionId) {
        Auction auction = auctions.get(auctionId);

        if (auction == null) {
            return "Auction not found: " + auctionId;
        }

        if (!auction.isOpen()) {
            return "Auction " + auctionId + " is already closed";
        }

        auction.setOpen(false);

        String result;
        String winner = auction.getHighestBidder();
        if (winner != null) {
            result = "Auction " + auctionId + " closed - WINNER: " +
                    winner + " with €" + auction.getHighestBid();
        } else {
            result = "Auction " + auctionId + " closed - No winner (no bids)";
        }

        System.out.println("[ServerLogic] " + result);
        return result;
    }

    /**
     * Gets the current status of an auction
     *
     * @param auctionId the auction identifier
     * @return a formatted string with auction details, or error message if not found
     */
    public String getAuctionStatus(String auctionId) {
        Auction auction = auctions.get(auctionId);

        if (auction == null) {
            return "Auction not found: " + auctionId;
        }

        StringBuilder status = new StringBuilder();
        status.append("=== Auction: ").append(auction.getItem().getName()).append(" ===\n");
        status.append("ID: ").append(auctionId).append("\n");
        status.append("Status: ").append(auction.isOpen() ? "OPEN" : "CLOSED").append("\n");
        status.append("Starting price: €").append(auction.getItem().getStartPrice()).append("\n");

        // TODO check for auction -> i will change return to Double.MIN_VALUE with a variable that hold the starting price or using NULL
        double highestBid = auction.getHighestBid();
        if (highestBid != Double.MIN_VALUE) {
            status.append("Current bid: €").append(highestBid).append("\n");
            status.append("Current bidder: ").append(auction.getHighestBidder()).append("\n");
        } else {
            status.append("No bids yet\n");
        }

        return status.toString();
    }

    /**
     * Lists all available auctions
     *
     * @return a formatted string with all auctions and their status
     */
    public String listAuctions() {
        if (auctions.isEmpty()) {
            return "No auctions available";
        }

        StringBuilder list = new StringBuilder("=== AVAILABLE AUCTIONS ===\n");

        for (Map.Entry<String, Auction> entry : auctions.entrySet()) {
            String auctionId = entry.getKey();
            Auction auction = entry.getValue();

            list.append(auctionId)
                    .append(" - ")
                    .append(auction.getItem().getName())
                    .append(" [")
                    .append(auction.isOpen() ? "OPEN" : "CLOSED")
                    .append("] ");

            // TODO check for auction -> i will change return to Double.MIN_VALUE with a variable that hold the starting price or using NULL
            double highestBid = auction.getHighestBid();
            if (highestBid != Double.MIN_VALUE) {
                list.append("€").append(highestBid);
            } else {
                list.append("(starting: €").append(auction.getItem().getStartPrice()).append(")");
            }

            list.append("\n");
        }

        return list.toString();
    }

    /**
     * Removes a closed auction from the system
     *
     * @param auctionId the auction identifier
     * @return true if removed successfully, false if not found or still open
     */
    public synchronized boolean removeAuction(String auctionId) {
        Auction auction = auctions.get(auctionId);

        if (auction == null) {
            return false; // Auction not found
        }

        if (auction.isOpen()) {
            return false; // Cannot remove an open auction
        }

        auctions.remove(auctionId);
        System.out.println("[ServerLogic] Auction removed: " + auctionId);
        return true;
    }

    /**
     * Gets a specific auction object
     *
     * @param auctionId the auction identifier
     * @return the Auction object, or null if not found
     */
    public Auction getAuction(String auctionId) {
        return auctions.get(auctionId);
    }

    /**
     * Checks if an auction exists
     *
     * @param auctionId the auction identifier
     * @return true if the auction exists, false otherwise
     */
    public boolean hasAuction(String auctionId) {
        return auctions.containsKey(auctionId);
    }

//    /**
//     * Gets the number of currently active (open) auctions
//     *
//     * @return the count of open auctions
//     */
//    public int getActiveAuctionsCount() {
//        return (int) auctions.values().stream()
//                .filter(Auction::isOpen)
//                .count();
//    }

    /**
     * Gets the total number of auctions (both open and closed)
     *
     * @return the total count of auctions
     */
    public int getTotalAuctionsCount() {
        return auctions.size();
    }

    /**
     * Closes all open auctions (typically called during server shutdown)
     *
     * @return the number of auctions that were closed
     */
    public synchronized int closeAllAuctions() {
        int count = 0;
        for (Auction auction : auctions.values()) {
            if (auction.isOpen()) {
                auction.setOpen(false);
                count++;
            }
        }
        System.out.println("[ServerLogic] Closed " + count + " open auctions");
        return count;
    }

    /**
     * Clears all auctions from the system
     * Warning: This should only be called during shutdown
     */
    public synchronized void clearAllAuctions() {
        auctions.clear();
        System.out.println("[ServerLogic] All auctions cleared");
    }

    /**
     * Attempts to add the input user to the set of users.
     * It may fail if the input username is already in the set of users.
     *
     * @param user the input user to add to the set of users.
     * @return true if the inout user is successfully added to the set. Returns false
     * if the user is already in the set.
     */
    public synchronized boolean registerUser(String user){
        if(users.contains(user)){
            return false;
        }
        users.add(user);
        return true;
    }

    /**
     * Attempts to remove the input user to the set of users.
     * It may fail if the input username is not in the set of users.
     *
     * @param user the input user to remove from the set of users.
     * @return true if the inout user is successfully removed from the set. Returns false
     * if the user is not in the set.
     */
    public synchronized boolean removeUser(String user){
        if(!users.contains(user)){
            return false;
        }

        users.remove(user);
        return true;
    }

    public synchronized Set<String> getUsers(){
        return users;
    }

    public Auction getActiveAuction() {
        return activeAuction;
    }
}