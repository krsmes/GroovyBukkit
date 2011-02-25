/*
Command: permit

Arguments: _PlayerName command

Examples:

    permit krsmes whitelist
        add 'whitelist' command to krsmes permissions

    permit krsmes g whitelist
        add 'g' and 'whitelist' command to krsmes permissions

    permit krmses *
        add the special * (all) permission to krsmes

    permit krsmes -whitelist
        remove 'whitelist' command from krsmes permissions

    permit -krsmes
        remote all permissions for krsmes

    permit * whitelist
        add 'whitelist' command to all users

    permit * -whitelist
        remove 'whitelist' command from all users

 */
command 'permit', { player, args ->
    def permissions = global.permissions ?: [:]
    def playerName = null
    args.each {
        def negative = it.startsWith('-')
        def arg = negative ? it.substring(1) : it
        // check if arg is a player
        def nameArg = !playerName
        if (nameArg && negative) {
            // remove all permissions for player name
            permissions.remove(arg)
        }
        else if (nameArg) {
            // make the player name this arg
            playerName = arg
        }
        else if (playerName) {
            // go through each player in the temp list and add or remove the permission (arg)
            def permList = permissions[playerName] ?: []
            if (negative) {
                permList.remove(arg)
            }
            else {
                permList.add(arg)
            }
            permissions[playerName] = permList
        }
    }
    global.permissions = permissions  // in case it didn't exist before
}
