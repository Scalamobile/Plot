package com.scala.plot.generator;

import com.scala.plot.managers.PlotManager;
import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class PlotWorldGenerator extends ChunkGenerator {
    
    private static final int PLOT_SIZE = PlotManager.getPlotSize();
    private static final int ROAD_SIZE = PlotManager.getRoadSize();
    private static final int TOTAL_SIZE = PlotManager.getTotalSize();
    
    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        // Generate flat world with plots
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;
                
                // Calculate position within plot grid
                int modX = Math.floorMod(worldX, TOTAL_SIZE);
                int modZ = Math.floorMod(worldZ, TOTAL_SIZE);
                
                boolean isRoad = (modX >= PLOT_SIZE || modZ >= PLOT_SIZE);
                
                // Bedrock layer at y=0
                chunkData.setBlock(x, 0, z, Material.BEDROCK);
                
                // Dirt layers from y=1 to y=62
                for (int y = 1; y <= 62; y++) {
                    chunkData.setBlock(x, y, z, Material.DIRT);
                }
                
                if (isRoad) {
                    // Roads are made of stone slabs at y=63 and y=64
                    chunkData.setBlock(x, 63, z, Material.SMOOTH_STONE_SLAB);
                    chunkData.setBlock(x, 64, z, Material.SMOOTH_STONE_SLAB);
                } else {
                    // Plot area has grass at y=63
                    chunkData.setBlock(x, 63, z, Material.GRASS_BLOCK);
                }
            }
        }
    }
    
    @Override
    public boolean shouldGenerateNoise() {
        return false;
    }
    
    @Override
    public boolean shouldGenerateSurface() {
        return false;
    }
    
    @Override
    public boolean shouldGenerateBedrock() {
        return false;
    }
    
    @Override
    public boolean shouldGenerateCaves() {
        return false;
    }
    
    @Override
    public boolean shouldGenerateDecorations() {
        return false;
    }
    
    @Override
    public boolean shouldGenerateMobs() {
        return false;
    }
    
    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }
}
