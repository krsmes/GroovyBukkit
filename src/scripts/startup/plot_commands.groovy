import org.bukkit.event.Event
import org.bukkit.event.player.PlayerJoinEvent
import net.krsmes.bukkit.groovy.GroovyRunner

/*
Commands:
    plot                     show plot information about your current location
    plot list                show a list of all plots

    plot claim               standing in an unclaimed plot, assign yourself as the new owner
    plot release             standing in a plot owned by you, release ownership so it becomes an unclaimed plot
    plot invite PLAYER       standing in an owned plot, invite PLAYER as a visitor allowed to work in this plot
    plot remove PLAYER       standing in an owned plot, remove PLAYER as a visitor allowed to work in this plot

    plot set home            standing in an owned plot, set this plot's home location (for /warp PLOTNAME commands)
    plot set open            standing in an owned plot, set this plot as open to the public
    plot set close           standing in an owned plot, set this plot as closed to the public

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

def defaultMaxOwned = 3


def playerPlot(player, Closure c) {
    def plot = plots().findPlot(player)
    (plot && ((plot.public && player.op) || (!plot.public && (plot.owner == player.name)))) ? c(plot) : "Plot '${plot?.name}' not owned by $player.name"
}


def plotClaim = { GroovyRunner r ->
    def plot = r.plots().findPlot(r.player)
    if (!plot || plot.public) return "Your are not in a plot"

    if (plot.owner) return "This plot already owned by $plot.owner"

    def ownedPlots = r.plots().findOwnedPlots(r.player.name)
    def allowed = r.data.plotMaxOwned ?: r.global.plotMaxOwned ?: defaultMaxOwned
    def owned = ownedPlots?.size() ?: 0
    if (owned >= allowed) {
        "You are not allowed to own additional plots ($owned>=$allowed)"
    }
    else {
        plot.owner = r.player.name
        "You are now the owner of plot '$plot.name'"
    }
}


def plotRelease = { GroovyRunner r ->
    playerPlot(r.player) { plot ->
        plot.owner = null
        plot.open = false
        "Plot '$plot.name' is now not owned by anyone"
    }
}


def plotInvite = { GroovyRunner r, List args ->
    playerPlot(r.player) { plot ->
        args.each {
            if (!plot.visitors.contains(it)) plot.visitors << it
            p(it)?.with { sendMessage "You are invited to work on plot '$plot'"}
        }
        "Invited ${args.join(',')} to plot '$plot.name'"
    }
}


def plotRemove = { GroovyRunner r, List args ->
    playerPlot(r.player) { plot ->
        args.each {
            if (plot.visitors.contains(name)) plot.visitors.remove(it)
            p(it)?.with { sendMessage "You've been removed from plot '$plot'"}
        }
        "Removed ${args.join(',')} from plot '$plot.name'"
    }
}


def plotSet = { GroovyRunner r, List args ->
    def setWhat = args.remove(0)
    def setter =
        setWhat == 'open' ? { plot ->
            plot.open = true
            "Plot '$plot.name' is now open land"
        } :
        setWhat == 'close' ? { plot ->
            plot.open = false
            "Plot '$plot.name' is now closed land"
        } :
        setWhat == 'home' ? { plot ->
            plot.home = r.player.location
            "Plot '$plot.name' home set to $plot.home"
        } :
        null

    setter ? playerPlot(r.player, setter) : "Unknown 'set' command"
}


def plotCorner = { GroovyRunner r ->
    def p = r.player
    def plot = r.plots().findPlot(p)
    if (plot?.public) {
        def loc = p.location
        r.data.temp.plotCorner = loc
        "Plot corner set to $loc.blockX,$loc.blockZ"
    }
    else {
        "You cannot set corner inside another plot"
    }
}


def plotCreate = { GroovyRunner r, List args ->
    if (args?.size() == 0) return "Name needed to create plot"
    def plot_name = args[0]
    if (plot_name.equalsIgnoreCase('public')) return "You cannot create plot named 'public'"
    if (r.plots().findPlot(plot_name)) return "Plot '$plot_name' already exists"

    def corner1 = r.data.temp.plotCorner
    if (!corner1) return "Plot needs an opposite corner, see /plot help"

    def corner2 = r.player.location
    if (!r.plots().findPlot(corner2)?.public) return "You cannot create plot inside another plot"

    def a = r.area(corner1, corner2)
    if (a.size > 1024 && !r.player.op) return "You cannot create a plot of this size"

    if (r.plots().createPlot(plot_name, a, r.world))
        "Plot '$plot_name' created $a (use '/plot claim' to claim)"
    else
        "Unable to create plot '$plot_name'"
}


command 'plot', { GroovyRunner r, List args ->
    def msgs = []
    if (args) switch (args.remove(0)) {
        case 'list': return r.plots().plots.values().name.toString()

        case 'claim': return plotClaim(r)
        case 'release': return plotRelease(r)

        case 'invite': return plotInvite(r, args)
        case 'remove': return plotRemove(r, args)

        case 'set': return plotSet(r, args)

        case 'corner': return plotCorner(r)
        case 'create': return plotCreate(r, args)

        default:
            msgs << "/plot  :show info about current plot"
            msgs << "/plot list  :list all plots"
            msgs << "/plot claim  :claim the current plot"
            msgs << "/plot release  :release claim on current plot"
            msgs << "/plot invite USERS  :invite users to work on the plot"
            msgs << "/plot remove USERS  :remove users to work on the plot"
            msgs << "/plot set home  :set the home location of this plot"
            msgs << "/plot set open  :set this plot to open land"
            msgs << "/plot set close  :set this plot to closed land"
            msgs << "/plot corner  :record corner for new plot"
            msgs << "/plot create NAME  :create plot using corner and here"
            break
    }
    // no args...
    def plot = r.plots().findPlot(r.player)
    if (!msgs && plot) {
        msgs << "Plot: $plot.name (${plot.open ? 'open' : 'closed'} land)"
        msgs << "Size: $plot.size (${plot.areas?.size() ?: 0} area(s))"
        msgs << "Owner: ${plot.owner ?: 'unclaimed'} ${p(plot.owner)?.online ? '(online)' : '(offline)'}"
        msgs << "Visitor: $plot.visitors"
    }
    if (plot) msgs << "You are ${plot.allowed(r.player) ? '' : 'not '}allowed to work here"

    msgs.each { r.player.sendMessage it }
    null
}


command 'plot-protection', { GroovyRunner r, List args ->
    if (args) r.plots().plotProtection = (args[0] == 'on')
    "Plot protection is ${r.plots().plotProtection?'on':'off'}"
}


command 'plot-create', { GroovyRunner r, List args ->
    if (args?.size() == 0) return "Name needed to create plot"

    def plot_name = args[0]
    if (plot_name.equalsIgnoreCase('public')) return "You cannot create plot named 'public'"
    if (r.plots().findPlot(plot_name)) return "Plot '$plot_name' already exists"

    def plot_loc1 = null
    def plot_loc2 = null

    // TODO: alternate ways of identifying a plot
    if (r.data.stickClicks?.size() > 1) {
        plot_loc1 = r.data.stickClicks[0]
        plot_loc2 = r.data.stickClicks[1]
    }

    if (!plot_loc1 || !plot_loc2) return "Unable to identify plot perimeter"

    def a = r.area(plot_loc1, plot_loc2)

    if (r.plots().createPlot(plot_name, a, r.world))
        "Plot '$plot_name' created with area $a"
    else
        "Unable to create plot '$plot_name'"
}


command 'plot-add', { GroovyRunner r, List args ->
    if (r.data.stickClicks?.size() > 1) {
        def plot_name = r.data.plot?.name
        if (args?.size() > 0) plot_name = args.join(' ')
        if (plot_name) {
            def plot = plots().findPlot(plot_name)
            if (plot) {
                def plot_loc1 = r.data.stickClicks[0]
                def plot_loc2 = r.data.stickClicks[1]
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


command 'plot-assign', { GroovyRunner r, List args ->
    if (args) {
        def plot = plots().findPlot(r.player)
        if (!plot.public) {
            plot.owner = args[0]
            "Plot '$plot.name' is now owned by $plot.owner"
        }
        else "Cannot assign this plot"
    }
    else "No user specified"
}


command 'plot-delete', { GroovyRunner r, List args ->
    def plot_name = r.data.plot?.name
    if (args?.size() > 0) plot_name = args.join(' ')
    if (plot_name) {
        def plot = r.plots().findPlot(plot_name)
        if (plot) {
            r.plots().removePlot(plot_name)
            "Plot '$plot_name' has been deleted"
        }
        else "Plot '$plot_name' not found"
    }
    else "No plot name specified"
}


command 'plot-max', { GroovyRunner r, List args ->
    if (args) {
        if (args.size()==2) {
            def name = args[0]
            def qty = args[1].toInteger()
            def player = p(name)
            if (player) { player.data.plotMaxOwned = qty; "$player.name can own $qty plots" } else { "$name not found/online" }
        }
        else {
            def qty = args[1].toInteger()
            r.global.plotMaxOwned = qty
            "Default maximum owned plots is now $global.plotMaxOwned"
        }
    }
    else "Maximum owned plots is ${global.plotMaxOwned?: defaultMaxOwned}"
}


[
    (Event.Type.PLAYER_JOIN): { GroovyRunner r, PlayerJoinEvent e ->
        if (r.plots().plotProtection) {
            r.player.sendMessage "This server has plot protection, see '/plot help'"
        }
    }
]