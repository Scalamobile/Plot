package com.scala.plot.listeners;

import com.scala.plot.PlotPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.ChatColor;

public class VoidListener implements Listener {
    private final PlotPlugin plugin;

    public VoidListener(PlotPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void VoidTrue(PlayerMoveEvent Player) {
        Player player = Player.getPlayer();
        Location playerLocation = player.getLocation();

        double y = playerLocation.getY();
        double x = playerLocation.getX();
        double z = playerLocation.getZ();

        if (y < 0) {
            player.sendMessage(ChatColor.RED + "You cant go to the void");
            player.teleport(new Location(Bukkit.getWorld("world"), x - 1, 64, z));
        }
    }
}
