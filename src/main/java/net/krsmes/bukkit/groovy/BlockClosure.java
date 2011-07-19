package net.krsmes.bukkit.groovy;

import groovy.lang.Closure;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.Event;

import java.io.Serializable;

public class BlockClosure implements Serializable {
    private static final long serialVersionUID = 1L;

    World world;
    int x;
    int y;
    int z;
    int typeId;
    String closureName;

    transient Closure closure;
    transient long triggerCount = 0;
    transient long sleepCount = 0;

    public BlockClosure() {
    }

    public BlockClosure(Block block, String closureName, Closure closure) {
        this.world = block.getWorld();
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
        this.typeId = block.getTypeId();
        this.closureName = closureName;
        this.closure = closure;
    }

    @Override
    public String toString() {
        return closureName + '(' + x + ':' + y + ':' + z + ')';
    }


    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getClosureName() {
        return closureName;
    }

    public void setClosureName(String closureName) {
        this.closureName = closureName;
    }


    public boolean trigger(GroovyPlugin plugin) {
        if (closure != null && --sleepCount < 0 && world.isChunkLoaded(x >> 4, z >> 4)) {
            Block block = world.getBlockAt(x, y, z);
            if (block.getTypeId() == typeId) {
                try {
                    int paramCount = closure.getMaximumNumberOfParameters();
                    Object result;
                    // call closure
                    if (paramCount == 3) {
                        result = closure.call(plugin.getRunner(), block, triggerCount++);
                    } else if (paramCount == 2) {
                        result = closure.call(plugin.getRunner(), block);
                    } else {
                        result = closure.call(block);
                    }
                    // process result
                    if (result instanceof Integer) {
                        sleepCount = (Integer) result;
                    } else if (result instanceof Event) {
                        plugin.getServer().getPluginManager().callEvent((Event) result);
                    } else if (result instanceof Boolean && !(Boolean) result) {
                        // false returned, unregister
                        return false;
                    }
                }
                catch (Exception e) {
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlockClosure that = (BlockClosure) o;

        if (x != that.x) return false;
        if (y != that.y) return false;
        if (z != that.z) return false;
        if (closureName != null ? !closureName.equals(that.closureName) : that.closureName != null) return false;
        if (world != null ? !world.equals(that.world) : that.world != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = world != null ? world.hashCode() : 0;
        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + z;
        result = 31 * result + (closureName != null ? closureName.hashCode() : 0);
        return result;
    }
}