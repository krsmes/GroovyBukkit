package net.krsmes.bukkit.groovy;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.*;


public class Plot implements Serializable {
    private static final long serialVersionUID = 1L;

    public static int PLOT_START_DEPTH = 32;

    String name;
    String owner;
    Location home;
    List<Area> areas = new ArrayList<Area>();
    List<String> visitors = new ArrayList<String>();
    int startDepth = PLOT_START_DEPTH;
    boolean open;

    public Plot() {}

    public Plot(String name) {
        this.name = name;
    }

    public Plot(String name, Area area) {
        this(name);
        if (area != null) { areas.add(area); }
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

    public Location getHome() {
        return home;
    }

    public void setHome(Location home) {
        this.home = home;
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
        return false;
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

    public boolean allowDamage(Player player, Block block) {
        // assumes plot contains block.x and block.z
        return (block.getY() < startDepth || allowed(player));
    }

    public boolean allowInteract(Player player, Block block, ItemStack item) {
        // assumes plot contains block.x and block.z
        return (block == null || block.getY() < startDepth || allowed(player));
    }

    public void processEvent(PlayerInteractEvent e) {
//        if ((e.getAction() != Action.RIGHT_CLICK_BLOCK || !allowInteract(e.getPlayer(), e.getClickedBlock(), e.getItem())) && !allowed(e.getPlayer())) {
        if (!allowInteract(e.getPlayer(), e.getClickedBlock(), e.getItem())) {
            e.setUseInteractedBlock(Event.Result.DENY);
        }
//        System.out.println("Plot: " + e.getEventName() + " (" + e.getPlayer().getName() + "): item=" + e.getItem() + ", action=" + e.getAction() + ", clickedBlock=" + e.getClickedBlock() + " useBlock=" + e.useInteractedBlock());
    }

    public void processEvent(BlockDamageEvent e) {
        if (!allowDamage(e.getPlayer(), e.getBlock())) {
            e.setCancelled(true);
        }
    }


}
