package net.krsmes.bukkit.groovy;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;

import java.util.*;
import java.util.logging.Logger;

/**
 * Singleton class for Plots suppport
 */
public class Plots implements EventExecutor, Listener {
    static Logger log = Logger.getLogger("Minecraft");

    public static String ATTR_PLOTS = "plots";
    public static String ATTR_PLOT = "plot";
    public static String ATTR_PLOT_SHOW = "plotShow";
    public static String ATTR_DATA = "plotsData";
    public static String ATTR_PUBLIC = "plotsPublic";
    public static String ATTR_PLOT_PROTECTION = "plotProtection";
    public static Plots instance;

    GroovyPlugin plugin;
    Map<String, Plot> data;
    Plot publicPlot;
    boolean plotProtection;


    private Plots(GroovyPlugin plugin) {
        log.info("Plots: creating instance");
        this.plugin = plugin;
        register();
    }


    public static synchronized Plots enable(GroovyPlugin plugin, Map<String, Object> global) {
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
        boolean plotProtection = (Boolean) global.get(ATTR_PLOT_PROTECTION);

        if (instance == null || instance.plugin != plugin) {
            instance = new Plots(plugin);
            instance.register();
            global.put(ATTR_PLOTS, instance);
        }
        log.info("Plots: setting data: "+ data.size() + " plots");
        instance.data = data;
        instance.publicPlot = publicPlot;
        instance.plotProtection = plotProtection;
        return instance;
    }


    public static synchronized void disable() {

    }


//
// EventExecutor
//

    public void execute(Listener listener, Event event) {
        if (plugin.enabled) {
            Player player;
            Map<String, Object> playerData;
            Plot plot;
            switch (event.getType()) {
                case PLAYER_MOVE:
                case PLAYER_TELEPORT:
                    PlayerMoveEvent pme = (PlayerMoveEvent) event;
                    player = pme.getPlayer();
                    playerData = plugin.getData(player);
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

                case BLOCK_DAMAGE:
                    if (plotProtection) {
                        BlockDamageEvent bde = (BlockDamageEvent) event;
                        player = bde.getPlayer();
                        playerData = plugin.getData(player);
                        Plot current = (Plot) playerData.get(ATTR_PLOT);
                        processEvent(current, bde);
                    }
                    break;

                case PLAYER_INTERACT:
                    if (plotProtection) {
                        PlayerInteractEvent pie = (PlayerInteractEvent) event;
                        player = pie.getPlayer();
                        playerData = plugin.getData(player);
                        Plot current = (Plot) playerData.get(ATTR_PLOT);
                        processEvent(current, pie);
                    }
                    break;
            }
        }
    }


    public Plot getPublicPlot() {
        return publicPlot;
    }

    public void setPublicPlot(Plot publicPlot) {
        this.publicPlot = publicPlot;
    }

    public boolean isPlotProtection() {
        return plotProtection;
    }

    public void setPlotProtection(boolean plotProtection) {
        this.plotProtection = plotProtection;
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



//
// helper methods
//

    protected void register() {
        log.info("Plots: registering");
        PluginManager mgr = plugin.getServer().getPluginManager();
        mgr.registerEvent(Event.Type.PLAYER_MOVE, this, this, Event.Priority.High, plugin);
        mgr.registerEvent(Event.Type.PLAYER_TELEPORT, this, this, Event.Priority.High, plugin);
        mgr.registerEvent(Event.Type.BLOCK_DAMAGE, this, this, Event.Priority.High, plugin);
        mgr.registerEvent(Event.Type.PLAYER_INTERACT, this, this, Event.Priority.High, plugin);
    }


    protected void processEvent(Plot firstCheck, PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (block != null) {
            findPlot(firstCheck, block.getX(), block.getZ()).processEvent(e);
        }
    }


    protected void processEvent(Plot firstCheck, BlockDamageEvent e) {
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
