package it.unibz.cn.auction;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Auction {

    final Item item;
    boolean open;

    /**
     * The map has as keys the users who participated in the auction so far,
     * and as values their respective highest bids.
     */
    HashMap<String, Double> usersHighestBids = null;

    public Auction(Item item){
        this.item = item;
        this.open = false;
    }

    // -------- Utility Methods --------

    /**
     * Given a username, returns true if that user has already placed
     * a bid, false otherwise.
     */
    public boolean isUserRegistered(String username){
        return false;
    }

    /**
     * Returns true if the input bid is equal or higher than the highest
     * bid placed so far, false otherwise.
     */
    public boolean isBidHighest(double bid){
        return false;
    }

    /**
     * Returns true if the user has already placed a bid and the bid is
     * higher than the input bid. Returns false if the user is not registered,
     * or the bid is lower than the input bid.
     */
    public boolean hasUserAHigherBid(String username, double bid){
        return false;
    }

    /**
     * Registers a user with its bid, regardless of the highest bid placed so far.
     * If the user is already registered, if the bid is higher than the previous,
     * the bid of the user is updated, otherwise the highest is kept.
     */
    public void addUserAndBid(String username, double bid){
        return;
    }

    /**
     * Removes the input user.
     */
    public void removeUser(String username){
        return;
    }

    /**
     * Returns the user with the highest bid. If no user participated, returns null.
     */
    public String getHighestBidder(){
        return null;
    }

    /**
     * Returns the highest bid. If no user participated, returns Double.MIN_VALUE.
     */
    public double getHighestBid(){
        return Double.MIN_VALUE;
    }

    // -------- Getters and Setters --------

    Item getItem() {
        return item;
    }

    Map<String, Double> getUserHighestBids() {
        return usersHighestBids;
    }

    Set<String> getRegisteredUsers() {
        return usersHighestBids.keySet();
    }

    public boolean getOpen(){
        return this.open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}
