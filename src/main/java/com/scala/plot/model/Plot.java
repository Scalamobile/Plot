package com.scala.plot.model;

import java.util.*;

public class Plot {
    
    private final PlotId id;
    private UUID owner;
    private final Set<UUID> members;
    private final Map<PlotFlag, Boolean> flags;
    
    public Plot(PlotId id) {
        this.id = id;
        this.members = new HashSet<>();
        this.flags = new HashMap<>();
        initializeDefaultFlags();
    }
    
    public Plot(PlotId id, UUID owner) {
        this(id);
        this.owner = owner;
    }
    
    private void initializeDefaultFlags() {
        flags.put(PlotFlag.BREAK, false);
        flags.put(PlotFlag.PLACE, false);
    }
    
    public PlotId getId() {
        return id;
    }
    
    public UUID getOwner() {
        return owner;
    }
    
    public void setOwner(UUID owner) {
        this.owner = owner;
    }
    
    public boolean hasOwner() {
        return owner != null;
    }
    
    public Set<UUID> getMembers() {
        return new HashSet<>(members);
    }
    
    public void addMember(UUID player) {
        members.add(player);
    }
    
    public void removeMember(UUID player) {
        members.remove(player);
    }
    
    public boolean isMember(UUID player) {
        return members.contains(player);
    }
    
    public boolean isOwner(UUID player) {
        return owner != null && owner.equals(player);
    }
    
    public boolean canBuild(UUID player) {
        return isOwner(player) || isMember(player);
    }
    
    public void setFlag(PlotFlag flag, boolean value) {
        flags.put(flag, value);
    }
    
    public boolean getFlag(PlotFlag flag) {
        return flags.getOrDefault(flag, false);
    }
    
    public Map<PlotFlag, Boolean> getFlags() {
        return new HashMap<>(flags);
    }
    
    public void reset() {
        this.owner = null;
        this.members.clear();
        initializeDefaultFlags();
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id.toString());
        if (owner != null) {
            data.put("owner", owner.toString());
        }
        List<String> memberList = new ArrayList<>();
        for (UUID member : members) {
            memberList.add(member.toString());
        }
        data.put("members", memberList);
        
        Map<String, Boolean> flagMap = new HashMap<>();
        for (Map.Entry<PlotFlag, Boolean> entry : flags.entrySet()) {
            flagMap.put(entry.getKey().name(), entry.getValue());
        }
        data.put("flags", flagMap);
        
        return data;
    }
    
    @SuppressWarnings("unchecked")
    public static Plot deserialize(Map<String, Object> data) {
        PlotId id = PlotId.fromString((String) data.get("id"));
        Plot plot = new Plot(id);
        
        if (data.containsKey("owner")) {
            plot.setOwner(UUID.fromString((String) data.get("owner")));
        }
        
        if (data.containsKey("members")) {
            List<String> memberList = (List<String>) data.get("members");
            if (memberList != null) {
                for (String member : memberList) {
                    plot.addMember(UUID.fromString(member));
                }
            }
        }
        
        if (data.containsKey("flags")) {
            Object flagsObj = data.get("flags");
            if (flagsObj instanceof Map) {
                Map<String, Object> flagMap = (Map<String, Object>) flagsObj;
                for (Map.Entry<String, Object> entry : flagMap.entrySet()) {
                    try {
                        PlotFlag flag = PlotFlag.valueOf(entry.getKey());
                        plot.setFlag(flag, (Boolean) entry.getValue());
                    } catch (IllegalArgumentException e) {
                        // Ignore invalid flags
                    }
                }
            }
        }
        
        return plot;
    }
}
