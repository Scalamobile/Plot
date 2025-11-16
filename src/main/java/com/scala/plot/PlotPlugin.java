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
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;

import java.io.File;


public class PlotPlugin extends JavaPlugin {

    private static PlotPlugin instance;
    private PlotManager plotManager;
    private FileConfiguration lang;

    public FileConfiguration getLang() {
        return lang;
    }


    public void loadLang(String langCode) {
        File langFile = new File(getDataFolder(), "lang/" + langCode + ".yml");


        if (!langCode.endsWith(".yml")) {
            langCode += ".yml";
        }

        // ora langCode è sempre corretto
        if (!new File(getDataFolder(), "lang/" + langCode).exists()) {
            saveResource("lang/" + langCode, false);
        }

        lang = YamlConfiguration.loadConfiguration(
                new File(getDataFolder(), "lang/" + langCode)
        );
        this.lang = YamlConfiguration.loadConfiguration(langFile);

    }





    @Override
    public void onEnable() {
        instance = this;


        
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
        getLogger().info("Language: " + getConfig().getString("lang"));
        getLogger().info("");

        saveDefaultConfig();
        loadLang(getConfig().getString("lang", "en_US"));


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
