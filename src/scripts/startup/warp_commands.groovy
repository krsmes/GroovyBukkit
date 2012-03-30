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



def warpTo = { r, warp_name ->
    def warp_loc = r.data.warps?."$warp_name"
    if (!warp_loc) {
        warp_loc = r.global.warps?."$warp_name"
    }
    if (!warp_loc) {
        warp_loc = r.plots().findPlot(warp_name)?.home
    }
    if (warp_loc) {
        r.data.lastloc = r.player.location
        r.player.teleport warp_loc
        warp_loc.toString()
    }
    else "Warp '$warp_name' not found"
}


def warpBack = { r ->
    if (r.data.lastloc) {
        def lastloc = r.player.location
        r.player.teleport r.data.lastloc
        r.data.lastloc = lastloc
    }
}


def warpCreate = { r, warp_name ->
    def warps = r.data.warps ?: [:]
    if (warp_name) {
        warps[warp_name] = r.player.location
    }
    r.data.warps = warps
    warps.keySet()
}


def warpDelete = { r, warp_name ->
    def warps = r.data.warps
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


def warpPublicCreate = { r, warp_name ->
    def warps = r.data.warps ?: [:]
    def warp_loc = warps."$warp_name"
    if (warp_loc) {
        warps.remove(warp_name)
    }
    else {
        warp_loc = r.player.location
    }
    warps = r.global.warps ?: [:]
    warps[warp_name] = warp_loc
    r.global.warps = warps
    warps.keySet()
}


def warpPublicDelete = { r, warp_name ->
    def warps = r.global.warps
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
    (PlayerJoinEvent): { GroovyRunner r, PlayerJoinEvent e ->
        if (r.permitted('warp')) {
            r.player.sendMessage "You have warp permissions, see '/warp help'"
        }
    },

    // right click on signs that have first line 'warp', second line is the name of the warp
    (PlayerInteractEvent): { GroovyRunner r, PlayerInteractEvent it ->
        def clicked = it.clickedBlock
        if (clicked) {
            if (clicked.type == Material.WALL_SIGN || clicked.type == Material.SIGN_POST) {
                def text = it.clickedBlock.state.lines
                if (text[0] == 'warp') r.runCommand('warp', ['to', text[1]])
            }
        }
    }
]