package it.unibz.cn.auction;

/**
 * Represents a single item up for auction.
 */
public class Item {

    // TODO do we need to add a name?
    String name;
    String description;
    double startPrice;
    double minIncrement;
    
    // Volatile as they are written by the auction thread and read by Connection threads

    public Item(String description, double startPrice, double minIncrement) {
        this.description = description;
        this.startPrice = startPrice;
        this.minIncrement = minIncrement;
    }

    @Override
    public String toString() {
        return String.format(
            "Item: '%s' (Start Price: $%.2f, Min Increment: $%.2f)",
            description, startPrice, minIncrement
        );
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public double getMinIncrement() {
        return minIncrement;
    }
}