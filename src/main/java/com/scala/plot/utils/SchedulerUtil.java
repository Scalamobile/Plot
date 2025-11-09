package com.scala.plot.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

/**
 * Utility class for scheduling tasks that works with both Bukkit and Folia
 */
public class SchedulerUtil {
    
    private static final boolean IS_FOLIA;
    
    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        IS_FOLIA = folia;
    }
    
    public static boolean isFolia() {
        return IS_FOLIA;
    }
    
    /**
     * Run a task on the main thread (or region thread in Folia)
     */
    public static void run(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    
    /**
     * Run a task later on the main thread (or region thread in Folia)
     */
    public static void runLater(Plugin plugin, Runnable task, long delay) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delay);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }
    
    /**
     * Run a task asynchronously
     */
    public static void runAsync(Plugin plugin, Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }
    
    /**
     * Run a task at a specific location (for Folia regional scheduling)
     */
    public static void runAtLocation(Plugin plugin, Location location, Runnable task) {
        if (IS_FOLIA) {
            Bukkit.getRegionScheduler().run(plugin, location, scheduledTask -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    
    /**
     * Run a task at a specific location with delay (for Folia regional scheduling)
     */
    public static void runAtLocationLater(Plugin plugin, Location location, Runnable task, long delay) {
        if (IS_FOLIA) {
            Bukkit.getRegionScheduler().runDelayed(plugin, location, scheduledTask -> task.run(), delay);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }
    
    /**
     * Run a task for a specific entity (for Folia entity scheduling)
     */
    public static void runAtEntity(Plugin plugin, Entity entity, Runnable task) {
        if (IS_FOLIA) {
            entity.getScheduler().run(plugin, scheduledTask -> task.run(), null);
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    
    /**
     * Teleport an entity (Folia-safe)
     */
    public static void teleportAsync(Plugin plugin, Entity entity, Location location, Runnable callback) {
        if (IS_FOLIA) {
            entity.teleportAsync(location).thenAccept(result -> {
                if (callback != null && result) {
                    runAtLocation(plugin, location, callback);
                }
            });
        } else {
            entity.teleport(location);
            if (callback != null) {
                callback.run();
            }
        }
    }
}
