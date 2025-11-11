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
    
    public void start() {
        if (isRunning) {
            LOGGER.warning("Notification scheduler is already running");
            return;
        }
        
        // Schedule the notification check to run every 24 hours
        scheduler.scheduleAtFixedRate(() -> {
            try {
                LOGGER.info("Running scheduled notification check...");
                notification.checkAllNotifications();
                LOGGER.info("Scheduled notification check completed");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error in scheduled notification check", e);
            }
        }, 0, 24, TimeUnit.HOURS); // Initial delay 0, repeat every 24 hours
        
        isRunning = true;
        LOGGER.info("Notification scheduler started successfully");
    }
    
    public void stop() {
        if (!isRunning) {
            LOGGER.warning("Notification scheduler is not running");
            return;
        }
        
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    LOGGER.severe("Notification scheduler did not terminate properly");
                }
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        isRunning = false;
        LOGGER.info("Notification scheduler stopped successfully");
    }
    
    public void triggerManualCheck() {
        new Thread(() -> {
            try {
                LOGGER.info("Manual notification check triggered...");
                notification.checkAllNotifications();
                LOGGER.info("Manual notification check completed");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error in manual notification check", e);
            }
        }).start();
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    // Static method to easily trigger stock quantity change notifications from other classes
    public static void notifyStockChange(int stockId, int oldQty, int newQty) {
        Notification notification = new Notification();
        notification.checkStockQuantityChange(stockId, oldQty, newQty);
    }
    
    // Static method for quick manual check
    public static void triggerQuickCheck() {
        NotificationScheduler scheduler = new NotificationScheduler();
        scheduler.triggerManualCheck();
    }
}