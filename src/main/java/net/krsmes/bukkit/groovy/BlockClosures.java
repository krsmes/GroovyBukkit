package net.krsmes.bukkit.groovy;

import groovy.lang.Closure;
import org.bukkit.block.Block;

import java.util.*;
import java.util.logging.Logger;


public class BlockClosures implements Runnable {
    static Logger LOG = Logger.getLogger("Minecraft");

    public static final String ATTR_BLOCK_CLOSURES = "blockClosures";
    public static BlockClosures instance;
    static final int PERIOD_TICKS = 100;

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

    public synchronized void load(Map<String, Object> data) {
        if (data.containsKey(ATTR_BLOCK_CLOSURES)) {
            //noinspection unchecked
            blockClosureList.addAll((Collection<? extends BlockClosure>) data.remove(ATTR_BLOCK_CLOSURES));
        }
    }

    public synchronized void save(Map<String, Object> data) {
        List<BlockClosure> blockClosures = new ArrayList<BlockClosure>();
        for (BlockClosure bc : blockClosureList) {
            if (bc.closure != null && bc.closureName != null) {
                blockClosures.add(bc);
            }
        }
        data.put(ATTR_BLOCK_CLOSURES, blockClosures);
    }


    public void run() {
        for (ListIterator<BlockClosure> iter = blockClosureList.listIterator(); iter.hasNext(); ) {
            if (!iter.next().trigger(plugin)) {
                iter.remove();
            }
        }
    }


    protected void schedule() {
        taskId = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, this, 50, PERIOD_TICKS);
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
        BlockClosure bc = new BlockClosure(block, closureName, registeredClosures.get(closureName));
        if (!blockClosureList.contains(bc)) {
            blockClosureList.add(bc);
        }
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

}
