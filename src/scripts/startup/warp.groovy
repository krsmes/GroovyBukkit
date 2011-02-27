command 'warp', { player, args ->
    def warp_name = args.join(' ')
    def warp_loc = data.warps?."$warp_name"
    log "$warp_name=$warp_loc"
    if (warp_loc) {
        player.teleportTo warp_loc
        "$warp_loc"
    }
    else "Warp '$warp_name' not found"
}


command 'warp-create', { player, args ->
    def warp_name = args.join(' ')
    def warps = data.warps ?: [:]
    warps[warp_name] = player.location
    data.warps = warps
}


command 'warp-delete', { player, args ->
    def warp_name = args.join(' ')
    def warps = data.warps
    if (warps) {
        if (warps.contains(warp_name)) warps.remove(warp_name)
    }
}


command 'warp-public', { player, args ->
    def warp_name = args.join(' ')

}


command 'warp-public-delete', { player, args ->
    def warp_name = args.join(' ')

}
