package com.scala.plot.model;

import java.util.Objects;

public class PlotId {
    
    private final int x;
    private final int z;
    
    public PlotId(int x, int z) {
        this.x = x;
        this.z = z;
    }
    
    public int getX() {
        return x;
    }
    
    public int getZ() {
        return z;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlotId plotId = (PlotId) o;
        return x == plotId.x && z == plotId.z;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }
    
    @Override
    public String toString() {
        return x + ";" + z;
    }
    
    public static PlotId fromString(String str) {
        String[] parts = str.split(";");
        return new PlotId(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
}
