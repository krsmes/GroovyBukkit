import org.bukkit.event.Event
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.block.BlockCanBuildEvent
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import net.krsmes.bukkit.groovy.Plot


def findPlot(x, z) {
    global.plots?.find { k, v -> v.contains(x, z) }
}


command 'plot-create', { runner, args ->

    if (runner.data.stickClicks?.size() > 1 && args?.size() > 0) {
        def plot_name = args.join(' ')
        def plot_loc1 = runner.data.stickClicks[0]
        def plot_loc2 = runner.data.stickClicks[1]
        def plots = runner.global.plots ?: [:]
        plots[plot_name] = plot(plot_loc1, plot_loc2)
        runner.global.plots = plots
        plots
    }
    else
        runner.player.sendMessage "need to stick-clicks (${runner.data.stickClicks?.size()}) and a name (${args.size()})"

}


listen 'temp', [

    (Event.Type.BLOCK_CANBUILD): { BlockCanBuildEvent e ->
        def block = e.block
        def plot = findPlot(block.x, block.z)
        if (plot) e.buildable = false
    },

    (Event.Type.BLOCK_BREAK): { BlockBreakEvent e ->
        def block = e.block
        def plot = findPlot(block.x, block.z)
        if (plot) e.cancelled = true
    },

    (Event.Type.BLOCK_DAMAGE): { BlockDamageEvent e ->
        def block = e.block
        def plot = findPlot(block.x, block.z)
        if (plot) e.cancelled = true
    },

    (Event.Type.PLAYER_MOVE): { PlayerMoveEvent e ->
        def from = e.from
        def to = e.to

        def fromX = from.blockX
        def fromZ = from.blockZ
        def toX = to.blockX
        def toZ = to.blockZ
        if (fromX != toX || fromZ != toZ) {
            debug "temp: $e.eventName ($e.player.name): from=$from, to=$to"

            def fromChunk = from.block.chunk
            def toChunk = to.block.chunk

            if (toChunk != fromChunk) {
                e.player.sendMessage("Now in $toChunk")
            }

            def fromPlot = findPlot(fromX, fromZ)
            def toPlot = findPlot(toX, toZ)

            if (fromPlot != toPlot) {
                e.player.sendMessage(toPlot ? "Welcome to $toPlot" : "Thanks for visiting $fromPlot")
            }
        }

    }

]