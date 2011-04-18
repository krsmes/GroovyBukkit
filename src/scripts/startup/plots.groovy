import org.bukkit.event.Event
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.block.Action

def playerPlot(player, Closure c) {
    def plot = findPlot(player)
    (plot && ((plot.public && player.op) || (!plot.public && (plot.owner == player.name)))) ? c(plot) : "Plot '${plot?.name}' not owned by $player.name"
}


command 'plot-help', { runner, args ->
    [
        "/plot  : show info about current plot",
        "/plot list  : list all plots",
        "/plot on|off  : show|hide entering/leaving plots",
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
            return "Show plot while moving is ${runner.data.plotShow?'on':'off'}"
        }
        else if (args[0] == 'list') {
            return runner.global?.plots?.data?.keySet()?.toString()
        }
        return "Unknown plot command: ${args[0]}"
    }
    def plot = findPlot(runner.player)
    if (plot) {
        [
            "Plot: $plot.name (${plot.open?'open':'closed'} land)",
            "Size: $plot.size",
            "Owner: ${plot.owner ?: 'no one'}",
            "Visitor: $plot.visitors"
        ].each { runner.player.sendMessage it }
        "You are ${plot.allowed(runner.player) ? '' : 'not '}allowed to work here"
    }
    else
        "You are not in a plotted area"
}

command 'plot-claim', { runner, args ->
    def plot = findPlot(runner.player)
    if (plot && !plot.public)
        if (!plot.owner) {
            def ownedPlots = findOwnedPlots(runner.player)
            def allowed = runner.data.plotsAllowed?.toInteger() ?: 1
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
    "Plot protection is ${global.plotProtection?'on':'off'}"
}


command 'plot-create', { runner, args ->
    if (runner.data.stickClicks?.size() > 1 && args?.size() > 0) {
        def plot_name = args.join(' ')
        if (plot_name.equalsIgnoreCase('public'))
            "You cannot create plot named 'public"
        else {
            if (!findPlots(plot_name)) {
                def plot_loc1 = runner.data.stickClicks[0]
                def plot_loc2 = runner.data.stickClicks[1]
                def a = area(plot_loc1, plot_loc2)
                if (plot(plot_name, a))
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
            def plot = findPlot(plot_name)
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
        def plot = findPlot(runner.player)
        if (plot && !plot.public) {
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
        def plot = findPlot(plot_name)
        if (plot) {
            removePlot(plot_name)
            "Plot '$plot_name' has been deleted"
        }
        else "Plot '$plot_name' not found"
    }
    else "No plot name specified"
}



def breakableTypeIds = [17, 18, 37, 38, 39, 40, 59, 81, 83, 86]
def placeableTypeIds = [6, 18, 37, 38, 39, 40, 59, 81, 83, 86, 295, 338, 354]
def interactableTypeIds = [26, 54, 58, 61, 64, 69, 71, 77, 93, 94, 95]

[

    (Event.Type.PLAYER_JOIN): { runner, PlayerJoinEvent e ->
        if (!runner.data.containsKey('plotShow')) {
            runner.data.plotShow = true
            runner.player.sendMessage "This server has plot protection, see /plot-help"
        }
    },

    (Event.Type.BLOCK_DAMAGE): { BlockDamageEvent e ->
        if (global.plotProtection) {
            def block = e.block
            def plot = findPlot(block)
            if (plot?.public) {
                if (!(block.typeId in breakableTypeIds)) plot.processEvent(e)
            }
            else plot?.processEvent(e)
        }
    },

    (Event.Type.PLAYER_INTERACT): { PlayerInteractEvent e ->
        if (global.plotProtection) {
            def block = e.clickedBlock
            def plot = findPlot(block)
            if (plot?.public) {
                if (e.action == Action.RIGHT_CLICK_BLOCK &&
                    ((e.item?.typeId in placeableTypeIds) ||
                     (block.typeId in interactableTypeIds))) {}
                else plot.processEvent(e)
            }
            else plot?.processEvent(e)
        }
    }

]