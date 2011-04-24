package net.krsmes.bukkit.groovy;

import net.krsmes.bukkit.groovy.events.DayChangeEvent;
import net.krsmes.bukkit.groovy.events.HourChangeEvent;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


public class Events implements Runnable {
    static Logger LOG = Logger.getLogger("Minecraft");

    public static Events instance;

    GroovyPlugin plugin;
    int taskId;
    Map<String,Long> lastHours = new HashMap<String,Long>();


    private Events(GroovyPlugin plugin) {
        this.plugin = plugin;
        schedule();
    }


    public static synchronized Events enable(GroovyPlugin plugin, Map<String, Object> global) {
        if (instance == null || instance.plugin != plugin) {
            instance = new Events(plugin);
        }
        return instance;
    }

    public static synchronized void disable() {
        if (instance != null) {
            instance.unschedule();
            instance = null;
        }
    }


    protected void hourChange(World w, int hour) {
        PluginManager mgr = plugin.getServer().getPluginManager();
        if (hour == 0) {
            mgr.callEvent(new DayChangeEvent(w));
        }
        mgr.callEvent(new HourChangeEvent(w, hour));
    }


    public void run() {
        for (World w : plugin.getServer().getWorlds()) {
            String name = w.getName();
            Long lastHour = lastHours.get(name);
            int curHour = (int) w.getTime() / 1000;
            if (lastHour == null || curHour != lastHour) {
                hourChange(w, curHour);
                lastHours.put(name, (long) curHour);
            }
        }
    }


    protected void schedule() {
        taskId = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, this, 50, 50);
    }


    protected void unschedule() {
        plugin.getServer().getScheduler().cancelTask(taskId);
    }

}
