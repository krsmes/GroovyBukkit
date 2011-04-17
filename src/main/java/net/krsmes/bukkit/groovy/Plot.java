package net.krsmes.bukkit.groovy;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.Serializable;
import java.util.*;


public class Plot implements Serializable {
    static String PUBLIC_PLOT_NAME = "PUBLIC";

    String name;
    String owner;
    Location home;
    List<Area> areas = new ArrayList<Area>();
    List<String> visitors = new ArrayList<String>();
    int startDepth = 32;
    boolean open;

    public Plot() {}

    public Plot(String name) {
        this.name = name;
    }

    public Plot(String name, Area area) {
        this(name);
        areas.add(area);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Plot) && (name.equals(((Plot) o).name));
    }

    @Override
    public String toString() {
        return name + (owner==null?"":'('+owner+')');
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setOwner(Player owner) {
        setOwner(owner.getName());
    }

    public List<Area> getAreas() {
        return areas;
    }

    public void setAreas(List<Area> areas) {
        this.areas = areas;
    }

    public List<String> getVisitors() {
        return visitors;
    }

    public void setVisitors(List<String> visitors) {
        this.visitors = visitors;
    }

    public int getStartDepth() {
        return startDepth;
    }

    public void setStartDepth(int startDepth) {
        this.startDepth = startDepth;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isPublic() {
        return this.name.equals(PUBLIC_PLOT_NAME);
    }

    public int getSize() {
        int result = 0;
        for (Area a : areas) {
            result += a.getSize();
        }
        return result;
    }

    public void addArea(Area area) {
        this.areas.add(area);
    }

    public void addVisitor(String visitor) {
        this.visitors.add(visitor);
    }

    public void addVisitor(Player player) {
        addVisitor(player.getName());
    }

    public void removeVisitor(String visitor) {
        this.visitors.remove(visitor);
    }

    public void removeVisitor(Player player) {
        removeVisitor(player.getName());
    }

    public boolean allowed(String name) {
        return (open || name.equals(owner) || visitors.contains(name));
    }

    public boolean allowed(Player player) {
        return open || allowed(player.getName());
    }

    public boolean contains(int x, int z) {
        for (Area area : areas) {
            if (area.contains(x, z)) { return true; }
        }
        return false;
    }

    public void processEvent(PlayerInteractEvent e) {
        if (!allowed(e.getPlayer())) {
            if (e.hasBlock()) {
                if (e.getClickedBlock().getY() >= startDepth) {
                    e.setUseInteractedBlock(Event.Result.DENY);
                }
            }
        }
    }

    public void processEvent(BlockDamageEvent e) {
        if (!allowed(e.getPlayer())) {
            if (e.getBlock().getY() >= startDepth) {
                e.setCancelled(true);
            }
        }
    }


}
