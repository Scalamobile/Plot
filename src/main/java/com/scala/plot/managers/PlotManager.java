package com.scala.plot.managers;

import com.scala.plot.PlotPlugin;
import com.scala.plot.model.Plot;
import com.scala.plot.model.PlotId;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlotManager {
    
    private static final int PLOT_SIZE = 100;
    private static final int ROAD_SIZE = 2;
    private static final int TOTAL_SIZE = PLOT_SIZE + ROAD_SIZE;
    
    private final PlotPlugin plugin;
    private final Map<PlotId, Plot> plots;
    private final File dataFile;
    
    public PlotManager(PlotPlugin plugin) {
        this.plugin = plugin;
        this.plots = new HashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "plots.yml");
        loadPlots();
    }
    
    public Plot getPlot(PlotId id) {
        return plots.computeIfAbsent(id, Plot::new);
    }
    
    public Plot getPlotAt(Location location) {
        PlotId id = getPlotIdAt(location);
        return id != null ? getPlot(id) : null;
    }
    
    public PlotId getPlotIdAt(Location location) {
        int x = location.getBlockX();
        int z = location.getBlockZ();
        
        // Check if we're on a road
        int modX = Math.floorMod(x, TOTAL_SIZE);
        int modZ = Math.floorMod(z, TOTAL_SIZE);
        
        if (modX >= PLOT_SIZE || modZ >= PLOT_SIZE) {
            return null; // On a road
        }
        
        // Calculate plot coordinates
        int plotX = Math.floorDiv(x, TOTAL_SIZE);
        int plotZ = Math.floorDiv(z, TOTAL_SIZE);
        
        return new PlotId(plotX, plotZ);
    }
    
    public Location getPlotCenter(PlotId id) {
        int worldX = id.getX() * TOTAL_SIZE + PLOT_SIZE / 2;
        int worldZ = id.getZ() * TOTAL_SIZE + PLOT_SIZE / 2;
        return new Location(plugin.getServer().getWorlds().get(0), worldX, 64, worldZ);
    }
    
    public Plot findNearestUnclaimedPlot(Location location) {
        PlotId startId = getPlotIdAt(location);
        if (startId == null) {
            startId = new PlotId(0, 0);
        }
        
        // Spiral search for unclaimed plot
        int maxRadius = 100;
        for (int radius = 0; radius <= maxRadius; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) == radius || Math.abs(dz) == radius) {
                        PlotId id = new PlotId(startId.getX() + dx, startId.getZ() + dz);
                        Plot plot = getPlot(id);
                        if (!plot.hasOwner()) {
                            return plot;
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    public List<Plot> getPlayerPlots(UUID player) {
        List<Plot> playerPlots = new ArrayList<>();
        for (Plot plot : plots.values()) {
            if (plot.isOwner(player)) {
                playerPlots.add(plot);
            }
        }
        return playerPlots;
    }
    
    public void resetPlot(Plot plot) {
        plot.reset();
        savePlots();
    }
    
    public void savePlots() {
        try {
            if (!dataFile.exists()) {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            }
            
            FileConfiguration config = new YamlConfiguration();
            
            for (Map.Entry<PlotId, Plot> entry : plots.entrySet()) {
                Plot plot = entry.getValue();
                if (plot.hasOwner() || !plot.getMembers().isEmpty()) {
                    config.set("plots." + plot.getId().toString(), plot.serialize());
                }
            }
            
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save plots: " + e.getMessage());
        }
    }
    
    public void saveAll() {
        savePlots();
    }
    
    private void loadPlots() {
        if (!dataFile.exists()) {
            return;
        }
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
            
            if (config.contains("plots")) {
                for (String key : config.getConfigurationSection("plots").getKeys(false)) {
                    String path = "plots." + key;
                    Map<String, Object> data = new HashMap<>();
                    
                    // Manually extract data to avoid MemorySection issues
                    data.put("id", config.getString(path + ".id"));
                    if (config.contains(path + ".owner")) {
                        data.put("owner", config.getString(path + ".owner"));
                    }
                    if (config.contains(path + ".members")) {
                        data.put("members", config.getStringList(path + ".members"));
                    }
                    if (config.contains(path + ".flags")) {
                        Map<String, Object> flags = new HashMap<>();
                        for (String flagKey : config.getConfigurationSection(path + ".flags").getKeys(false)) {
                            flags.put(flagKey, config.getBoolean(path + ".flags." + flagKey));
                        }
                        data.put("flags", flags);
                    }
                    
                    Plot plot = Plot.deserialize(data);
                    plots.put(plot.getId(), plot);
                }
            }
            
            plugin.getLogger().info("Loaded " + plots.size() + " plots");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load plots: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static int getPlotSize() {
        return PLOT_SIZE;
    }
    
    public static int getRoadSize() {
        return ROAD_SIZE;
    }
    
    public static int getTotalSize() {
        return TOTAL_SIZE;
    }
}
