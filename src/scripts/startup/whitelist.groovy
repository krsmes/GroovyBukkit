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



// command implementation
command 'whitelist', { runner, args ->
    def glob = runner.global
    args.each {
        if (it == 'on') {
            glob.whitelistEnabled = true
        }
        else if (it == 'off') {
            glob.whitelistEnabled = false
        }
        else if (it.startsWith('-')) {
            def playerName = it.substring(1)
            glob.whitelist?.remove(playerName)
            def p = p(playerName)
            if (p) {
                def msg = glob.whitelistMessage ?: "Hey dude, you're not on the list"
                p.kickPlayer(msg)
            }
        }
        else {
            if (!glob.whitelist) glob.whitelist = []
            glob.whitelist.add(it)
        }
    }
    "enabled=$glob.whitelistEnabled $glob.whitelist"
}

// event handlers necessary to support whitelist
[
	(Event.Type.PLAYER_LOGIN): { PlayerLoginEvent e ->
        def msg = global.whitelistMessage ?: "Hey dude, you're not on the list"
		if (global.whitelistEnabled && !global.whitelist.contains(e.player.name))
			e.disallow(PlayerLoginEvent.Result.KICK_OTHER, msg)
	}
]

*/