package net.krsmes.bukkit.groovy.events;

import net.krsmes.bukkit.groovy.Plot;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;


public class PlotChangeEvent extends Event implements Cancellable {
    static String NAME = "PLOT CHANGE";

    protected boolean cancelled;

    public Player player;
    public Plot from;
    public Plot to;


    public PlotChangeEvent(Player player, Plot from, Plot to) {
        super(NAME);

        this.player = player;
        this.from = from;
        this.to = to;
    }


    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
