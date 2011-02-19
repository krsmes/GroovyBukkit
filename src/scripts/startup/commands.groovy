command 'permit', { player, args ->
    def permissions = global.permissions ?: [:]
    def players = []
    args.each {
        def negative = it.startsWith('-')
        def arg = negative ? it.substring(1) : it
        def pArg = p(arg)
        if (pArg && negative) {
            // remove all permissions for player pArg
            permissions.remove(arg)
        }
        else if (pArg) {
            // add this player to the temp list
            players << arg
        }
        else if (players) {
            // go through each player and add or remove the permission (arg)
            players.each {
                def permList = permissions[it] ?: []
                if (negative) {
                    permList.remove(arg)
                }
                else {
                    permList.add(arg)
                }
                permissions[it] = permList
            }
        }
    }
    global.permissions = permissions
}