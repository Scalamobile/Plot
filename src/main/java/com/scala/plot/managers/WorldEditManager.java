package com.scala.plot.managers;

import com.scala.plot.PlotPlugin;
import com.scala.plot.model.Plot;
import com.scala.plot.model.PlotId;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public class WorldEditManager {
    
    private final PlotPlugin plugin;
    private final Map<UUID, Location> pos1Map;
    private final Map<UUID, Location> pos2Map;
    private final Map<UUID, List<BlockData>> clipboardMap;
    private final Map<UUID, Stack<List<BlockData>>> undoMap;
    
    public WorldEditManager(PlotPlugin plugin) {
        this.plugin = plugin;
        this.pos1Map = new HashMap<>();
        this.pos2Map = new HashMap<>();
        this.clipboardMap = new HashMap<>();
        this.undoMap = new HashMap<>();
    }
    
    public void setPos1(Player player, Location location) {
        pos1Map.put(player.getUniqueId(), location.clone());
    }
    
    public void setPos2(Player player, Location location) {
        pos2Map.put(player.getUniqueId(), location.clone());
    }
    
    public Location getPos1(Player player) {
        return pos1Map.get(player.getUniqueId());
    }
    
    public Location getPos2(Player player) {
        return pos2Map.get(player.getUniqueId());
    }
    
    public boolean hasSelection(Player player) {
        return pos1Map.containsKey(player.getUniqueId()) && pos2Map.containsKey(player.getUniqueId());
    }
    
    public void clearSelection(Player player) {
        pos1Map.remove(player.getUniqueId());
        pos2Map.remove(player.getUniqueId());
    }
    
    public boolean isInPlot(Location location, Plot plot) {
        PlotId plotId = plugin.getPlotManager().getPlotIdAt(location);
        return plotId != null && plotId.equals(plot.getId());
    }
    
    public boolean isSelectionInPlot(Player player, Plot plot) {
        if (!hasSelection(player)) {
            return false;
        }
        
        Location pos1 = getPos1(player);
        Location pos2 = getPos2(player);
        
        // Check all corners of the selection
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        
        Location[] corners = {
            new Location(pos1.getWorld(), minX, pos1.getBlockY(), minZ),
            new Location(pos1.getWorld(), maxX, pos1.getBlockY(), minZ),
            new Location(pos1.getWorld(), minX, pos1.getBlockY(), maxZ),
            new Location(pos1.getWorld(), maxX, pos1.getBlockY(), maxZ)
        };
        
        for (Location corner : corners) {
            if (!isInPlot(corner, plot)) {
                return false;
            }
        }
        
        return true;
    }
    
    public int setBlocks(Player player, Material material) {
        if (!hasSelection(player)) {
            return 0;
        }
        
        Location pos1 = getPos1(player);
        Location pos2 = getPos2(player);
        
        List<BlockData> oldBlocks = new ArrayList<>();
        int count = 0;
        
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = pos1.getWorld().getBlockAt(x, y, z);
                    oldBlocks.add(new BlockData(block.getLocation().clone(), block.getType()));
                    block.setType(material);
                    count++;
                }
            }
        }
        
        addUndo(player, oldBlocks);
        return count;
    }
    
    public int fillPlot(Player player, Plot plot, Material material) {
        PlotId id = plot.getId();
        int plotSize = PlotManager.getPlotSize();
        int totalSize = PlotManager.getTotalSize();
        
        int startX = id.getX() * totalSize;
        int startZ = id.getZ() * totalSize;
        
        List<BlockData> oldBlocks = new ArrayList<>();
        int count = 0;
        
        for (int x = startX; x < startX + plotSize; x++) {
            for (int z = startZ; z < startZ + plotSize; z++) {
                for (int y = 1; y <= 64; y++) {
                    Block block = player.getWorld().getBlockAt(x, y, z);
                    if (block.getType() != Material.AIR) {
                        oldBlocks.add(new BlockData(block.getLocation().clone(), block.getType()));
                        block.setType(material);
                        count++;
                    }
                }
            }
        }
        
        addUndo(player, oldBlocks);
        return count;
    }
    
    public int setWalls(Player player, Plot plot, Material material) {
        PlotId id = plot.getId();
        int plotSize = PlotManager.getPlotSize();
        int totalSize = PlotManager.getTotalSize();
        
        int startX = id.getX() * totalSize;
        int startZ = id.getZ() * totalSize;
        int endX = startX + plotSize - 1;
        int endZ = startZ + plotSize - 1;
        
        List<BlockData> oldBlocks = new ArrayList<>();
        int count = 0;
        
        // Set all four walls
        for (int y = 1; y <= 64; y++) {
            // North wall (z = startZ)
            for (int x = startX; x <= endX; x++) {
                Block block = player.getWorld().getBlockAt(x, y, startZ);
                oldBlocks.add(new BlockData(block.getLocation().clone(), block.getType()));
                block.setType(material);
                count++;
            }
            
            // South wall (z = endZ)
            for (int x = startX; x <= endX; x++) {
                Block block = player.getWorld().getBlockAt(x, y, endZ);
                oldBlocks.add(new BlockData(block.getLocation().clone(), block.getType()));
                block.setType(material);
                count++;
            }
            
            // West wall (x = startX)
            for (int z = startZ + 1; z < endZ; z++) {
                Block block = player.getWorld().getBlockAt(startX, y, z);
                oldBlocks.add(new BlockData(block.getLocation().clone(), block.getType()));
                block.setType(material);
                count++;
            }
            
            // East wall (x = endX)
            for (int z = startZ + 1; z < endZ; z++) {
                Block block = player.getWorld().getBlockAt(endX, y, z);
                oldBlocks.add(new BlockData(block.getLocation().clone(), block.getType()));
                block.setType(material);
                count++;
            }
        }
        
        addUndo(player, oldBlocks);
        return count;
    }
    
    public void copy(Player player) {
        if (!hasSelection(player)) {
            return;
        }
        
        Location pos1 = getPos1(player);
        Location pos2 = getPos2(player);
        
        List<BlockData> clipboard = new ArrayList<>();
        
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = pos1.getWorld().getBlockAt(x, y, z);
                    // Store relative position
                    Location relLoc = new Location(null, x - minX, y - minY, z - minZ);
                    clipboard.add(new BlockData(relLoc, block.getType()));
                }
            }
        }
        
        clipboardMap.put(player.getUniqueId(), clipboard);
    }
    
    public int paste(Player player, Location pasteLocation) {
        List<BlockData> clipboard = clipboardMap.get(player.getUniqueId());
        if (clipboard == null || clipboard.isEmpty()) {
            return 0;
        }
        
        List<BlockData> oldBlocks = new ArrayList<>();
        int count = 0;
        
        for (BlockData data : clipboard) {
            Location relLoc = data.getLocation();
            Location actualLoc = new Location(
                pasteLocation.getWorld(),
                pasteLocation.getBlockX() + relLoc.getBlockX(),
                pasteLocation.getBlockY() + relLoc.getBlockY(),
                pasteLocation.getBlockZ() + relLoc.getBlockZ()
            );
            
            Block block = actualLoc.getBlock();
            oldBlocks.add(new BlockData(actualLoc.clone(), block.getType()));
            block.setType(data.getMaterial());
            count++;
        }
        
        addUndo(player, oldBlocks);
        return count;
    }
    
    public boolean undo(Player player) {
        Stack<List<BlockData>> undoStack = undoMap.get(player.getUniqueId());
        if (undoStack == null || undoStack.isEmpty()) {
            return false;
        }
        
        List<BlockData> blocks = undoStack.pop();
        for (BlockData data : blocks) {
            Block block = data.getLocation().getBlock();
            block.setType(data.getMaterial());
        }
        
        return true;
    }
    
    private void addUndo(Player player, List<BlockData> blocks) {
        Stack<List<BlockData>> undoStack = undoMap.computeIfAbsent(
            player.getUniqueId(), 
            k -> new Stack<>()
        );
        
        undoStack.push(blocks);
        
        // Limit undo history to 10 actions
        if (undoStack.size() > 10) {
            undoStack.remove(0);
        }
    }
    
    public int getSelectionSize(Player player) {
        if (!hasSelection(player)) {
            return 0;
        }
        
        Location pos1 = getPos1(player);
        Location pos2 = getPos2(player);
        
        int sizeX = Math.abs(pos2.getBlockX() - pos1.getBlockX()) + 1;
        int sizeY = Math.abs(pos2.getBlockY() - pos1.getBlockY()) + 1;
        int sizeZ = Math.abs(pos2.getBlockZ() - pos1.getBlockZ()) + 1;
        
        return sizeX * sizeY * sizeZ;
    }
    
    public static class BlockData {
        private final Location location;
        private final Material material;
        
        public BlockData(Location location, Material material) {
            this.location = location;
            this.material = material;
        }
        
        public Location getLocation() {
            return location;
        }
        
        public Material getMaterial() {
            return material;
        }
    }
}
