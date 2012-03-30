package net.krsmes.bukkit.groovy.events;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HourChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public World world;
    public int hour;

    public HourChangeEvent(World world, int hour) {
        this.world = world;
        this.hour = hour;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}