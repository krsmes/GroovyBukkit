package net.krsmes.bukkit.groovy;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

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
            publicPlot = new Plot(Plot.PUBLIC_PLOT_NAME);
            publicPlot.setStartDepth(48);
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
        plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, this, this, Event.Priority.High, plugin);
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


    public Plot findPlot(Location loc) {
        return (loc == null) ? publicPlot : findPlot(loc.getBlockX(), loc.getBlockZ());
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


    public static Plot findPlot(Collection<Plot> plots, int x, int z) {
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
