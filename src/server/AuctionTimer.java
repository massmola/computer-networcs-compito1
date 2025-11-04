package server;

import auction.Auction;
import java.util.concurrent.*;

/**
 * Manages the timing of an auction by scheduling a task that triggers when the auction duration expires.
 *
 * This class uses a ScheduledExecutorService to handle auction timers in a reliable and thread-safe way.
 * When the time runs out, a callback function is invoked (usually defined in ServerLogic)
 * to close the auction or perform any necessary finalization.
 */
public class AuctionTimer {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledTask;
    private long startTimeMillis;
    private long durationMillis;

    /**
     * Starts the timer for the given auction. When the duration ends, the provided callback is executed.
     *
     * @param auction    The auction object containing duration and item details.
     * @param onTimeout  The callback to run when the auction duration expires.
     */
    public void startTimer(Auction auction, Runnable onTimeout) {
        // Store start time and total duration (converted to milliseconds)
        startTimeMillis = System.currentTimeMillis();
        durationMillis = TimeUnit.MINUTES.toMillis(auction.getDurationInMinutes());

        System.out.println("Auction for " + auction.getItem().getName() + " is open for "
                + auction.getDurationInMinutes() + " minutes.");

        // Schedule the task that will execute when the time expires
        scheduledTask = scheduler.schedule(() -> {
            System.out.println("⏰ Time ended for the item: " + auction.getItem().getName());
            onTimeout.run(); // Execute the callback
        }, auction.getDurationInMinutes(), TimeUnit.MINUTES);
    }

    /**
     * Returns the remaining time before the auction ends, in minutes.
     *
     * @return Remaining time in minutes, or 0 if the timer has expired or has not started.
     */
    public double getRemainingTime() {
        if (scheduledTask == null || scheduledTask.isDone()) {
            return 0.0;
        }
        long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
        long remainingMillis = durationMillis - elapsedMillis;
        return Math.max(0, remainingMillis) / 60000.0; // convert ms → minutes
    }
}
