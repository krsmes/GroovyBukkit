import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.Event

[
	(Event.Type.PLAYER_LOGIN): { PlayerLoginEvent e ->
		if (whitelistEnabled && !whitelist.contains(e.player.name))
			e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Hey dude, you're not on the whitelist")
	}
]
