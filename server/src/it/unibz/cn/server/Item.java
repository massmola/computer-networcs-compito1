package it.unibz.cn.server;

/**
 * Represents a single item up for auction.
 */
public class Item {
    String description;
    double startPrice;
    double minIncrement;
    
    // Volatile as they are written by the auction thread and read by Connection threads
    volatile double currentHighestBid = 0;
    volatile String currentHighestBidder = null; // Nickname of the highest bidder

    public Item(String description, double startPrice, double minIncrement) {
        this.description = description;
        this.startPrice = startPrice;
        this.minIncrement = minIncrement;
        this.currentHighestBid = 0; // Start with no bid
        this.currentHighestBidder = null;
    }

    /**
     * Calculates the minimum legal bid required to be the new high bidder.
     */
    public double getMinimumNextBid() {
        if (currentHighestBid == 0) {
            return startPrice;
        }
        return currentHighestBid + minIncrement;
    }

    @Override
    public String toString() {
        return String.format(
            "Item: '%s' (Start Price: $%.2f, Min Increment: $%.2f)",
            description, startPrice, minIncrement
        );
    }
}