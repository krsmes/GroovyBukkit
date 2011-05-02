package net.krsmes.bukkit.groovy;


import groovy.lang.Closure;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.EventExecutor;

import java.util.*;
import java.util.logging.Logger;

public class ListenerClosures implements EventExecutor, Listener {
    static Logger LOG = Logger.getLogger("Minecraft");

    public static ListenerClosures instance;

    GroovyPlugin plugin;
    Map<String, List<Closure>> listeners = new HashMap<String, List<Closure>>();
    Map<String, List<Closure>> registered = new HashMap<String, List<Closure>>();


    private ListenerClosures(GroovyPlugin plugin) {
        this.plugin = plugin;
    }


    public static synchronized ListenerClosures enable(GroovyPlugin plugin) {
        if (instance == null || instance.plugin != plugin) {
            instance = new ListenerClosures(plugin);
        }
        return instance;
    }


    public static synchronized void disable() {
        if (instance != null) {
            instance.listeners.clear();
            // only remove the closures from the registered list as this instance will always be registered for a type
            for (List<Closure> closures : instance.registered.values()) {
                closures.clear();
            }
        }
    }


    public synchronized void load(Map<String, Object> global) {
    }


    public synchronized void save(Map<String, Object> global) {
    }

//
// EventExecutor
//

    public void execute(Listener listener, Event event) {
        if (plugin.enabled) {
            List<Closure> closures = registered.get(event.getEventName());
            if (closures != null) {
                Player player = getEventPlayer(event);

                GroovyRunner runner = plugin.getRunner(player);
                if (runner != null) {
                    for (Closure c : closures) {
                        try {
                            runner.execute(c, event);
                        }
                        catch (Exception ex) {
                            //LOG.severe("Closure '" + c + "' exception: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }


//
// public methods
//

    public void register(String name, Event.Type type, Closure closure) {
        LOG.info(plugin.getDescription().getName() + " registered '" + name + "' for event: " + type);
        List<Closure> namedListener = getOrCreateListeners(name);
        List<Closure> registeredType = getOrCreateRegistered(type, type.toString());
        if (!namedListener.contains(closure)) {
            namedListener.add(closure);
        }
        if (!registeredType.contains(closure)) {
            registeredType.add(closure);
        }
    }


    public void register(String name, String eventName, Closure closure) {
        LOG.info(plugin.getDescription().getName() + " registered '" + name + "' for custom event: " + eventName);
        List<Closure> namedListener = getOrCreateListeners(name);
        List<Closure> registeredType = getOrCreateRegistered(Event.Type.CUSTOM_EVENT, eventName);
        if (!namedListener.contains(closure)) {
            namedListener.add(closure);
        }
        if (!registeredType.contains(closure)) {
            registeredType.add(closure);
        }
    }


    public void unregister(String name) {
        List<Closure> namedListener = listeners.remove(name);
        if (namedListener != null) {
            for (Closure c : namedListener) {
                for (List<Closure> closures : registered.values()) {
                    closures.remove(c);
                }
            }
            LOG.info(plugin.getDescription().getName() + " unregistered '" + name + "'");
        }
    }


//
// helper methods
//

    protected List<Closure> getOrCreateListeners(String name) {
        List<Closure> result = listeners.get(name);
        if (result == null) {
            result = new ArrayList<Closure>();
            listeners.put(name, result);
        }
        return result;
    }


    protected List<Closure> getOrCreateRegistered(Event.Type type, String eventName) {
        List<Closure> result = registered.get(eventName);
        if (result == null) {
            result = new ArrayList<Closure>();
            registered.put(eventName, result);
            plugin.getServer().getPluginManager().registerEvent(type, this, this, Event.Priority.Normal, plugin);
        }
        return result;
    }


    protected Player getEventPlayer(Event event) {
        Player player = null;
        if (event instanceof PlayerEvent) {
            player = ((PlayerEvent) event).getPlayer();
        }
        else if (event instanceof BlockBreakEvent) {
            player = ((BlockBreakEvent) event).getPlayer();
        }
        else if (event instanceof BlockDamageEvent) {
            player = ((BlockDamageEvent) event).getPlayer();
        }
        else if (event instanceof BlockIgniteEvent) {
            player = ((BlockIgniteEvent) event).getPlayer();
        }
        else if (event instanceof BlockPlaceEvent) {
            player = ((BlockPlaceEvent) event).getPlayer();
        }
        else if (event instanceof SignChangeEvent) {
            player = ((SignChangeEvent) event).getPlayer();
        }
        else if (event instanceof VehicleEnterEvent) {
            Entity entity = ((VehicleEnterEvent) event).getEntered();
            if (entity instanceof Player) {
                player = (Player) entity;
            }
        }
        else if (event instanceof VehicleExitEvent) {
            Entity entity = ((VehicleExitEvent) event).getExited();
            if (entity instanceof Player) {
                player = (Player) entity;
            }
        }
        return player;
    }

}
