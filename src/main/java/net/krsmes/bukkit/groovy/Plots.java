package net.krsmes.bukkit.groovy;

import net.krsmes.bukkit.groovy.events.PlotChangeEvent;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
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
    public static String ATTR_DATA = "plotsData";
    public static String ATTR_PUBLIC = "plotsPublic";
    public static String ATTR_PLOT_PROTECTION = "plotProtection";
    public static Plots instance;

    Plugin plugin;

    Map<String, Plot> plots;
    Plot publicPlot;
    boolean plotProtection;


    private Plots(Plugin plugin) {
        log.info("Plots: creating instance");
        this.plugin = plugin;
        if (this.plugin != null) { register(); }
    }


    public static synchronized Plots enable(Plugin plugin) {
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
        if (plugin.isEnabled() && plotProtection) {
            Player player;
            Map<String, Object> playerData;
            Plot current;
            if (event instanceof PlayerMoveEvent) {
                PlayerMoveEvent pme = (PlayerMoveEvent) event;
                player = pme.getPlayer();
                playerData = ((GroovyPlugin) plugin).getData(player);
                processEvent(playerData, pme);
            }

            else if (event instanceof BlockDamageEvent) {
                BlockDamageEvent bde = (BlockDamageEvent) event;
                player = bde.getPlayer();
                playerData = ((GroovyPlugin) plugin).getData(player);
                current = playerData == null ? null : (Plot) playerData.get(ATTR_PLOT);
                processEvent(current, bde);
            }

            else if (event instanceof PlayerInteractEvent) {
                PlayerInteractEvent pie = (PlayerInteractEvent) event;
                player = pie.getPlayer();
                playerData = ((GroovyPlugin) plugin).getData(player);
                current = playerData == null ? null : (Plot) playerData.get(ATTR_PLOT);
                processEvent(current, pie);
            }

            else if (event instanceof PlayerGameModeChangeEvent) {
                PlayerGameModeChangeEvent pgmce = (PlayerGameModeChangeEvent) event;
                player = pgmce.getPlayer();
                playerData = ((GroovyPlugin) plugin).getData(player);
                current = playerData == null ? null : (Plot) playerData.get(ATTR_PLOT);
                if (current != null && current.isNoCreative() && pgmce.getNewGameMode() == GameMode.CREATIVE) {
                    pgmce.setCancelled(true);
                }
            }

            else if (event instanceof ExplosionPrimeEvent) {
                ExplosionPrimeEvent epe = (ExplosionPrimeEvent) event;
                current = findPlot(epe.getEntity());
                epe.setCancelled(current.isNoExplode());
                if (current.isNoIgnite()) { epe.setFire(false); }
            }

            else if (event instanceof CreatureSpawnEvent) {
                CreatureSpawnEvent cse = (CreatureSpawnEvent) event;
                if (cse.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
                    current = findPlot(cse.getLocation());
                    cse.setCancelled(current.isNoSpawn());
                }
            }

            else if (event instanceof EntityTargetEvent) {
                EntityTargetEvent ete = (EntityTargetEvent) event;
                player = ete.getTarget() instanceof Player ? (Player) ete.getTarget() : null;
                if (player != null) {
                    current = findPlot(player);
                    ete.setCancelled(current.isNoTarget() && current.allowed(player));
                }
            }

            else if (event instanceof PlayerChatEvent) {
                PlayerChatEvent pce = (PlayerChatEvent) event;
                player = pce.getPlayer();
                current = findPlot(player);
                pce.setCancelled(current.isNoChat());
            }

            else if (event instanceof LightningStrikeEvent) {
                LightningStrikeEvent lse = (LightningStrikeEvent) event;
                current = findPlot(lse.getLightning());
                lse.setCancelled(current.isNoLightning());
            }

            else if (event instanceof BlockIgniteEvent) {
                BlockIgniteEvent bie = (BlockIgniteEvent) event;
                current = findPlot(bie.getBlock().getLocation());
                bie.setCancelled(current.isNoIgnite());
            }

            else if (event instanceof EntityDamageEvent) {
                EntityDamageEvent ede = (EntityDamageEvent) event;
                player = ede.getEntity() instanceof Player ? (Player) ede.getEntity() : null;
                if (player != null) {
                    current = findPlot(player);
                    ede.setCancelled(current.isNoDamage() && current.allowed(player));
                }
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

    /*
    Does a normal x/z plot find but first checks "firstCheck" plot for optimization
     */
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
        mgr.registerEvent(PlayerMoveEvent.class, this, EventPriority.LOW, this, plugin);
        mgr.registerEvent(PlayerTeleportEvent.class, this, EventPriority.LOW, this, plugin);
        mgr.registerEvent(BlockDamageEvent.class, this, EventPriority.LOWEST, this, plugin);
        mgr.registerEvent(PlayerInteractEvent.class, this, EventPriority.LOWEST, this, plugin);
        mgr.registerEvent(PlayerGameModeChangeEvent.class, this, EventPriority.LOWEST, this, plugin);

        mgr.registerEvent(ExplosionPrimeEvent.class, this, EventPriority.LOWEST, this, plugin);
        mgr.registerEvent(CreatureSpawnEvent.class, this, EventPriority.LOWEST, this, plugin);
        mgr.registerEvent(EntityTargetEvent.class, this, EventPriority.LOWEST, this, plugin);
        mgr.registerEvent(PlayerChatEvent.class, this, EventPriority.LOWEST, this, plugin);
        mgr.registerEvent(LightningStrikeEvent.class, this, EventPriority.LOWEST, this, plugin);
        mgr.registerEvent(BlockIgniteEvent.class, this, EventPriority.LOWEST, this, plugin);
        mgr.registerEvent(EntityDamageEvent.class, this, EventPriority.LOWEST, this, plugin);
    }


    protected void processEvent(Plot firstCheck, PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (block != null) {
            findPlot(firstCheck, block.getX(), block.getZ()).processEvent(e);
        }
    }


    protected void processEvent(Plot firstCheck, BlockDamageEvent e) {
        final Block block = e.getBlock();
        findPlot(firstCheck, block.getX(), block.getZ()).processEvent(e);

        if (e.isCancelled() && block.getState() instanceof Sign) {
            // send block update on signs so the text is restored
            plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,
                new Runnable() {
                    public void run() {
                        block.getState().update();
                    }
                },
                50
            );
        }
    }


    protected void processEvent(Map<String, Object> playerData, PlayerMoveEvent e) {
        if (playerData != null) {
            Location to = e.getTo();
            int toX = to.getBlockX();
            int toZ = to.getBlockZ();
            Location from = e.getFrom();
            int fromX = from.getBlockX();
            int fromZ = from.getBlockZ();
            // see if player moved off of block horizontally
            if (toX != fromX || toZ != fromZ || e instanceof PlayerTeleportEvent) {
                Player player = e.getPlayer();
                Plot currentPlot = playerData.containsKey(ATTR_PLOT) ? (Plot) playerData.get(ATTR_PLOT) : null;
                // see if new location is in the same plot (faster than doing a full plot scan)
                if (currentPlot != null && currentPlot.contains(toX, toZ)) {
                    if (e instanceof PlayerTeleportEvent && currentPlot.isNoTeleport()) {
                        // no teleporting within current plot
                        e.setCancelled(true);
                    }
                }
                else {
                    // where are we now?
                    Plot destinationPlot = findPlot(toX, toZ);
                    if (destinationPlot != currentPlot) {
                        if (currentPlot != null && e instanceof PlayerTeleportEvent && currentPlot.isNoTeleport()) {
                            // no teleporting out of current plot
                            e.setCancelled(true);
                        }
                        else if (plotChange(player, currentPlot, destinationPlot)) {
                            // plot change is allowed...
                            if (destinationPlot.isNoCreative() && player.getGameMode() == GameMode.CREATIVE) {
                                player.setGameMode(GameMode.SURVIVAL);
                            }
                            playerData.put(ATTR_PLOT, destinationPlot);
                            Util.sendMessage(plugin, player, ChatColor.DARK_AQUA + "Now in plot " + destinationPlot);
                        }
                        else {
                            // plot change is not allowed...
                            e.setCancelled(true);
                            if (!(e instanceof PlayerTeleportEvent)) {
                                Util.teleport(plugin, player, from);
                            }
                        }
                    }
                }
            }
        }
    }


    protected boolean plotChange(Player p, Plot from, Plot to) {
        // first ask plots if departure and arrival are allowed
        if ((from == null || from.allowDeparture(p)) && (to == null || to.allowArrival(p))) {
            // fire PlotChangeEvent
            PluginManager mgr = plugin.getServer().getPluginManager();
            PlotChangeEvent pce = new PlotChangeEvent(p, from, to);
            mgr.callEvent(pce);
            // make sure it isn't cancelled
            if (!pce.isCancelled()) {
                // notify from/to plots of departure/arrival
                if (from != null) { from.depart(p); }
                if (to != null) { to.arrive(p); }
                return true;
            }
        }
        return false;
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
