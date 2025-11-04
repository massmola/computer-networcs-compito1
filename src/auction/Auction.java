package auction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class Auction {
    public final static double USER_NOT_FOUND = Double.MIN_VALUE;

    // TODO, i will change return to Double.MIN_VALUE with a variable that hold the starting price or using NULL

    // TODO we missed to add placeBid() or is in an other class ???
    final Item item;
    boolean open;
    // We use long for comfort, because the duration is converted in millis to use some functions of the timer.
    private long durationInSeconds;

    /**
     * The map has as keys the users who participated in the auction so far,
     * and as values their respective highest bids.
     */
    HashMap<String, Double> usersBids = null;

    public Auction(Item item, long duration){
        this.item = item;
        this.open = false;
        this.durationInSeconds = duration;
        usersBids = new HashMap<>();
    }

    // -------- Utility Methods --------

    /**
     * Given a username, returns true if that user has already placed
     * a bid, false otherwise.
     */
    public boolean isUserRegistered(String username){
        return usersBids.containsKey(username);
    }

    /**
     * Returns the input user's bid. If the user is not registered, it returns
     * Double.MIN_VALUE
     */
    public double getUserBid(String username){
        if (!isUserRegistered(username))
            return USER_NOT_FOUND;
        return usersBids.get(username);
    }

    /**
     * Returns true if the user has already placed a bid and the bid is
     * higher than the input bid. Returns false if the user is not registered,
     * or the bid is lower or equal than the input bid.
     */
    public boolean hasUserAHigherBid(String username, double bid){
        if(!usersBids.containsKey(username))
            return false;
        return usersBids.get(username) > bid;
    }

    /**
     * Registers a user with its bid, regardless of the highest bid placed so far.
     * If the user is already registered, if the bid is higher than the previous,
     * the bid of the user is updated, otherwise the highest is kept.
     */
    // TODO we need a return value to know if the user was registered or not/and the pid was updated or not
    public boolean placeBid(String username, double bid){
        if(usersBids.containsKey(username)) {
            if(!isBidValid(bid))
                return false;
        }
        usersBids.put(username, bid);
        return true;
    }

    public boolean isBidValid(double bid){
        return isBidAmountSuperior(bid) && isBidIncrementValid(bid);
    }

    /**
     *
     * @param bid the new input bid
     * @return true if the input bid is higher than the current bid; false otherwise.
     */
    public boolean isBidAmountSuperior(double bid){
        return bid > Math.max(getHighestBid(), item.getStartPrice());
    }

    /**
     *
     * @param bid the new input bid
     * @return true if the input bid is a valid increment of the item's starting price; false otherwise.
     */
    public boolean isBidIncrementValid(double bid){

        if (bid == 0.0) return false;

        double difference = item.getStartPrice() - bid;
        double ratio = difference/item.getMinIncrement();
        double rounded = Math.round(ratio);
        double eps = 1e-9;

        return Math.abs(ratio - rounded) < eps;
    }

    /**
     * Removes the input user.
     */
    public void removeUser(String username){
        if(!usersBids.containsKey(username))
            return;
        usersBids.remove(username);
    }

    /**
     * Returns the user with the highest bid. If no user participated, returns null.
     */
    public String getHighestBidder(){
        if(usersBids.keySet().isEmpty())
            return null;

        double highestBid = getHighestBid();
        String highestBidder = null;

        for(String user : usersBids.keySet()){
            if(usersBids.get(user) == highestBid){
                highestBidder = user;
                break;
            }
        }

        return highestBidder;
    }

    /**
     * Returns the highest bid. If no user participated, returns Double.MIN_VALUE.
     */
    public double getHighestBid(){
        if(usersBids.keySet().isEmpty())
            return Double.MIN_VALUE;

        double highestBid = Double.MIN_VALUE;
        for(String user : usersBids.keySet()){
            double curBid = usersBids.get(user);
            if(curBid > highestBid){
                highestBid = curBid;
            }
        }

        return highestBid;
    }

    // -------- Getters and Setters --------

    public Item getItem() {
        return item;
    }

    public Map<String, Double> getUserHighestBids() {
        return usersBids;
    }

    public Set<String> getRegisteredUsers() {
        return usersBids.keySet();
    }

    // TODO
    //  solo cambiato il nome di getOpen in isOpen per maggiore chiarezza.
    public boolean isOpen(){
        return this.open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public long getDurationInSeconds() {
        return durationInSeconds;
    }
}
