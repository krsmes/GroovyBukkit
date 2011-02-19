import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.Event

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

[
	(Event.Type.PLAYER_LOGIN): { PlayerLoginEvent e ->
		if (global.whitelistEnabled && !global.whitelist.contains(e.player.name))
			e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Hey dude, you're not on the whitelist")
	}
]
