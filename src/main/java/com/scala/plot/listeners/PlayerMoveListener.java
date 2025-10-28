package com.scala.plot.listeners;

import com.scala.plot.PlotPlugin;
import com.scala.plot.model.Plot;
import com.scala.plot.model.PlotId;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerMoveListener implements Listener {
    
    private final PlotPlugin plugin;
    private final Map<UUID, PlotId> lastPlot;
    
    public PlayerMoveListener(PlotPlugin plugin) {
        this.plugin = plugin;
        this.lastPlot = new HashMap<>();
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Only check if player moved to a different block
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        PlotId currentPlotId = plugin.getPlotManager().getPlotIdAt(event.getTo());
        PlotId previousPlotId = lastPlot.get(player.getUniqueId());
        
        // Check if player entered a different plot
        if (currentPlotId != null && !currentPlotId.equals(previousPlotId)) {
            Plot plot = plugin.getPlotManager().getPlot(currentPlotId);
            
            if (plot.hasOwner() && !plot.isOwner(player.getUniqueId())) {
                String ownerName = Bukkit.getOfflinePlayer(plot.getOwner()).getName();
                String message = ChatColor.YELLOW + "Plot owned by " + ChatColor.GOLD + ownerName;
                
                // Send action bar message
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
            }
            
            lastPlot.put(player.getUniqueId(), currentPlotId);
        } else if (currentPlotId == null && previousPlotId != null) {
            // Player left a plot (entered road)
            lastPlot.remove(player.getUniqueId());
        }
    }
}
