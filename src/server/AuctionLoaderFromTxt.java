package server;

import auction.Auction;

import java.io.*;
import java.util.*;

import auction.Item;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class AuctionLoaderFromTxt {
private static final String auctionFilePath = "src/server/auctions.txt";

    /**
     * Simple class to load auctions from a plain text file.
     * Each line in the file must have the format:
     * name;startPrice;durationInSeconds;minIncrement
     */
    public class AuctionLoader {

        /**
         * Loads all auctions from the given file path.
         *
         * @param filePath the path to the auction file
         * @return a list of Auction objects
         */
        public static ArrayList<Auction> loadAuctions(String filePath) {
            ArrayList<Auction> auctions = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Ignore empty lines or comments
                    if (line.isBlank() || line.startsWith("#")) continue;

                    // Split fields by semicolon
                    String[] parts = line.split(";");
                    if (parts.length != 4) {
                        System.err.println("Skipping malformed line: " + line);
                        continue;
                    }

                    String name = parts[0].trim();
                    double startPrice = Double.parseDouble(parts[1].trim());
                    long duration = Long.parseLong(parts[2].trim());
                    double minIncrement = Double.parseDouble(parts[3].trim());

                    // Create item and auction
                    Item item = new Item(name, startPrice, minIncrement);
                    Auction auction = new Auction(item, duration);

                    auctions.add(auction);
                }

            } catch (IOException e) {
                System.err.println("Error reading auction file: " + e.getMessage());
            }

            return auctions;
        }
    }
}
