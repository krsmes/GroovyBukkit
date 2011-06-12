package net.krsmes.bukkit.groovy;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.util.Eval;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {

    static final int[] SEETHRU = new int[] {50, 55, 65, 66, 78};


    public static Block lookingAtBlock(LivingEntity ent, double maxDist, double precision) {
        Location loc = ent.getLocation();
        World world = loc.getWorld();
        double eyePosX = loc.getX();
        double eyePosY = loc.getY() + ent.getEyeHeight();
        double eyePosZ = loc.getZ();

        Vector eyeDir = loc.getDirection();
        double eyeDirX = eyeDir.getX();
        double eyeDirY = eyeDir.getY();
        double eyeDirZ = eyeDir.getZ();

        Block result = null;
        double precIncr = precision / 10.0;
        int lastX = 0;
        int lastY = 0;
        int lastZ = 0;
        double dist = 0.0;
        while (dist < maxDist) {
            dist += precision;
            int x = (int) Math.floor(eyePosX + (eyeDirX * dist));
            int y = (int) Math.floor(eyePosY + (eyeDirY * dist));
            int z = (int) Math.floor(eyePosZ + (eyeDirZ * dist));
            if (x != lastX || y != lastY || z != lastZ) {
                result = world.getBlockAt(x, y, z);
                if (y == 128 || y == 1) {
                    break;
                }
                int typeId = result.getTypeId();
                if (typeId > 0 && Arrays.binarySearch(SEETHRU, typeId) < 0) {
                    break;
                }
                lastX = x;
                lastY = y;
                lastZ = z;
            }
            precision += precIncr;
        }
        return result;
    }


    static List<BlockFace> ACTUAL_FACES = Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN);


    public static void eachAttached(Block block, Closure closure) {
        for (BlockFace face : ACTUAL_FACES) { closure.call(block.getFace(face)); }
    }


    public static List collectAttached(Block block, Closure closure) {
        List resultList = new ArrayList();
        for (BlockFace face : ACTUAL_FACES) {
            Object result = closure.call(block.getFace(face));
            if (result != null) {
                resultList.add(result);
            }
        }
        return resultList;
    }


    public static Block findAttached(Block block, Closure closure) {
        for (BlockFace face : ACTUAL_FACES) {
            Block b = block.getFace(face);
            if ((Boolean) Eval.xy(closure, b, "!!x(y)")) {
                return b;
            }
        }
        return null;
    }


    public static List<Block> findAllAttached(Block block, Closure closure) {
        List<Block> resultList = new ArrayList<Block>();
        for (BlockFace face : ACTUAL_FACES) {
            Block b = block.getFace(face);
            if ((Boolean) Eval.xy(closure, b, "!!x(y)")) {
                resultList.add(b);
            }
        }
        return resultList;
    }


    public static void eachBlock(World world, Vector v1, Vector v2, Closure closure) {
        int minX = Math.min(v1.getBlockX(), v2.getBlockX());
        int minY = Math.min(v1.getBlockY(), v2.getBlockY());
        int minZ = Math.min(v1.getBlockZ(), v2.getBlockZ());
        int maxX = Math.max(v1.getBlockX(), v2.getBlockX());
        int maxY = Math.max(v1.getBlockY(), v2.getBlockY());
        int maxZ = Math.max(v1.getBlockZ(), v2.getBlockZ());

        for (int yy = minY; yy <= maxY; yy++) {
            for (int xx = minX; xx <= maxX; xx++) {
                for (int zz = minZ; zz <= maxZ; zz++) {
                    closure.call(world.getBlockAt(xx, yy, zz));
                }
            }
        }
    }


    public static List collectBlock(World world, Vector v1, Vector v2, Closure closure) {
        int minX = Math.min(v1.getBlockX(), v2.getBlockX());
        int minY = Math.min(v1.getBlockY(), v2.getBlockY());
        int minZ = Math.min(v1.getBlockZ(), v2.getBlockZ());
        int maxX = Math.max(v1.getBlockX(), v2.getBlockX());
        int maxY = Math.max(v1.getBlockY(), v2.getBlockY());
        int maxZ = Math.max(v1.getBlockZ(), v2.getBlockZ());

        List resultList = new ArrayList();
        for (int yy = minY; yy <= maxY; yy++) {
            for (int xx = minX; xx <= maxX; xx++) {
                for (int zz = minZ; zz <= maxZ; zz++) {
                    Object result = closure.call(world.getBlockAt(xx, yy, zz));
                    if (result != null) {
                        resultList.add(result);
                    }
                }
            }
        }
        return resultList;
    }


    public static Block findBlock(World world, Vector v1, Vector v2, Closure closure) {
        int minX = Math.min(v1.getBlockX(), v2.getBlockX());
        int minY = Math.min(v1.getBlockY(), v2.getBlockY());
        int minZ = Math.min(v1.getBlockZ(), v2.getBlockZ());
        int maxX = Math.max(v1.getBlockX(), v2.getBlockX());
        int maxY = Math.max(v1.getBlockY(), v2.getBlockY());
        int maxZ = Math.max(v1.getBlockZ(), v2.getBlockZ());

        for (int yy = minY; yy <= maxY; yy++) {
            for (int xx = minX; xx <= maxX; xx++) {
                for (int zz = minZ; zz <= maxZ; zz++) {
                    Block block = world.getBlockAt(xx, yy, zz);
                    if ((Boolean)Eval.xy(closure, block, "!!x(y)")) {
                        return block;
                    }
                }
            }
        }
        return null;
    }


    public static List<Block> findAllBlock(World world, Vector v1, Vector v2, Closure closure) {
        int minX = Math.min(v1.getBlockX(), v2.getBlockX());
        int minY = Math.min(v1.getBlockY(), v2.getBlockY());
        int minZ = Math.min(v1.getBlockZ(), v2.getBlockZ());
        int maxX = Math.max(v1.getBlockX(), v2.getBlockX());
        int maxY = Math.max(v1.getBlockY(), v2.getBlockY());
        int maxZ = Math.max(v1.getBlockZ(), v2.getBlockZ());

        List<Block> resultList = new ArrayList<Block>();
        for (int yy = minY; yy <= maxY; yy++) {
            for (int xx = minX; xx <= maxX; xx++) {
                for (int zz = minZ; zz <= maxZ; zz++) {
                    Block block = world.getBlockAt(xx, yy, zz);
                    if ((Boolean) Eval.xy(closure, block, "!!x(y)")) {
                        resultList.add(block);
                    }
                }
            }
        }
        return resultList;
    }


    public static String join(String glue, String... s) {
        StringBuilder out = new StringBuilder();
        int k = s.length;
        if (k > 0) {
            out.append(s[0]);
            for (int x = 1; x < k; ++x) {
                out.append(glue).append(s[x]);
            }
        }
        return out.toString();
    }


    public static void sendMessage(JavaPlugin plugin, final Player player, final String message) {
        plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,
            new Runnable() {
                public void run() {
                    player.sendMessage(message);
                }
            }
        );
    }


    public static void teleport(GroovyPlugin plugin, final Player player, final Location dest) {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
                new Runnable() {
                    public void run() {
                        player.teleport(dest);
                    }
                }
        );
    }
}
