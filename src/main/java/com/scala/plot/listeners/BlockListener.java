package com.scala.plot.listeners;

import com.scala.plot.PlotPlugin;
import com.scala.plot.model.Plot;
import com.scala.plot.model.PlotFlag;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener implements Listener {
    
    private final PlotPlugin plugin;
    private String plotWorldName;
    private FileConfiguration lang;

    public BlockListener(PlotPlugin plugin) {
        this.plugin = plugin;
        this.plotWorldName = plugin.getConfig().getString("plot_world", "plotworld");
        this.lang = plugin.getLang();
    }

    public String t(String path) {
        return ChatColor.translateAlternateColorCodes('&', lang.getString(path, path));
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!player.getWorld().getName().equalsIgnoreCase(plotWorldName)) {
            return;
        }

        Plot plot = plugin.getPlotManager().getPlotAt(event.getBlock().getLocation());
        
        if (plot == null) {
            // On road, deny
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot break blocks on roads!");
            return;
        }
        
        // Check if player can build
        if (plot.canBuild(player.getUniqueId())) {
            return; // Owner or member can always build
        }
        
        // Check flag for non-members
        if (!plot.getFlag(PlotFlag.BREAK)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot break blocks in this plot!");
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (!player.getWorld().getName().equalsIgnoreCase(plotWorldName)) {
            return;
        }

        Plot plot = plugin.getPlotManager().getPlotAt(event.getBlock().getLocation());
        
        if (plot == null) {
            // On road, deny
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot place blocks on roads!");
            return;
        }
        
        // Check if player can build
        if (plot.canBuild(player.getUniqueId())) {
            return; // Owner or member can always build
        }
        
        // Check flag for non-members
        if (!plot.getFlag(PlotFlag.PLACE)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot place blocks in this plot!");
        }
    }
}
