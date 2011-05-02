package net.krsmes.bukkit.groovy;

import net.krsmes.bukkit.groovy.events.PlotChangeEvent;
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

    Map<String, Plot> plots;
    Plot publicPlot;
    boolean plotProtection;


    private Plots(GroovyPlugin plugin) {
        log.info("Plots: creating instance");
        this.plugin = plugin;
        register();
    }


    public static synchronized Plots enable(GroovyPlugin plugin) {
        if (instance == null || instance.plugin != plugin) {
            instance = new Plots(plugin);
        }
        return instance;
    }


    public static synchronized void disable() {
    }


    @SuppressWarnings({"unchecked"})
    public synchronized void load(Map<String, Object> data) {
        plots = (Map) data.remove(ATTR_DATA);
        if (plots == null) {
            plots = new HashMap<String, Plot>();
        }
        publicPlot = (Plot) data.remove(ATTR_PUBLIC);
        if (publicPlot == null) {
            publicPlot = new PublicPlot();
        }
        Object plotProtectionObj = data.remove(ATTR_PLOT_PROTECTION);
        plotProtection = (plotProtectionObj != null && ((Boolean) plotProtectionObj));

        log.info("Plots: load " + plots.size() + " plots");
    }


    public synchronized void save(Map<String, Object> data) {
        data.put(ATTR_DATA, plots);
        data.put(ATTR_PUBLIC, publicPlot);
        data.put(ATTR_PLOT_PROTECTION, plotProtection);
    }



//
// EventExecutor
//

    public void execute(Listener listener, Event event) {
        if (plugin.enabled) {
            Player player;
            Map<String, Object> playerData;
            switch (event.getType()) {
                case PLAYER_MOVE:
                case PLAYER_TELEPORT:
                    PlayerMoveEvent pme = (PlayerMoveEvent) event;
                    player = pme.getPlayer();
                    playerData = plugin.getData(player);
                    processEvent(playerData, pme);
                    break;

                case BLOCK_DAMAGE:
                    if (plotProtection) {
                        BlockDamageEvent bde = (BlockDamageEvent) event;
                        player = bde.getPlayer();
                        playerData = plugin.getData(player);
                        Plot current = playerData == null ? null : (Plot) playerData.get(ATTR_PLOT);
                        processEvent(current, bde);
                    }
                    break;

                case PLAYER_INTERACT:
                    if (plotProtection) {
                        PlayerInteractEvent pie = (PlayerInteractEvent) event;
                        player = pie.getPlayer();
                        playerData = plugin.getData(player);
                        Plot current = playerData == null ? null : (Plot) playerData.get(ATTR_PLOT);
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
        return plots.put(plot.getName(), plot);
    }


    public Plot createPlot(String name, Area area, World world) {
        Plot result = null;
        if (!plots.containsKey(name) && !name.equalsIgnoreCase(PublicPlot.PUBLIC_PLOT_NAME)) {
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
        plots.remove(name);
    }


    public Plot findPlot(int x, int z) {
        Plot result = findPlot(plots.values(), x, z);
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
        return plots.get(name);
    }


    public List<Plot> findOwnedPlots(String owner) {
        List<Plot> result = new ArrayList<Plot>();
        if (owner != null) {
            for (Plot plot : plots.values()) {
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
        mgr.registerEvent(Event.Type.PLAYER_MOVE, this, this, Event.Priority.Low, plugin);
        mgr.registerEvent(Event.Type.PLAYER_TELEPORT, this, this, Event.Priority.Low, plugin);
        mgr.registerEvent(Event.Type.BLOCK_DAMAGE, this, this, Event.Priority.Lowest, plugin);
        mgr.registerEvent(Event.Type.PLAYER_INTERACT, this, this, Event.Priority.Lowest, plugin);
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


    protected void processEvent(Map<String, Object> playerData, PlayerMoveEvent e) {
        Event.Type type = e.getType();
        Location to = e.getTo();
        int toX = to.getBlockX();
        int toZ = to.getBlockZ();
        Location from = e.getFrom();
        int fromX = from.getBlockX();
        int fromZ = from.getBlockZ();
        // see if player moved off of block horizontally
        if (toX != fromX || toZ != fromZ || type == Event.Type.PLAYER_TELEPORT) {
            Plot current = (Plot) playerData.get(ATTR_PLOT);
            // see if new location is in the same plot (faster than doing a full plot scan)
            if ((current != null) && current.contains(toX, toZ)) {
            } else {
                // where are we now?
                Plot plot = findPlot(toX, toZ);
                if (plot != current) {
                    Player player = e.getPlayer();
                    if (plotChange(player, current, plot)) {
                        playerData.put(ATTR_PLOT, plot);

                        Object plotShow = playerData.get(ATTR_PLOT_SHOW);
                        if (plotShow instanceof Boolean && (Boolean) plotShow) {
                            Util.sendMessage(plugin, player, ChatColor.DARK_AQUA + "Now in plot " + plot);
                        }
                    }
                    else {
                        e.setCancelled(true);
                        Util.teleport(plugin, player, from);
                    }
                }
            }
        }
    }


    protected boolean plotChange(Player p, Plot from, Plot to) {
        PluginManager mgr = plugin.getServer().getPluginManager();
        PlotChangeEvent pce = new PlotChangeEvent(p, from, to);
        mgr.callEvent(pce);
        return !pce.isCancelled();
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
