import org.bukkit.event.Event
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent

/*
Commands:
    plot                     show plot information about your current location
    plot ON|OFF              turn on or off the messaging of changing plots as you move or teleport
    plot-list                show a list of all plots

    plot-claim               standing in an unclaimed plot, assign yourself as the new owner
    plot-release             standing in a plot owned by you, release ownership so it becomes an unclaimed plot
    plot-invite PLAYER       standing in an owned plot, invite PLAYER as a visitor allowed to work in this plot

    plot-home                standing in an owned plot, set this plot's home location (for /warp PLOTNAME commands)
    plot-open                standing in an owned plot, set this plot as open to the public
    plot-close               standing in an owned plot, set this plot as closed to the public

    plot-protection ON|OFF   turn plot protection system on or off
    plot-create NAME         using the area defined by the last two Stick-RightClicks (from powertools), create a named plot
    plot-delete NAME         delete the named plot
    plot-assign PLAYER       standing in a plot, assign PLAYER as the owner (note: ignores max-owned)
    plot-add NAME            add the area defined by the last two Stick-RightClicks (from powertools) to the named plot

    plot-max #               set server default maximum plots a player can own
    plot-max PLAYER #        override server default maximum plots for this user (can be lower or higher)

Event Listeners:
    PLAYER_JOIN              tells user if plot protection is on, sets new users to default '/plot on'
    BLOCK_DAMAGE             prevent block damage if player is not allowed to work at the block's location
    PLAYER_INTERACT          prevent building if player is not allowed to work at the location

Plugin 'global' variables in use:
    plotProtection : boolean           true if the plot system is turned on
    plotData : Map<String,Plot>        all system plot data
    plotPublic : Plot                  plot data about public (non-plotted) area
    plotMaxOwned : int                 default maximum number of plots a player can own (defaults to 1)

Player 'data' variables:
    plotMaxOwned : int                 maximum number of plots this player can own
    plot : Plot                        plot the user is currently in (only available with '/plot on')

Notes:
    All area default to a 'PUBLIC' plot that cannot be owned by anyone.
    Ops can plot-invite, plot-open, or plot-close the PUBLIC plot
    Plots can have multiple disjointed areas by using /plot-add
    The default starting depth of a plot is 32 (everything below that depth is open to the public)
    The PUBLIC plot has a starting depth of 48

 */


def playerPlot(player, Closure c) {
    def plot = plots().findPlot(player)
    (plot && ((plot.public && player.op) || (!plot.public && (plot.owner == player.name)))) ? c(plot) : "Plot '${plot?.name}' not owned by $player.name"
}

def defaultMaxOwned = 1

command 'plot-help', { runner, args ->
    [
        "/plot  : show info about current plot",
        "/plot on|off  : show|hide entering/leaving plots",
        "/plot-list  : list all plots",
        "/plot-claim  : claim the current plot",
        "/plot-release  : release claim on current plot",
        "/plot-invite USER  : invite users to work on the plot",
        "/plot-home  : set the home location of this plot",
        "/plot-open  : set this plot to open land",
        "/plot-close  : set this plot to closed land"
    ].each { runner.player.sendMessage it }
    null
}

command 'plot', { runner, args ->
    if (args) {
        if (args[0] in ['on','off']) {
            runner.data.plotShow = args[0] == 'on'
            if (!runner.data.plotShow) runner.data.plot = null
            return "Show plot while moving is ${runner.data.plotShow?'on':'off'}"
        }
        return "Unknown plot command: ${args[0]}"
    }
    def plot = plots().findPlot(runner.player)
    if (plot) {
        [
            "Plot: $plot.name (${plot.open?'open':'closed'} land)",
            "Size: $plot.size (${plot.areas?.size()?:0} area(s))",
            "Owner: ${plot.owner ?: 'unclaimed'} ${p(plot.owner)?.online?'(online)':'(offline)'}",
            "Visitor: $plot.visitors"
        ].each { runner.player.sendMessage it }
        "You are ${plot.allowed(runner.player) ? '' : 'not '}allowed to work here"
    }
    else
        "You are not in a plotted area"
}

command 'plot-list', { runner, args ->
    runner.global?.plots?.data?.values()?.name?.toString()
}

command 'plot-claim', { runner, args ->
    def plot = plots().findPlot(runner.player)
    if (plot && !plot.public)
        if (!plot.owner) {
            def ownedPlots = plots().findOwnedPlots(runner.player.name)
            def allowed = runner.data.plotMaxOwned ?: global.plotMaxOwned ?: defaultMaxOwned
            def owned = ownedPlots?.size() ?: 0
            if (owned >= allowed) {
                "You are not allowed to own additional plots ($owned>=$allowed)"
            }
            else {
                plot.owner = runner.player.name
                "You are now the owner of plot '$plot.name'"
            }
        }
        else "This plot already owned by $plot.owner"
    else "Your are not in a plot"
}


command 'plot-release', { runner, args ->
    playerPlot(runner.player) { plot ->
        plot.owner = null
        plot.open = false
        "Plot '$plot.name' is now not owned by anyone"
    }
}


command 'plot-invite', { runner, args ->
    playerPlot(runner.player) { plot ->
        args.each {
            if (it.startsWith('-')) {
                def name = it.substring(1)
                if (plot.visitors.contains(name)) plot.visitors.remove(name)
                p(name)?.with { sendMessage "You've been uninvited from plot '$plot'"}
            }
            else {
                if (!plot.visitors.contains(it)) plot.visitors << it
                p(it)?.with { sendMessage "You are invited to work on plot '$plot'"}
            }
        }
        "Invited ${args.join(',')} to plot '$plot.name'"
    }
}


command 'plot-home', { runner, args ->
    playerPlot(runner.player) { plot ->
        plot.home = runner.player.location
        "Plot '$plot.name' home set to $plot.home"
    }
}


command 'plot-open', { runner, args ->
    playerPlot(runner.player) { plot ->
        plot.open = true
        "Plot '$plot.name' is now open land"
    }
}


command 'plot-close', { runner, args ->
    playerPlot(runner.player) { plot ->
        plot.open = false
        "Plot '$plot.name' is now closed land"
    }
}



command 'plot-protection', { runner, args ->
    if (args) global.plotProtection = (args[0] == 'on')
    plots().plotProtection = global.plotProtection
    "Plot protection is ${plots().plotProtection?'on':'off'}"
}


command 'plot-create', { runner, args ->
    if (runner.data.stickClicks?.size() > 1 && args?.size() > 0) {
        def plot_name = args.join(' ')
        if (plot_name.equalsIgnoreCase('public'))
            "You cannot create plot named 'public"
        else {
            if (!plots().findPlot(plot_name)) {
                def plot_loc1 = runner.data.stickClicks[0]
                def plot_loc2 = runner.data.stickClicks[1]
                def a = area(plot_loc1, plot_loc2)
                if (plots().createPlot(plot_name, a, runner.world))
                    "Plot '$plot_name' created with area $a"
                else
                    "Unable to create plot '$plot_name'"
            }
            else "Plot '$plot_name' already exists"
        }
    }
    else "To create you need 2 stick-clicks (${runner.data.stickClicks?.size()}) and a name"
}

command 'plot-add', { runner, args ->
    if (runner.data.stickClicks?.size() > 1) {
        def plot_name = runner.data.plot?.name
        if (args?.size() > 0) plot_name = args.join(' ')
        if (plot_name) {
            def plot = plots().findPlot(plot_name)
            if (plot) {
                def plot_loc1 = runner.data.stickClicks[0]
                def plot_loc2 = runner.data.stickClicks[1]
                def a = area(plot_loc1, plot_loc2)
                plot.addArea(a)
                "$a added to $plot"
            }
            else "Plot '$plot_name' not found"
        }
        else "No plot name specified"
    }
    else "No stick-clicks"
}


command 'plot-assign', { runner, args ->
    if (args) {
        def plot = plots().findPlot(runner.player)
        if (!plot.public) {
            plot.owner = args[0]
            "Plot '$plot.name' is now owned by $plot.owner"
        }
        else "Cannot assign this plot"
    }
    else "No user specified"
}


command 'plot-delete', { runner, args ->
    def plot_name = runner.data.plot?.name
    if (args?.size() > 0) plot_name = args.join(' ')
    if (plot_name) {
        def plot = plots().findPlot(plot_name)
        if (plot) {
            plots().removePlot(plot_name)
            "Plot '$plot_name' has been deleted"
        }
        else "Plot '$plot_name' not found"
    }
    else "No plot name specified"
}


command 'plot-max', { runner, args ->
    if (args) {
        if (args.size()==2) {
            def name = args[0]
            def qty = args[1].toInteger()
            def player = p(name)
            if (player) { player.data.plotMaxOwned = qty; "$player.name can own $qty plots" } else { "$name not found/online" }
        }
        else {
            def qty = args[1].toInteger()
            global.plotMaxOwned = qty
            "Default maximum owned plots is now $global.plotMaxOwned"
        }
    }
    else "Maximum owned plots is ${global.plotMaxOwned?: defaultMaxOwned}"
}


[
    (Event.Type.PLAYER_JOIN): { runner, PlayerJoinEvent e ->
        if (!runner.data.containsKey('plotShow')) {
            runner.data.plotShow = true
        }
        if (plots().plotProtection) {
            runner.player.sendMessage "This server has plot protection, see '/plot-help'"
        }
    }
]