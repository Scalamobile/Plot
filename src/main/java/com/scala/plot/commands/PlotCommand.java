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
import org.bukkit.configuration.file.FileConfiguration;
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
    private FileConfiguration lang;

    public PlotCommand(PlotPlugin plugin) {
        this.plugin = plugin;
        this.worldEditManager = new WorldEditManager(plugin);
        this.lang = plugin.getLang();
    }

    public String t(String path) {
        return ChatColor.translateAlternateColorCodes('&', lang.getString(path, path));
    }

    public WorldEditManager getWorldEditManager() {
        return worldEditManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + t("console_execution"));
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
                    player.sendMessage(ChatColor.RED + t("add_command_usage"));
                    return true;
                }
                handleAdd(player, args[1]);
                break;
            case "remove":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + t("remove_command_usage"));
                    return true;
                }
                handleRemove(player, args[1]);
                break;
            case "flag":
                if (args.length < 4 || !args[1].equalsIgnoreCase("set")) {
                    player.sendMessage(ChatColor.RED + t("flag_command_usage"));
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
                    player.sendMessage(ChatColor.RED + t("no_perms"));
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
                    player.sendMessage(ChatColor.RED + t("set_command_usage"));
                    return true;
                }
                handleSet(player, args[1]);
                break;
            case "walls":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + t("walls_command_usage"));
                    return true;
                }
                handleWalls(player, args[1]);
                break;
            case "fill":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + t("fill_command_usage"));
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
        List<Plot> playerPlots = plugin.getPlotManager().getPlayerPlots(player.getUniqueId());
        if (!playerPlots.isEmpty()) {
            player.sendMessage(ChatColor.RED + t("claim_already_owned"));
            return;
        }

        Plot nearestPlot = plugin.getPlotManager().findNearestUnclaimedPlot(player.getLocation());

        if (nearestPlot == null) {
            player.sendMessage(ChatColor.RED + t("claim_no_plot"));
            return;
        }

        nearestPlot.setOwner(player.getUniqueId());
        plugin.getPlotManager().savePlots();

        Location center = plugin.getPlotManager().getPlotCenter(nearestPlot.getId());
        SchedulerUtil.teleportAsync(plugin, player, center, () -> {
            player.sendMessage(ChatColor.GREEN + t("claim_success").replace("{plot_id}", String.valueOf(nearestPlot.getId())));
            player.sendMessage(ChatColor.GRAY + t("claim_teleport"));
        });
    }

    private void handleInfo(Player player) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());

        if (plot == null) {
            player.sendMessage(ChatColor.RED + t("info_no_plot"));
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== Plot Info ===");
        player.sendMessage(ChatColor.YELLOW + "ID: " + ChatColor.WHITE + plot.getId());

        if (plot.hasOwner()) {
            String ownerName = Bukkit.getOfflinePlayer(plot.getOwner()).getName();
            player.sendMessage(ChatColor.YELLOW + t("info_owner").replace("{owner}", ownerName));

            if (!plot.getMembers().isEmpty()) {
                StringBuilder members = new StringBuilder();
                for (UUID memberId : plot.getMembers()) {
                    if (members.length() > 0) members.append(", ");
                    members.append(Bukkit.getOfflinePlayer(memberId).getName());
                }
                player.sendMessage(ChatColor.YELLOW + t("info_members").replace("{members}", members.toString()));
            }

            player.sendMessage(ChatColor.YELLOW + t("info_flags"));
            for (PlotFlag flag : PlotFlag.values()) {
                boolean value = plot.getFlag(flag);
                player.sendMessage(ChatColor.GRAY + "  - " + flag.getName() + ": " +
                        (value ? ChatColor.GREEN + t("info_flag_true").replace("{flag}", flag.getName())
                                : ChatColor.RED + t("info_flag_false").replace("{flag}", flag.getName())));
            }

        } else {
            player.sendMessage(ChatColor.YELLOW + t("info_owner_none"));
        }
    }

    private void handleAdd(Player player, String targetName) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());

        if (plot == null) {
            player.sendMessage(ChatColor.RED + t("info_no_plot"));
            return;
        }

        if (!plot.isOwner(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + t("reset_no_perms"));
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + t("add_player_not_found"));
            return;
        }

        if (plot.isMember(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + t("add_player_already_member"));
            return;
        }

        plot.addMember(target.getUniqueId());
        plugin.getPlotManager().savePlots();

        player.sendMessage(ChatColor.GREEN + t("add_player_success_owner").replace("{player}", target.getName()));
        target.sendMessage(ChatColor.GREEN + t("add_player_success_target")
                .replace("{plot_id}", String.valueOf(plot.getId()))
                .replace("{owner}", player.getName()));
    }

    private void handleRemove(Player player, String targetName) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());

        if (plot == null) {
            player.sendMessage(ChatColor.RED + t("info_no_plot"));
            return;
        }

        if (!plot.isOwner(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + t("reset_no_perms"));
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + t("remove_player_not_found"));
            return;
        }

        if (!plot.isMember(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + t("remove_player_not_member"));
            return;
        }

        plot.removeMember(target.getUniqueId());
        plugin.getPlotManager().savePlots();

        player.sendMessage(ChatColor.GREEN + t("remove_player_success_owner").replace("{player}", target.getName()));
        target.sendMessage(ChatColor.YELLOW + t("remove_player_success_target").replace("{plot_id}", String.valueOf(plot.getId())));
    }

    private void handleFlag(Player player, String flagName, String value) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());

        if (plot == null) {
            player.sendMessage(ChatColor.RED + t("info_no_plot"));
            return;
        }

        if (!plot.isOwner(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + t("reset_no_perms"));
            return;
        }

        PlotFlag flag = PlotFlag.fromString(flagName);
        if (flag == null) {
            player.sendMessage(ChatColor.RED + t("flag_invalid_flag"));
            return;
        }

        boolean boolValue;
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes")) {
            boolValue = true;
        } else if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no")) {
            boolValue = false;
        } else {
            player.sendMessage(ChatColor.RED + t("flag_invalid_value"));
            return;
        }

        plot.setFlag(flag, boolValue);
        plugin.getPlotManager().savePlots();

        player.sendMessage(ChatColor.GREEN + t("flag_set_success")
                .replace("{flag}", flag.getName())
                .replace("{value}", String.valueOf(boolValue)));
    }

    private void handleReset(Player player) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());

        if (plot == null) {
            player.sendMessage(ChatColor.RED + t("reset_no_plot"));
            return;
        }

        if (!plot.isOwner(player.getUniqueId()) && !player.hasPermission("plot.admin")) {
            player.sendMessage(ChatColor.RED + t("reset_no_perms"));
            return;
        }

        plugin.getPlotManager().resetPlot(plot);
        player.sendMessage(ChatColor.GREEN + t("reset_success"));
    }

    private void handleTeleport(Player player) {
        List<Plot> playerPlots = plugin.getPlotManager().getPlayerPlots(player.getUniqueId());

        if (playerPlots.isEmpty()) {
            player.sendMessage(ChatColor.RED + t("tp_no_plot"));
            return;
        }

        Plot plot = playerPlots.get(0);
        Location center = plugin.getPlotManager().getPlotCenter(plot.getId());
        SchedulerUtil.teleportAsync(plugin, player, center, () -> player.sendMessage(ChatColor.GREEN + t("tp_success")));
    }

    private void handleAdmin(Player player, String[] args) {
        switch (args[1].toLowerCase()) {
            case "reset":
                handleAdminReset(player);
                break;
            case "setowner":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + t("admin_usage_setowner"));
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
            player.sendMessage(ChatColor.RED + t("info_no_plot"));
            return;
        }

        plugin.getPlotManager().resetPlot(plot);
        player.sendMessage(ChatColor.GREEN + t("admin_reset_success"));
    }

    private void handleAdminSetOwner(Player player, String targetName) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());

        if (plot == null) {
            player.sendMessage(ChatColor.RED + t("info_no_plot"));
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + t("add_player_not_found"));
            return;
        }

        plot.setOwner(target.getUniqueId());
        plugin.getPlotManager().savePlots();

        player.sendMessage(ChatColor.GREEN + t("admin_setowner_success_owner").replace("{player}", target.getName()));
        target.sendMessage(ChatColor.GREEN + t("admin_setowner_success_target")
                .replace("{plot_id}", String.valueOf(plot.getId())));
    }

    private void handleAdminDelete(Player player) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());

        if (plot == null) {
            player.sendMessage(ChatColor.RED + t("info_no_plot"));
            return;
        }

        if (!plot.hasOwner()) {
            player.sendMessage(ChatColor.RED + t("admin_delete_no_owner"));
            return;
        }

        plugin.getPlotManager().resetPlot(plot);
        player.sendMessage(ChatColor.GREEN + t("admin_delete_success"));
    }

    private void handlePos1(Player player) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());

        if (plot == null || !plot.canBuild(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + t("pos1_no_perms"));
            return;
        }

        worldEditManager.setPos1(player, player.getLocation());
        player.sendMessage(ChatColor.GREEN + t("pos1_set")
                .replace("{x}", String.valueOf(player.getLocation().getBlockX()))
                .replace("{y}", String.valueOf(player.getLocation().getBlockY()))
                .replace("{z}", String.valueOf(player.getLocation().getBlockZ())));
    }

    private void handlePos2(Player player) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());

        if (plot == null || !plot.canBuild(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + t("pos2_no_perms"));
            return;
        }

        worldEditManager.setPos2(player, player.getLocation());
        player.sendMessage(ChatColor.GREEN + t("pos2_set")
                .replace("{x}", String.valueOf(player.getLocation().getBlockX()))
                .replace("{y}", String.valueOf(player.getLocation().getBlockY()))
                .replace("{z}", String.valueOf(player.getLocation().getBlockZ())));

        if (worldEditManager.hasSelection(player)) {
            int size = worldEditManager.getSelectionSize(player);
            player.sendMessage(ChatColor.GRAY + t("pos2_selection_size").replace("{size}", String.valueOf(size)));
        }
    }

    private void handleSet(Player player, String materialName) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());

        if (plot == null || !plot.canBuild(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + t("set_no_perms"));
            return;
        }

        if (!worldEditManager.hasSelection(player)) {
            player.sendMessage(ChatColor.RED + t("set_no_selection"));
            return;
        }

        if (!worldEditManager.isSelectionInPlot(player, plot)) {
            player.sendMessage(ChatColor.RED + t("set_out_of_plot"));
            return;
        }

        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + t("set_invalid_material").replace("{material}", materialName));
            return;
        }

        int count = worldEditManager.setBlocks(player, material);
        player.sendMessage(ChatColor.GREEN + t("set_success").replace("{count}", String.valueOf(count)).replace("{material}", material.name()));
    }

    private void handleWalls(Player player, String materialName) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());

        if (plot == null || !plot.canBuild(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + t("walls_no_perms"));
            return;
        }

        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + t("walls_invalid_material").replace("{material}", materialName));
            return;
        }

        player.sendMessage(ChatColor.YELLOW + t("walls_starting"));
        int count = worldEditManager.setWalls(player, plot, material);
        player.sendMessage(ChatColor.GREEN + t("walls_success").replace("{count}", String.valueOf(count)).replace("{material}", material.name()));
    }

    private void handleFill(Player player, String materialName) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());

        if (plot == null || !plot.canBuild(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + t("fill_no_perms"));
            return;
        }

        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + t("fill_invalid_material").replace("{material}", materialName));
            return;
        }

        player.sendMessage(ChatColor.YELLOW + t("fill_starting"));
        int count = worldEditManager.fillPlot(player, plot, material);
        player.sendMessage(ChatColor.GREEN + t("fill_success").replace("{count}", String.valueOf(count)).replace("{material}", material.name()));
    }

    private void handleCopy(Player player) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());

        if (plot == null || !plot.canBuild(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + t("copy_no_perms"));
            return;
        }

        if (!worldEditManager.hasSelection(player)) {
            player.sendMessage(ChatColor.RED + t("copy_no_selection"));
            return;
        }

        if (!worldEditManager.isSelectionInPlot(player, plot)) {
            player.sendMessage(ChatColor.RED + t("copy_out_of_plot"));
            return;
        }

        worldEditManager.copy(player);
        int size = worldEditManager.getSelectionSize(player);
        player.sendMessage(ChatColor.GREEN + t("copy_success").replace("{count}", String.valueOf(size)));
    }

    private void handlePaste(Player player) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());

        if (plot == null || !plot.canBuild(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + t("paste_no_perms"));
            return;
        }

        int count = worldEditManager.paste(player, player.getLocation());
        if (count == 0) {
            player.sendMessage(ChatColor.RED + t("paste_empty_clipboard"));
        } else {
            player.sendMessage(ChatColor.GREEN + t("paste_success").replace("{count}", String.valueOf(count)));
        }
    }

    private void handleUndo(Player player) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());

        if (plot == null || !plot.canBuild(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + t("undo_no_perms"));
            return;
        }

        if (worldEditManager.undo(player)) {
            player.sendMessage(ChatColor.GREEN + t("undo_success"));
        } else {
            player.sendMessage(ChatColor.RED + t("undo_nothing"));
        }
    }

    private void handleWand(Player player) {
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
        player.sendMessage(ChatColor.GREEN + t("wand_received"));
        player.sendMessage(ChatColor.GRAY + t("wand_instructions"));
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Plot Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/plot claim" + ChatColor.GRAY + " - " + t("claim_already_owned"));
        player.sendMessage(ChatColor.YELLOW + "/plot tp" + ChatColor.GRAY + " - Teletrasportati al tuo plot");
        player.sendMessage(ChatColor.YELLOW + "/plot info" + ChatColor.GRAY + " - Visualizza info del plot");
        player.sendMessage(ChatColor.YELLOW + "/plot add <player>" + ChatColor.GRAY + " - Aggiungi un giocatore al plot");
        player.sendMessage(ChatColor.YELLOW + "/plot remove <player>" + ChatColor.GRAY + " - Rimuovi un giocatore dal plot");
        player.sendMessage(ChatColor.YELLOW + "/plot flag set <flag> <value>" + ChatColor.GRAY + " - Imposta flag del plot");
        player.sendMessage(ChatColor.YELLOW + "/plot reset" + ChatColor.GRAY + " - Resetta il plot");
        player.sendMessage(ChatColor.GOLD + "=== WorldEdit Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/plot pos1" + ChatColor.GRAY + " - Imposta posizione 1");
        player.sendMessage(ChatColor.YELLOW + "/plot pos2" + ChatColor.GRAY + " - Imposta posizione 2");
        player.sendMessage(ChatColor.YELLOW + "/plot set <material>" + ChatColor.GRAY + " - Imposta area selezionata");
        player.sendMessage(ChatColor.YELLOW + "/plot walls <material>" + ChatColor.GRAY + " - Imposta muri del plot");
        player.sendMessage(ChatColor.YELLOW + "/plot fill <material>" + ChatColor.GRAY + " - Riempie il plot");
        player.sendMessage(ChatColor.YELLOW + "/plot copy" + ChatColor.GRAY + " - Copia selezione");
        player.sendMessage(ChatColor.YELLOW + "/plot paste" + ChatColor.GRAY + " - Incolla selezione");
        player.sendMessage(ChatColor.YELLOW + "/plot undo" + ChatColor.GRAY + " - Undo ultimo comando");
        player.sendMessage(ChatColor.YELLOW + "/plot wand" + ChatColor.GRAY + " - Ottieni bacchetta selezione");
        if (player.hasPermission("plot.admin")) {
            player.sendMessage(ChatColor.YELLOW + "/plot admin" + ChatColor.GRAY + " - Comandi admin");
        }
    }

    private void sendAdminHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Plot Admin Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/plot admin reset" + ChatColor.GRAY + " - Resetta qualsiasi plot");
        player.sendMessage(ChatColor.YELLOW + "/plot admin setowner <player>" + ChatColor.GRAY + " - Imposta proprietario");
        player.sendMessage(ChatColor.YELLOW + "/plot admin delete" + ChatColor.GRAY + " - Elimina e resetta plot");
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
