package net.krsmes.bukkit.groovy;

import groovy.lang.Closure;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.Event;

import java.util.*;
import java.util.logging.Logger;


public class BlockClosures implements Runnable {
    static Logger LOG = Logger.getLogger("Minecraft");

    public static BlockClosures instance;

    GroovyPlugin plugin;
    int taskId;

    Map<String,Closure> registeredClosures = new HashMap<String, Closure>();
    List<BlockClosure> blockClosureList = new ArrayList<BlockClosure>();


    private BlockClosures(GroovyPlugin plugin) {
        this.plugin = plugin;
        schedule();
    }


    public static synchronized BlockClosures enable(GroovyPlugin plugin) {
        if (instance == null || instance.plugin != plugin) {
            instance = new BlockClosures(plugin);
        }
        return instance;
    }

    public static synchronized void disable() {
        if (instance != null) {
            instance.unschedule();
            instance = null;
        }
    }

    public synchronized void load(Map<String, Object> global) {
        // TODO !
    }

    public synchronized void save(Map<String, Object> global) {
        // TODO !
    }


    public void run() {
        for (ListIterator<BlockClosure> iter = blockClosureList.listIterator(); iter.hasNext(); ) {
            if (!iter.next().trigger(plugin)) {
                iter.remove();
            }
        }
    }


    protected void schedule() {
        taskId = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, this, 50, 100);
    }


    protected void unschedule() {
        plugin.getServer().getScheduler().cancelTask(taskId);
    }


    public void registerClosure(String name, Closure c) {
        LOG.info("GroovyBukkit registered closure '"+name+"'");
        registeredClosures.put(name, c);
        for (BlockClosure bc : blockClosureList) {
            if (name.equals(bc.closureName)) { bc.closure = c; }
        }
    }


    public void registerBlock(Block block, String closureName) {
        blockClosureList.add(new BlockClosure(block, closureName, registeredClosures.get(closureName)));
    }

    /* note: this form will not persist between reloads */
    public void registerBlock(Block block, Closure closure) {
        blockClosureList.add(new BlockClosure(block, null, closure));
    }

    public void unregisterBlock(Block block) {
        for (ListIterator<BlockClosure> iter = blockClosureList.listIterator(); iter.hasNext(); ) {
            if (!iter.next().is(block)) {
                iter.remove();
            }
        }
    }


    static class BlockClosure {
        World world;
        int x;
        int y;
        int z;
        int typeId;
        String closureName;

        transient Closure closure;
        transient long triggerCount = 0;
        transient long sleepCount = 0;

        public BlockClosure() {}

        public BlockClosure(Block block, String closureName, Closure closure) {
            this.world = block.getWorld();
            this.x = block.getX();
            this.y = block.getY();
            this.z = block.getZ();
            this.typeId = block.getTypeId();
            this.closureName = closureName;
            this.closure = closure;
        }

        public boolean trigger(GroovyPlugin plugin) {
            if (closure != null && --sleepCount < 0 && world.isChunkLoaded(x >> 4, z >> 4)) {
                Block block = world.getBlockAt(x, y, z);
                if (block.getTypeId() == typeId) {
                    int paramCount = closure.getMaximumNumberOfParameters();
                    Object result;
                    // call closure
                    if (paramCount == 3) {
                        result = closure.call(plugin.getRunner(), block, triggerCount++);
                    }
                    else if (paramCount == 2) {
                        result = closure.call(plugin.getRunner(), block);
                    }
                    else {
                        result = closure.call(block);
                    }
                    // process result
                    if (result instanceof Integer) {
                        sleepCount = (Integer) result;
                    }
                    else if (result instanceof Event) {
                        plugin.getServer().getPluginManager().callEvent((Event)result);
                    }
                    else if (result instanceof Boolean && !(Boolean)result) {
                        // false returned, unregister
                        return false;
                    }
                }
                else {
                    // typeId has changed, unregister
                    return false;
                }
            }
            return true;
        }

        public boolean is(Block block) {
            return world.equals(block.getWorld()) &&
                    x == block.getX() && y == block.getY() && z == block.getZ();
        }
    }
}
