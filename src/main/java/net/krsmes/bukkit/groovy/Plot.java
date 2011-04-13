package net.krsmes.bukkit.groovy;


import org.bukkit.Location;
import org.bukkit.block.Block;

public class Plot {

    int minX;
    int maxX;
    int minZ;
    int maxZ;

    public Plot(int x1, int x2, int z1, int z2) {
        minX = Math.min(x1, x2);
        maxX = Math.max(x1, x2);
        minZ = Math.min(z1, z2);
        maxZ = Math.max(z1, z2);
    }

    public Plot(Location loc1, Location loc2) {
        this(loc1.getBlockX(), loc2.getBlockX(), loc1.getBlockZ(), loc2.getBlockZ());
    }

    public Plot(Block blk1, Block blk2) {
        this(blk1.getX(), blk2.getX(), blk1.getZ(), blk2.getZ());
    }

    boolean contains(int x, int z) {
        return (x >= minX && x <= maxX) && (z >= minZ && z <= maxZ);
    }

    boolean contains(Block block) {
        return contains(block.getX(), block.getZ());
    }

    boolean contains(Location loc) {
        return contains(loc.getBlockX(), loc.getBlockZ());
    }

    public String toString() {
        return "Plot[" + minX + ',' + minZ + ':' + maxX + ',' + maxZ + ']';
    }

}
