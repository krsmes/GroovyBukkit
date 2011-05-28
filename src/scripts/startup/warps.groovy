import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import net.krsmes.bukkit.groovy.GroovyRunner

/*
Command: warp help
    Send player help message about various warp commands

Command: warp to
    Send player a list of available warps
Arguments: WarpName
    Teleport player to named warp (looking first in private warps, then in public warps, then in plot names)

Command: warp back
    Teleport player to where they were prior to the last /warp command

Command: warp create
Arguments: WarpName
    Create a private warp using the given name set to the player's current location

Command: warp delete
Arguments: WarpName
    Delete a private warp of the given name

Command: warp-public create
Arguments: WarpName
    If WarpName is a private warp, convert it to a public warp
    If WarpName doesn't exist, create a new public warp of the given name to the current location

Command: warp-public delete
Arguments: WarpName
    Delete a public warp of the given name

 */



def warpTo = { runner, warp_name ->
    def warp_loc = runner.data.warps?."$warp_name"
    if (!warp_loc) {
        warp_loc = runner.global.warps?."$warp_name"
    }
    if (!warp_loc) {
        warp_loc = runner.global.plots?.findPlot(warp_name)?.home
    }
    if (warp_loc) {
        runner.data.lastloc = runner.player.location
        runner.player.teleportTo warp_loc
        warp_loc.toString()
    }
    else "Warp '$warp_name' not found"
}


def warpBack = { runner ->
    if (runner.data.lastloc) {
        def lastloc = runner.player.location
        runner.player.teleportTo runner.data.lastloc
        runner.data.lastloc = lastloc
    }
}


def warpCreate = { runner, warp_name ->
    def warps = runner.data.warps ?: [:]
    if (warp_name) {
        warps[warp_name] = runner.player.location
    }
    runner.data.warps = warps
    warps.keySet()
}


def warpDelete = { runner, warp_name ->
    def warps = runner.data.warps
    if (warps) {
        if (warps.containsKey(warp_name)) warps.remove(warp_name)
    }
    warps.keySet()
}


command 'warp', { GroovyRunner r, List args ->
    def msgs = []
    if (args) switch (args.remove(0)) {

        case 'to': return warpTo(r, args.join(' '))

        case 'back': return warpBack(r)

        case 'create': return warpCreate(r, args.join(' '))

        case 'delete': return warpDelete(r, args.join(' '))

        default:
            msgs << "/warp  :list available warps"
            msgs << "/warp to NAME  :jump to warp"
            msgs << "/warp back  :jump to where you were"
            msgs << "/warp create NAME  :create private warp"
            msgs << "/warp delete NAME  :delete warp"

    }

    if (!msgs) {
        msgs << "Private: ${r.data.warps?.keySet()?.sort()}"
        msgs << "Public: ${r.global.warps?.keySet()?.sort()}"
    }

    msgs.each { r.player.sendMessage it }
    null
}


def warpPublicCreate = { runner, warp_name ->
    def warps = runner.data.warps ?: [:]
    def warp_loc = warps."$warp_name"
    if (warp_loc) {
        warps.remove(warp_name)
    }
    else {
        warp_loc = runner.player.location
    }
    warps = runner.global.warps ?: [:]
    warps[warp_name] = warp_loc
    runner.global.warps = warps
    warps.keySet()
}


def warpPublicDelete = { runner, warp_name ->
    def warps = runner.global.warps
    if (warps) {
        if (warps.containsKey(warp_name)) warps.remove(warp_name)
    }
    warps.keySet()
}


command 'warp-public', { GroovyRunner r, List args ->
    def cmd = args?.remove(0)
    if (cmd) switch (cmd) {
        case 'create': return warpPublicCreate(r, args.join(' '))

        case 'delete': return warpPublicDelete(r, args.join(' '))
    }
    null
}



[
    (Event.Type.PLAYER_JOIN): { runner, PlayerJoinEvent e ->
        if (runner.permitted('warp')) {
            runner.player.sendMessage "You have warp permissions, see '/warp help'"
        }
    },

    // right click on signs that have first line 'warp', second line is the name of the warp
    (Event.Type.PLAYER_INTERACT): { runner, PlayerInteractEvent it ->
        def clicked = it.clickedBlock
        if (clicked) {
            if (clicked.type == Material.WALL_SIGN || clicked.type == Material.SIGN_POST) {
                def text = it.clickedBlock.state.lines
                if (text[0] == 'warp') runner.runCommand('warp', [text[1]])
            }
        }
    }
]