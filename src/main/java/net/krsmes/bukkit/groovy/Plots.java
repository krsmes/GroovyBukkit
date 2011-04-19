package net.krsmes.bukkit.groovy;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import javax.imageio.metadata.IIOInvalidTreeException;
import java.util.*;
import java.util.logging.Logger;

public class Plots implements EventExecutor, Listener {
    static Logger log = Logger.getLogger("Minecraft");

    public static String ATTR_PLOT = "plot";
    public static String ATTR_PLOT_SHOW = "plotShow";
    public static String ATTR_DATA = "plotsData";
    public static String ATTR_PUBLIC = "plotsPublic";
    public static Plots instance;

    Map<String, Plot> data;
    Plot publicPlot;


    private Plots() {
        log.info("Plots: creating instance");
    }

    public static Plots create(Plugin plugin, Map<String,Object> global) {
        Map<String, Plot> data = (Map) global.get(ATTR_DATA);
        if (data == null) {
            data = new HashMap<String, Plot>();
            global.put(ATTR_DATA, data);
        }
        Plot publicPlot = (Plot) global.get(ATTR_PUBLIC);
        if (publicPlot == null) {
            publicPlot = new PublicPlot();
            global.put(ATTR_PUBLIC, publicPlot);
        }
        if (instance == null) {
            instance = new Plots();
            instance.register(plugin);
        }
        log.info("Plots: setting data: "+ data.size() + " plots");
        instance.data = data;
        instance.publicPlot = publicPlot;
        return instance;
    }

    public void register(Plugin plugin) {
        log.info("Plots: registering");
        plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, this, this, Event.Priority.High, plugin);
        plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_TELEPORT, this, this, Event.Priority.High, plugin);
    }

    public void execute(Listener listener, Event event) {
        Player player;
        Map<String, Object> playerData;
        Plot plot;
        switch (event.getType()) {
            case PLAYER_MOVE:
            case PLAYER_TELEPORT:
                PlayerMoveEvent pme = (PlayerMoveEvent) event;
                player = pme.getPlayer();
                playerData = GroovyPlugin.getInstance().getData(player);
                Object plotShow = playerData.get(ATTR_PLOT_SHOW);
                if (plotShow instanceof Boolean && (Boolean)plotShow) {
                    Location to = pme.getTo();
                    int toX = to.getBlockX();
                    int toZ = to.getBlockZ();
                    Object current = playerData.get(ATTR_PLOT);
                    if ((current instanceof Plot) && ((Plot) current).contains(toX, toZ)) {
                    }
                    else {
                        plot = findPlot(toX, toZ);
                        if (plot != current) {
                            playerData.put(ATTR_PLOT, plot);
                            player.sendMessage(ChatColor.DARK_AQUA + "Now in plot " + plot);
                        }
                    }
                }
                break;
        }
    }


    public Plot addPlot(Plot plot) {
        log.info("Plots: adding " + plot.getName());
        return data.put(plot.getName(), plot);
    }


    public Plot createPlot(String name, Area area, World world) {
        Plot result = null;
        if (!data.containsKey(name) && !name.equalsIgnoreCase(PublicPlot.PUBLIC_PLOT_NAME)) {
            result = new Plot(name, area);
            addPlot(result);
            int x = area.getCenterX();
            int z = area.getCenterZ();
            int y = world.getHighestBlockYAt(x, z);
            result.setHome(new Location(world, x + 0.5, y + 1.0, z + 0.5));
        }
        return result;
    }


    public void removePlot(String name) {
        data.remove(name);
    }


    public Plot findPlot(int x, int z) {
        Plot result = findPlot(data.values(), x, z);
        if (result == null) {
            result = publicPlot;
        }
        return result;
    }


    public Plot findPlot(Plot firstCheck, int x, int z) {
        Plot result = firstCheck;
        if (firstCheck == null || !firstCheck.contains(x, z)) {
            result = findPlot(x, z);
        }
        return result;
    }


    public Plot findPlot(Location loc) {
        return (loc == null) ? publicPlot : findPlot(loc.getBlockX(), loc.getBlockZ());
    }


    public Plot findPlot(Entity ent) {
        return (ent == null) ? publicPlot : findPlot(ent.getLocation());
    }


    public Plot findPlot(String name) {
        return data.get(name);
    }


    public List<Plot> findOwnedPlots(String owner) {
        List<Plot> result = new ArrayList<Plot>();
        if (owner != null) {
            for (Plot plot : data.values()) {
                if (owner.equals(plot.getOwner())) {
                    result.add(plot);
                }
            }
        }
        return result;
    }


    public void processEvent(Plot firstCheck, PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (block != null) {
            findPlot(firstCheck, block.getX(), block.getZ()).processEvent(e);
        }
    }


    public void processEvent(Plot firstCheck, BlockDamageEvent e) {
        Block block = e.getBlock();
        findPlot(firstCheck, block.getX(), block.getZ()).processEvent(e);
    }


    private static Plot findPlot(Collection<Plot> plots, int x, int z) {
        if (plots != null) {
            for (Plot plot : plots) {
                if (plot.contains(x, z)) {
                    return plot;
                }
            }
        }
        return null;
    }

}
