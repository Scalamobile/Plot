package com.scala.plot.listeners;

import com.scala.plot.PlotPlugin;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldListener implements Listener {
    
    private final PlotPlugin plugin;
    
    public WorldListener(PlotPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        
        // Set world rules for the main world
        if (world.getName().equals("world")) {
            world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(org.bukkit.GameRule.DO_MOB_SPAWNING, false);
            world.setTime(6000); // Noon
            
            plugin.getLogger().info("Plot world configured: Always day, no mob spawning");
        }
    }
    
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // Prevent all mob spawning in the world
        if (event.getEntity().getWorld().getName().equals("world")) {
            if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM &&
                event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
                event.setCancelled(true);
            }
        }
    }
}
