package net.krsmes.bukkit.groovy.events;

import org.bukkit.World;
import org.bukkit.event.Event;

public class DayChangeEvent extends Event {
    static String NAME = "DAY CHANGE";

    public World world;

    public DayChangeEvent(World world) {
        super(NAME);
        this.world = world;
    }
}
