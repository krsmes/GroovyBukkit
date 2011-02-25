import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.Event

/*
Command: whitelist

Arguments: on|off|[-]playername

Examples:

    whitelist on
        turn whitelist functionality on

    whitelist off
        turn whitelist functionality on

    whitelist krsmes
        add player 'krsmes' to the whitelist

    whitelist -krsmes
        remove player 'krsmes' from the whitelist, this also kicks krsmes if they are currently online

 */

// command implementation
command 'whitelist', { player, args ->
    args.each {
        if (it == 'on') {
            global.whitelistEnabled = true
        }
        else if (it == 'off') {
            global.whitelistEnabled = false
        }
        else if (it.startsWith('-')) {
            def playerName = it.substring(1)
            global.whitelist.remove(playerName)
            def p = p(playerName)
            if (p) {
                p.kickPlayer("Hey dude, you're not on the whitelist")
            }
        }
        else {
            global.whitelist.add(it)
        }
    }
    "enabled=$global.whitelistEnabled $global.whitelist"
}

// event handlers necessary to support whitelist
[
	(Event.Type.PLAYER_LOGIN): { PlayerLoginEvent e ->
		if (global.whitelistEnabled && !global.whitelist.contains(e.player.name))
			e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Hey dude, you're not on the whitelist")
	}
]
