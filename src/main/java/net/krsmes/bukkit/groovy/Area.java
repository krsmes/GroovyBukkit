package net.krsmes.bukkit.groovy;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.io.Serializable;

public class Area implements Serializable {
    private static final long serialVersionUID = 1L;

    int minX;
    int maxX;
    int minZ;
    int maxZ;

    public Area(int x1, int x2, int z1, int z2) {
        minX = Math.min(x1, x2);
        maxX = Math.max(x1, x2);
        minZ = Math.min(z1, z2);
        maxZ = Math.max(z1, z2);
    }

    public Area(Location loc1, Location loc2) {
        this(loc1.getBlockX(), loc2.getBlockX(), loc1.getBlockZ(), loc2.getBlockZ());
    }

    public Area(Block blk1, Block blk2) {
        this(blk1.getX(), blk2.getX(), blk1.getZ(), blk2.getZ());
    }

    public int getWidth() {
        return (maxX - minX) + 1;
    }

    public int getDepth() {
        return (maxZ - minZ) + 1;
    }

    public int getSize() {
        return ((maxX - minX) + 1) * ((maxZ - minZ) + 1);
    }

    public int getCenterX() {
        return minX + (getWidth() / 2);
    }

    public int getCenterZ() {
        return minZ + (getDepth() / 2);
    }

    public boolean intersects(Area a) {
        return a.contains(minX, minZ) || a.contains(maxX, maxZ);
    }

    public boolean contains(int x, int z) {
        return (x >= minX && x <= maxX) && (z >= minZ && z <= maxZ);
    }

    boolean contains(Block block) {
        return contains(block.getX(), block.getZ());
    }

    boolean contains(Location loc) {
        return contains(loc.getBlockX(), loc.getBlockZ());
    }

    public String toString() {
        return "Area[" + minX + ',' + minZ + ':' + maxX + ',' + maxZ + ']';
    }

}
