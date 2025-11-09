package com.scala.plot.listeners;

import com.scala.plot.PlotPlugin;
import com.scala.plot.managers.WorldEditManager;
import com.scala.plot.model.Plot;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WandListener implements Listener {
    
    private final PlotPlugin plugin;
    private final WorldEditManager worldEditManager;
    
    public WandListener(PlotPlugin plugin, WorldEditManager worldEditManager) {
        this.plugin = plugin;
        this.worldEditManager = worldEditManager;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Check if player is holding the wand
        if (item == null || item.getType() != Material.STICK) {
            return;
        }
        
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }
        
        String displayName = item.getItemMeta().getDisplayName();
        if (!displayName.equals(ChatColor.GOLD + "Plot Wand")) {
            return;
        }
        
        // Check if player is in their plot
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());
        if (plot == null || !plot.canBuild(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You can only use the wand in your own plot!");
            event.setCancelled(true);
            return;
        }
        
        Action action = event.getAction();
        
        if (action == Action.LEFT_CLICK_BLOCK) {
            // Left click sets position 1
            event.setCancelled(true);
            worldEditManager.setPos1(player, event.getClickedBlock().getLocation());
            player.sendMessage(ChatColor.GREEN + "Position 1 set to " + 
                event.getClickedBlock().getX() + ", " + 
                event.getClickedBlock().getY() + ", " + 
                event.getClickedBlock().getZ());
                
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            // Right click sets position 2
            event.setCancelled(true);
            worldEditManager.setPos2(player, event.getClickedBlock().getLocation());
            player.sendMessage(ChatColor.GREEN + "Position 2 set to " + 
                event.getClickedBlock().getX() + ", " + 
                event.getClickedBlock().getY() + ", " + 
                event.getClickedBlock().getZ());
            
            if (worldEditManager.hasSelection(player)) {
                int size = worldEditManager.getSelectionSize(player);
                player.sendMessage(ChatColor.GRAY + "Selection size: " + size + " blocks");
            }
        }
    }
}
