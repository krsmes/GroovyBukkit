package net.krsmes.bukkit.groovy.events;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DayChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public World world;

    public DayChangeEvent(World world) {
        this.world = world;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
