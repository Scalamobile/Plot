package com.scala.plot;

import com.scala.plot.commands.PlotCommand;
import com.scala.plot.generator.PlotWorldGenerator;
import com.scala.plot.listeners.PlayerMoveListener;
import com.scala.plot.listeners.BlockListener;
import com.scala.plot.listeners.VoidListener;
import com.scala.plot.listeners.WandListener;
import com.scala.plot.listeners.WorldListener;
import com.scala.plot.managers.PlotManager;
import com.scala.plot.VersionChecker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;
import com.scala.scalaAnalytics.AnalyticsManager;


public class PlotPlugin extends JavaPlugin {

    private AnalyticsManager analytics;
    private static PlotPlugin instance;
    private PlotManager plotManager;
    
    @Override
    public void onEnable() {
        instance = this;

        analytics = AnalyticsManager.builder()
                .plugin(this)
                .apiKey("27a3bc001bc66ce302fa39ae9b2e08ac")
                .updateInterval(60) // 5 minuti
                .build();
        analytics.start();

        
        // Print banner
        getLogger().info("");
        getLogger().info("██████╗ ██╗      ██████╗ ████████╗");
        getLogger().info("██╔══██╗██║     ██╔═══██╗╚══██╔══╝");
        getLogger().info("██████╔╝██║     ██║   ██║   ██║   ");
        getLogger().info("██╔═══╝ ██║     ██║   ██║   ██║   ");
        getLogger().info("██║     ███████╗╚██████╔╝   ██║   ");
        getLogger().info("╚═╝     ╚══════╝ ╚═════╝    ╚═╝   ");
        getLogger().info("");
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info("Author: " + getDescription().getAuthors().get(0));
        getLogger().info("");

        //check version
        VersionChecker.checkVersion();

        // Initialize bStats metrics
        int pluginId = 27812;
        Metrics metrics = new Metrics(this, pluginId);

        // Initialize managers
        plotManager = new PlotManager(this);
        
        // Register commands
        PlotCommand plotCommand = new PlotCommand(this);
        getCommand("plot").setExecutor(plotCommand);
        getCommand("plot").setTabCompleter(plotCommand);
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);
        getServer().getPluginManager().registerEvents(new VoidListener(this), this);
        getServer().getPluginManager().registerEvents(new WandListener(this, plotCommand.getWorldEditManager()), this);
        
        // Configure main world if already loaded
        if (getServer().getWorld("world") != null) {
            org.bukkit.World world = getServer().getWorld("world");
            world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(org.bukkit.GameRule.DO_MOB_SPAWNING, false);
            world.setTime(6000); // Noon
        }
        
        getLogger().info("Plot plugin enabled!");
    }
    
    @Override
    public void onDisable() {
        if (analytics != null) {
            analytics.stop();
        }

        if (plotManager != null) {
            plotManager.saveAll();
        }
        
        getLogger().info("");
        getLogger().info("██████╗ ██╗      ██████╗ ████████╗");
        getLogger().info("██╔══██╗██║     ██╔═══██╗╚══██╔══╝");
        getLogger().info("██████╔╝██║     ██║   ██║   ██║   ");
        getLogger().info("██╔═══╝ ██║     ██║   ██║   ██║   ");
        getLogger().info("██║     ███████╗╚██████╔╝   ██║   ");
        getLogger().info("╚═╝     ╚══════╝ ╚═════╝    ╚═╝   ");
        getLogger().info("");
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info("Author: " + getDescription().getAuthors().get(0));
        getLogger().info("");
        getLogger().info("Plot plugin disabled!");
    }
    
    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new PlotWorldGenerator();
    }
    
    public static PlotPlugin getInstance() {
        return instance;
    }
    
    public PlotManager getPlotManager() {
        return plotManager;
    }



}
