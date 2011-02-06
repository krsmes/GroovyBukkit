import org.bukkit.event.Event.Type as ET
import org.bukkit.event.player.PlayerLoginEvent

[
	(ET.PLAYER_LOGIN): { PlayerLoginEvent event ->
		s.onlinePlayers.each { it.sendMessage "Hey everyone, ${event.player.name} has logged in" }
	}
]