command 'warp', { runner, args ->
    def warp_name = args.join(' ')
    def warp_loc = runner.data.warps?."$warp_name"
    if (!warp_loc) {
        warp_loc = runner.global.warps?."$warp_name"
    }
    if (warp_loc) {
        runner.player.teleportTo warp_loc
        "$warp_loc"
    }
    else "Warp '$warp_name' not found"
}


command 'warp-create', { runner, args ->
    def warp_name = args.join(' ')
    def warps = runner.data.warps ?: [:]
    warps[warp_name] = runner.player.location
    runner.data.warps = warps
}


command 'warp-delete', { runner, args ->
    def warp_name = args.join(' ')
    def warps = runner.data.warps
    if (warps) {
        if (warps.contains(warp_name)) warps.remove(warp_name)
    }
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
}


command 'warp-public-delete', { runner, args ->
    def warp_name = args.join(' ')
    def warps = runner.global.warps
    if (warps) {
        if (warps.contains(warp_name)) warps.remove(warp_name)
    }
}
