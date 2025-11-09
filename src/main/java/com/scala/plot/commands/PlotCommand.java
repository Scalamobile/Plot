package com.scala.plot.commands;

import com.scala.plot.PlotPlugin;
import com.scala.plot.managers.WorldEditManager;
import com.scala.plot.model.Plot;
import com.scala.plot.model.PlotFlag;
import com.scala.plot.utils.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PlotCommand implements CommandExecutor, TabCompleter {
    
    private final PlotPlugin plugin;
    private final WorldEditManager worldEditManager;
    
    public PlotCommand(PlotPlugin plugin) {
        this.plugin = plugin;
        this.worldEditManager = new WorldEditManager(plugin);
    }
    
    public WorldEditManager getWorldEditManager() {
        return worldEditManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "claim":
                handleClaim(player);
                break;
            case "info":
                handleInfo(player);
                break;
            case "add":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /plot add <player>");
                    return true;
                }
                handleAdd(player, args[1]);
                break;
            case "remove":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /plot remove <player>");
                    return true;
                }
                handleRemove(player, args[1]);
                break;
            case "flag":
                if (args.length < 4 || !args[1].equalsIgnoreCase("set")) {
                    player.sendMessage(ChatColor.RED + "Usage: /plot flag set <flag> <value>");
                    return true;
                }
                handleFlag(player, args[2], args[3]);
                break;
            case "reset":
                handleReset(player);
                break;
            case "tp":
            case "teleport":
                handleTeleport(player);
                break;
            case "admin":
                if (!player.hasPermission("plot.admin")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use admin commands!");
                    return true;
                }
                if (args.length < 2) {
                    sendAdminHelp(player);
                    return true;
                }
                handleAdmin(player, args);
                break;
            case "pos1":
                handlePos1(player);
                break;
            case "pos2":
                handlePos2(player);
                break;
            case "set":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /plot set <material>");
                    return true;
                }
                handleSet(player, args[1]);
                break;
            case "walls":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /plot walls <material>");
                    return true;
                }
                handleWalls(player, args[1]);
                break;
            case "fill":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /plot fill <material>");
                    return true;
                }
                handleFill(player, args[1]);
                break;
            case "copy":
                handleCopy(player);
                break;
            case "paste":
                handlePaste(player);
                break;
            case "undo":
                handleUndo(player);
                break;
            case "wand":
                handleWand(player);
                break;
            default:
                sendHelp(player);
                break;
        }
        
        return true;
    }
    
    private void handleClaim(Player player) {
        // Check if player already has a plot
        List<Plot> playerPlots = plugin.getPlotManager().getPlayerPlots(player.getUniqueId());
        if (!playerPlots.isEmpty()) {
            player.sendMessage(ChatColor.RED + "You already have a plot! Use /plot tp to teleport to it.");
            return;
        }
        
        Plot nearestPlot = plugin.getPlotManager().findNearestUnclaimedPlot(player.getLocation());
        
        if (nearestPlot == null) {
            player.sendMessage(ChatColor.RED + "No unclaimed plots available nearby!");
            return;
        }
        
        nearestPlot.setOwner(player.getUniqueId());
        plugin.getPlotManager().savePlots();
        
        Location center = plugin.getPlotManager().getPlotCenter(nearestPlot.getId());
        SchedulerUtil.teleportAsync(plugin, player, center, () -> {
            player.sendMessage(ChatColor.GREEN + "Plot " + nearestPlot.getId() + " claimed successfully!");
            player.sendMessage(ChatColor.GRAY + "You have been teleported to your plot.");
        });
    }
    
    private void handleInfo(Player player) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());
        
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "You are not standing in a plot!");
            return;
        }
        
        player.sendMessage(ChatColor.GOLD + "=== Plot Info ===");
        player.sendMessage(ChatColor.YELLOW + "ID: " + ChatColor.WHITE + plot.getId());
        
        if (plot.hasOwner()) {
            String ownerName = Bukkit.getOfflinePlayer(plot.getOwner()).getName();
            player.sendMessage(ChatColor.YELLOW + "Owner: " + ChatColor.WHITE + ownerName);
            
            if (!plot.getMembers().isEmpty()) {
                StringBuilder members = new StringBuilder();
                for (UUID memberId : plot.getMembers()) {
                    if (members.length() > 0) members.append(", ");
                    members.append(Bukkit.getOfflinePlayer(memberId).getName());
                }
                player.sendMessage(ChatColor.YELLOW + "Members: " + ChatColor.WHITE + members);
            }
            
            player.sendMessage(ChatColor.YELLOW + "Flags:");
            for (PlotFlag flag : PlotFlag.values()) {
                boolean value = plot.getFlag(flag);
                player.sendMessage(ChatColor.GRAY + "  - " + flag.getName() + ": " + 
                    (value ? ChatColor.GREEN + "true" : ChatColor.RED + "false"));
            }
        } else {
            player.sendMessage(ChatColor.YELLOW + "Owner: " + ChatColor.WHITE + "None (unclaimed)");
        }
    }
    
    private void handleAdd(Player player, String targetName) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());
        
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "You are not standing in a plot!");
            return;
        }
        
        if (!plot.isOwner(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You don't own this plot!");
            return;
        }
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        
        if (plot.isMember(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "This player is already a member!");
            return;
        }
        
        plot.addMember(target.getUniqueId());
        plugin.getPlotManager().savePlots();
        
        player.sendMessage(ChatColor.GREEN + target.getName() + " has been added to the plot!");
        target.sendMessage(ChatColor.GREEN + "You have been added to plot " + plot.getId() + " by " + player.getName());
    }
    
    private void handleRemove(Player player, String targetName) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());
        
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "You are not standing in a plot!");
            return;
        }
        
        if (!plot.isOwner(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You don't own this plot!");
            return;
        }
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        
        if (!plot.isMember(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "This player is not a member!");
            return;
        }
        
        plot.removeMember(target.getUniqueId());
        plugin.getPlotManager().savePlots();
        
        player.sendMessage(ChatColor.GREEN + target.getName() + " has been removed from the plot!");
        target.sendMessage(ChatColor.YELLOW + "You have been removed from plot " + plot.getId());
    }
    
    private void handleFlag(Player player, String flagName, String value) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());
        
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "You are not standing in a plot!");
            return;
        }
        
        if (!plot.isOwner(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You don't own this plot!");
            return;
        }
        
        PlotFlag flag = PlotFlag.fromString(flagName);
        if (flag == null) {
            player.sendMessage(ChatColor.RED + "Invalid flag! Available flags: break, place");
            return;
        }
        
        boolean boolValue;
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes")) {
            boolValue = true;
        } else if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no")) {
            boolValue = false;
        } else {
            player.sendMessage(ChatColor.RED + "Invalid value! Use true or false.");
            return;
        }
        
        plot.setFlag(flag, boolValue);
        plugin.getPlotManager().savePlots();
        
        player.sendMessage(ChatColor.GREEN + "Flag " + flag.getName() + " set to " + boolValue);
    }
    
    private void handleReset(Player player) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());
        
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "You are not standing in a plot!");
            return;
        }
        
        if (!plot.isOwner(player.getUniqueId()) && !player.hasPermission("plot.admin")) {
            player.sendMessage(ChatColor.RED + "You don't own this plot!");
            return;
        }
        
        plugin.getPlotManager().resetPlot(plot);
        player.sendMessage(ChatColor.GREEN + "Plot has been reset!");
    }
    
    private void handleTeleport(Player player) {
        List<Plot> playerPlots = plugin.getPlotManager().getPlayerPlots(player.getUniqueId());
        
        if (playerPlots.isEmpty()) {
            player.sendMessage(ChatColor.RED + "You don't have a plot! Use /plot claim to get one.");
            return;
        }
        
        Plot plot = playerPlots.get(0);
        Location center = plugin.getPlotManager().getPlotCenter(plot.getId());
        SchedulerUtil.teleportAsync(plugin, player, center, () -> {
            player.sendMessage(ChatColor.GREEN + "Teleported to your plot!");
        });
    }
    
    private void handleAdmin(Player player, String[] args) {
        switch (args[1].toLowerCase()) {
            case "reset":
                handleAdminReset(player);
                break;
            case "setowner":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /plot admin setowner <player>");
                    return;
                }
                handleAdminSetOwner(player, args[2]);
                break;
            case "delete":
                handleAdminDelete(player);
                break;
            default:
                sendAdminHelp(player);
                break;
        }
    }
    
    private void handleAdminReset(Player player) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());
        
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "You are not standing in a plot!");
            return;
        }
        
        plugin.getPlotManager().resetPlot(plot);
        player.sendMessage(ChatColor.GREEN + "Plot has been reset by admin!");
    }
    
    private void handleAdminSetOwner(Player player, String targetName) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());
        
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "You are not standing in a plot!");
            return;
        }
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        
        plot.setOwner(target.getUniqueId());
        plugin.getPlotManager().savePlots();
        
        player.sendMessage(ChatColor.GREEN + "Plot owner set to " + target.getName());
        target.sendMessage(ChatColor.GREEN + "You have been given ownership of plot " + plot.getId() + " by an admin!");
    }
    
    private void handleAdminDelete(Player player) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());
        
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "You are not standing in a plot!");
            return;
        }
        
        if (!plot.hasOwner()) {
            player.sendMessage(ChatColor.RED + "This plot is already unclaimed!");
            return;
        }
        
        plugin.getPlotManager().resetPlot(plot);
        player.sendMessage(ChatColor.GREEN + "Plot deleted and reset!");
    }
    
    private void handlePos1(Player player) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());
        
        if (plot == null || !plot.canBuild(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You can only set positions in your own plot!");
            return;
        }
        
        worldEditManager.setPos1(player, player.getLocation());
        player.sendMessage(ChatColor.GREEN + "Position 1 set to " + 
            player.getLocation().getBlockX() + ", " + 
            player.getLocation().getBlockY() + ", " + 
            player.getLocation().getBlockZ());
    }
    
    private void handlePos2(Player player) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());
        
        if (plot == null || !plot.canBuild(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You can only set positions in your own plot!");
            return;
        }
        
        worldEditManager.setPos2(player, player.getLocation());
        player.sendMessage(ChatColor.GREEN + "Position 2 set to " + 
            player.getLocation().getBlockX() + ", " + 
            player.getLocation().getBlockY() + ", " + 
            player.getLocation().getBlockZ());
        
        if (worldEditManager.hasSelection(player)) {
            int size = worldEditManager.getSelectionSize(player);
            player.sendMessage(ChatColor.GRAY + "Selection size: " + size + " blocks");
        }
    }
    
    private void handleSet(Player player, String materialName) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());
        
        if (plot == null || !plot.canBuild(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You can only use WorldEdit commands in your own plot!");
            return;
        }
        
        if (!worldEditManager.hasSelection(player)) {
            player.sendMessage(ChatColor.RED + "You need to select an area first! Use /plot pos1 and /plot pos2");
            return;
        }
        
        if (!worldEditManager.isSelectionInPlot(player, plot)) {
            player.sendMessage(ChatColor.RED + "Your selection must be entirely within your plot!");
            return;
        }
        
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid material: " + materialName);
            return;
        }
        
        int count = worldEditManager.setBlocks(player, material);
        player.sendMessage(ChatColor.GREEN + "Set " + count + " blocks to " + material.name());
    }
    
    private void handleWalls(Player player, String materialName) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());
        
        if (plot == null || !plot.canBuild(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You can only use this command in your own plot!");
            return;
        }
        
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid material: " + materialName);
            return;
        }
        
        player.sendMessage(ChatColor.YELLOW + "Setting plot walls... This may take a moment.");
        int count = worldEditManager.setWalls(player, plot, material);
        player.sendMessage(ChatColor.GREEN + "Set " + count + " blocks to " + material.name());
    }
    
    private void handleFill(Player player, String materialName) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());
        
        if (plot == null || !plot.canBuild(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You can only use this command in your own plot!");
            return;
        }
        
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid material: " + materialName);
            return;
        }
        
        player.sendMessage(ChatColor.YELLOW + "Filling plot... This may take a moment.");
        int count = worldEditManager.fillPlot(player, plot, material);
        player.sendMessage(ChatColor.GREEN + "Filled " + count + " blocks with " + material.name());
    }
    
    private void handleCopy(Player player) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());
        
        if (plot == null || !plot.canBuild(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You can only use WorldEdit commands in your own plot!");
            return;
        }
        
        if (!worldEditManager.hasSelection(player)) {
            player.sendMessage(ChatColor.RED + "You need to select an area first! Use /plot pos1 and /plot pos2");
            return;
        }
        
        if (!worldEditManager.isSelectionInPlot(player, plot)) {
            player.sendMessage(ChatColor.RED + "Your selection must be entirely within your plot!");
            return;
        }
        
        worldEditManager.copy(player);
        int size = worldEditManager.getSelectionSize(player);
        player.sendMessage(ChatColor.GREEN + "Copied " + size + " blocks to clipboard");
    }
    
    private void handlePaste(Player player) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());
        
        if (plot == null || !plot.canBuild(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You can only paste in your own plot!");
            return;
        }
        
        int count = worldEditManager.paste(player, player.getLocation());
        if (count == 0) {
            player.sendMessage(ChatColor.RED + "Your clipboard is empty! Use /plot copy first.");
        } else {
            player.sendMessage(ChatColor.GREEN + "Pasted " + count + " blocks");
        }
    }
    
    private void handleUndo(Player player) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());
        
        if (plot == null || !plot.canBuild(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You can only undo in your own plot!");
            return;
        }
        
        if (worldEditManager.undo(player)) {
            player.sendMessage(ChatColor.GREEN + "Undo successful!");
        } else {
            player.sendMessage(ChatColor.RED + "Nothing to undo!");
        }
    }
    
    private void handleWand(Player player) {
        // Create the wand item
        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta meta = wand.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Plot Wand");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Left click: Set position 1");
            lore.add(ChatColor.GRAY + "Right click: Set position 2");
            meta.setLore(lore);
            wand.setItemMeta(meta);
        }
        
        player.getInventory().addItem(wand);
        player.sendMessage(ChatColor.GREEN + "You received the Plot Wand!");
        player.sendMessage(ChatColor.GRAY + "Left click to set position 1, right click to set position 2");
    }
    
    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Plot Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/plot claim" + ChatColor.GRAY + " - Claim a plot (limit: 1 per player)");
        player.sendMessage(ChatColor.YELLOW + "/plot tp" + ChatColor.GRAY + " - Teleport to your plot");
        player.sendMessage(ChatColor.YELLOW + "/plot info" + ChatColor.GRAY + " - View plot information");
        player.sendMessage(ChatColor.YELLOW + "/plot add <player>" + ChatColor.GRAY + " - Add a player to your plot");
        player.sendMessage(ChatColor.YELLOW + "/plot remove <player>" + ChatColor.GRAY + " - Remove a player from your plot");
        player.sendMessage(ChatColor.YELLOW + "/plot flag set <flag> <value>" + ChatColor.GRAY + " - Set plot flags");
        player.sendMessage(ChatColor.YELLOW + "/plot reset" + ChatColor.GRAY + " - Reset your plot");
        player.sendMessage(ChatColor.GOLD + "=== WorldEdit Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/plot pos1" + ChatColor.GRAY + " - Set first position");
        player.sendMessage(ChatColor.YELLOW + "/plot pos2" + ChatColor.GRAY + " - Set second position");
        player.sendMessage(ChatColor.YELLOW + "/plot set <material>" + ChatColor.GRAY + " - Set selected area to material");
        player.sendMessage(ChatColor.YELLOW + "/plot walls <material>" + ChatColor.GRAY + " - Set plot walls");
        player.sendMessage(ChatColor.YELLOW + "/plot fill <material>" + ChatColor.GRAY + " - Fill entire plot");
        player.sendMessage(ChatColor.YELLOW + "/plot copy" + ChatColor.GRAY + " - Copy selection");
        player.sendMessage(ChatColor.YELLOW + "/plot paste" + ChatColor.GRAY + " - Paste copied blocks");
        player.sendMessage(ChatColor.YELLOW + "/plot undo" + ChatColor.GRAY + " - Undo last action");
        player.sendMessage(ChatColor.YELLOW + "/plot wand" + ChatColor.GRAY + " - Get the selection wand");
        if (player.hasPermission("plot.admin")) {
            player.sendMessage(ChatColor.YELLOW + "/plot admin" + ChatColor.GRAY + " - Admin commands");
        }
    }
    
    private void sendAdminHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Plot Admin Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/plot admin reset" + ChatColor.GRAY + " - Reset any plot");
        player.sendMessage(ChatColor.YELLOW + "/plot admin setowner <player>" + ChatColor.GRAY + " - Set plot owner");
        player.sendMessage(ChatColor.YELLOW + "/plot admin delete" + ChatColor.GRAY + " - Delete and reset a plot");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("claim", "tp", "info", "add", "remove", "flag", "reset",
                "pos1", "pos2", "set", "walls", "fill", "copy", "paste", "undo", "wand"));
            if (sender.hasPermission("plot.admin")) {
                completions.add("admin");
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            } else if (args[0].equalsIgnoreCase("flag")) {
                completions.add("set");
            } else if (args[0].equalsIgnoreCase("admin") && sender.hasPermission("plot.admin")) {
                completions.addAll(Arrays.asList("reset", "setowner", "delete"));
            } else if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("walls") || 
                       args[0].equalsIgnoreCase("fill")) {
                // Add common materials
                completions.addAll(Arrays.asList("STONE", "GRASS_BLOCK", "DIRT", "COBBLESTONE", 
                    "WOOD", "GLASS", "SAND", "GRAVEL", "GOLD_BLOCK", "IRON_BLOCK", 
                    "DIAMOND_BLOCK", "EMERALD_BLOCK", "QUARTZ_BLOCK", "BRICK", "STONE_BRICKS"));
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("flag") && args[1].equalsIgnoreCase("set")) {
                completions.add("break");
                completions.add("place");
            } else if (args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("setowner")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("flag") && args[1].equalsIgnoreCase("set")) {
            completions.add("true");
            completions.add("false");
        }
        
        return completions;
    }
}
