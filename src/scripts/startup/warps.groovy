import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerInteractEvent

command 'warp', { runner, args ->
    def warp_name = args.join(' ')
    if (warp_name) {
        def warp_loc = runner.data.warps?."$warp_name"
        if (!warp_loc) {
            warp_loc = runner.global.warps?."$warp_name"
        }
        if (warp_loc) {
            runner.data.lastloc = runner.player.location
            runner.player.teleportTo warp_loc
            warp_loc.toString()
        }
        else "Warp '$warp_name' not found"
    }
    else "Private: ${runner.data.warps?.keySet()} -- Public: ${runner.global.warps?.keySet()}"
}


command 'warp-back', { runner, args ->
    if (runner.data.lastloc) {
        def lastloc = runner.player.location
        runner.player.teleportTo runner.data.lastloc
        runner.data.lastloc = lastloc
    }
}


command 'warp-create', { runner, args ->
    def warp_name = args.join(' ')
    def warps = runner.data.warps ?: [:]
    if (warp_name) {
        warps[warp_name] = runner.player.location
    }
    runner.data.warps = warps
    warps.keySet()
}


command 'warp-delete', { runner, args ->
    def warp_name = args.join(' ')
    def warps = runner.data.warps
    if (warps) {
        if (warps.containsKey(warp_name)) warps.remove(warp_name)
    }
    warps.keySet()
}


command 'warp-public', { runner, args ->
    def warp_name = args.join(' ')
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


command 'warp-public-delete', { runner, args ->
    def warp_name = args.join(' ')
    def warps = runner.global.warps
    if (warps) {
        if (warps.containsKey(warp_name)) warps.remove(warp_name)
    }
    warps.keySet()
}


[
    // right click on signs that have first line 'warp', second line is the name of the warp
    (Event.Type.PLAYER_INTERACT): { runner, PlayerInteractEvent it ->
        if (it.clickedBlock.type == Material.WALL_SIGN) {
            def text = it.clickedBlock.state.lines
            if (text[0] == 'warp') runner.runCommand('warp', [text[1]])
        }
    }
]