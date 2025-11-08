package com.scala.plot.model;

public enum PlotFlag {
    BREAK("break", "Allows breaking blocks"),
    PLACE("place", "Allows placing blocks");
    
    private final String name;
    private final String description;
    
    PlotFlag(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static PlotFlag fromString(String name) {
        for (PlotFlag flag : values()) {
            if (flag.getName().equalsIgnoreCase(name)) {
                return flag;
            }
        }
        return null;
    }
}
