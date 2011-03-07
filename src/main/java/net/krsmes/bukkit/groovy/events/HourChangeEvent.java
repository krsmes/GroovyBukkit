package net.krsmes.bukkit.groovy.events;

import org.bukkit.World;
import org.bukkit.event.Event;

public class HourChangeEvent extends Event {
    static String NAME = "HOUR CHANGE";

    public World world;
    public int hour;

    public HourChangeEvent(World world, int hour) {
        super(NAME);
        this.world = world;
        this.hour = hour;
    }
}