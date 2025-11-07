package server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

import auction.*;
import common.EServerToClientCommands;
import common.Message;

import static server.AuctionLoaderFromTxt.AuctionLoader.loadAuctions;

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

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledFuture;
    private ArrayList<Auction> auctions;
    private final Set<String> users;
    private Auction activeAuction = null;
    private int curAuctionId = -1;

    public ServerLogic() {
        this.auctions = new ArrayList<>();
        this.users = new HashSet<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    // ========== AUCTION MANAGEMENT ==========

    public synchronized void createAllAuctions(){
        auctions = loadAuctions("src/server/auctions.txt");
    }

    /**
     * Closes the current auction, if there is one, ad broadcasts the result.
     * If there is a next auction, opens it and starts the timer.
     * If all auctions are closed, broadcasts a message saying that all auctions are terminated.
     */

    public synchronized void nextAuction() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("-> ");

        // Checks if there is a previous auction to close.
        /*
         * If there is previous auction to close, close it and send the ending message to announce
         * the winner.
         * If there is no active auction, this is the first one.
         */

        if(activeAuction != null){
            activeAuction.setOpen(false);
            stringBuilder.append(getCloseAuctionMessage(activeAuction));
            stringBuilder.append("\nAuction was closed\n");
        }

        // Check if there is a next auction.
        /*
         * If there is a next auction, update curAuctionId and activeAuction.
         * If the starting auction is the last, pass as runnable the ending function.
         * Start the timer on the auction.
         *
         * If there is not a next auction, do nothing.
         */

        if(auctions.size() <= curAuctionId + 1 || auctions.isEmpty()){
            stringBuilder.append("All Auctions are terminated");
            activeAuction = null;
            scheduler.shutdown();
        } else {

            // Start next auction;
            curAuctionId = curAuctionId + 1;
            activeAuction = auctions.get(curAuctionId);
            activeAuction.setOpen(true);

            stringBuilder.append("Next Auction Started");

            // Schedule time
            scheduledFuture = scheduler.schedule(() -> {
                nextAuction();
            }, activeAuction.getDurationInSeconds(), TimeUnit.SECONDS);
        }

        Message message = new Message();
        message.type = EServerToClientCommands.PRINT_MESSAGE.name();
        message.content = stringBuilder.toString();

        try{
            RMIServer.broadcast(message.encode());
        } catch (RemoteException e){
            System.out.println("Remote exception: " + e);
        }
    }

    /**
     * @return remaining time of the currently active auction
     */
    public long getRemainingTime(){
        if(scheduledFuture.isDone() || scheduledFuture.isCancelled()) return 0;
        else return(scheduledFuture.getDelay(TimeUnit.SECONDS));
    }

    /**
     * Places a bid on the active auction
     *
     * @param amount the bid amount
     * @return true if the bid was accepted, false otherwise
     */
    public synchronized boolean placeBid(String user, double amount) {

        if (activeAuction == null) {
            return false;
        }

        if(!activeAuction.isBidValid(amount)){
            return false;
        }

        boolean accepted = activeAuction.placeBid(user, amount);

        if (accepted) {
            System.out.println("[ServerLogic] New bid: " + activeAuction.getItem().getName() + " -> " + user + " €" + amount);
        }

        return accepted;
    }

    /**
     * Gets the current status of an auction
     *
     * @return a formatted string with auction details, or error message if not found
     */
    public String getAuctionStatus() {

        if (activeAuction == null) {
            return "No active auctions found";
        }

        StringBuilder status = new StringBuilder();
        status.append("=== Auction: ").append(activeAuction.getItem().getDescription()).append(" ===\n");
        status.append("Starting price: €").append(activeAuction.getItem().getStartPrice()).append("\n");
        status.append("Minimum increment: €").append(activeAuction.getItem().getMinIncrement()).append("\n");
        status.append("Remaining time: ").append(formatTime(getRemainingTime())).append("\n");

        double highestBid = activeAuction.getHighestBid();
        if (highestBid != Double.MIN_VALUE) {
            status.append("Current bid: €").append(highestBid).append("\n");
            status.append("Highest bidder: ").append(activeAuction.getHighestBidder()).append("\n");
        } else {
            status.append("No bids yet\n");
        }

        return status.toString();
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

    // ------- Private methods -------

    private String getCloseAuctionMessage(Auction auction){

        if(auction.getHighestBidder() == null){
            return "Auction: " + auction.getItem().getDescription() +
                    "\nterminated with no winning bidder.";
        } else {
            return "Auction: " + auction.getItem().getDescription() +
                    "\nis won by: " + auction.getHighestBidder() +
                    "\nwith a winning bid of: €" + auction.getHighestBid();
        }
    }

    private static String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append(" hour").append(hours > 1 ? "s" : "");
        if (minutes > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
        }
        if (secs > 0 || sb.length() == 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(secs).append(" second").append(secs != 1 ? "s" : "");
        }
        return sb.toString();
    }
}