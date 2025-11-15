package lk.com.pos.privateclasses;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotificationScheduler {
    private static final Logger LOGGER = Logger.getLogger(NotificationScheduler.class.getName());
    private final ScheduledExecutorService scheduler;
    private final Notification notification;
    private boolean isRunning = false;

    public NotificationScheduler() {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.notification = new Notification();
    }

    public void startScheduler() {
        if (isRunning) {
            LOGGER.warning("Notification scheduler is already running");
            return;
        }

        LOGGER.info("Starting notification scheduler with validation...");

        // Schedule expired products check every 6 hours
        scheduler.scheduleAtFixedRate(
            this::checkExpiredProducts,
            0, // initial delay - start immediately
            6, // period
            TimeUnit.HOURS
        );

        // Schedule periodic notifications every 12 hours
        scheduler.scheduleAtFixedRate(
            this::checkPeriodicNotifications,
            0, // initial delay - start immediately
            12, // period
            TimeUnit.HOURS
        );

        // Schedule cleanup every 24 hours (daily)
        scheduler.scheduleAtFixedRate(
            this::cleanupOldNotifications,
            0, // initial delay - start immediately
            24, // period
            TimeUnit.HOURS
        );

        // Optional: Frequent checks every 30 minutes for critical alerts
        scheduler.scheduleAtFixedRate(
            this::checkFrequentNotifications,
            0, // initial delay - start immediately
            30, // period
            TimeUnit.MINUTES
        );

        isRunning = true;
        LOGGER.info("Notification scheduler started successfully with validation rules");
    }

    public void stopScheduler() {
        if (!isRunning) {
            LOGGER.warning("Notification scheduler is not running");
            return;
        }

        LOGGER.info("Stopping notification scheduler...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        isRunning = false;
        LOGGER.info("Notification scheduler stopped");
    }

    private void checkExpiredProducts() {
        try {
            LOGGER.info("Running scheduled expired products check (6-hour interval) - Only active stocks with quantity > 0");
            notification.checkExpiredProductsEvery6Hours();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in scheduled expired products check", e);
        }
    }

    private void checkPeriodicNotifications() {
        try {
            LOGGER.info("Running scheduled periodic notifications (12-hour interval) - Only active stocks");
            notification.checkPeriodicNotificationsEvery12Hours();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in scheduled periodic notifications check", e);
        }
    }

    private void cleanupOldNotifications() {
        try {
            LOGGER.info("Running scheduled cleanup of old notifications (daily)");
            Notification.clearOldNotifications();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in scheduled cleanup", e);
        }
    }

    private void checkFrequentNotifications() {
        try {
            LOGGER.info("Running frequent notifications check (30-minute interval) - Only active stocks");
            notification.checkFrequentNotificationsEveryHalfHour();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in frequent notifications check", e);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    // Shutdown hook for graceful shutdown
    public void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopScheduler));
    }
}