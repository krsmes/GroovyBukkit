import net.krsmes.bukkit.groovy.GroovyRunner
import org.bukkit.event.player.PlayerChatEvent

listen 'chats', 'player chat', { GroovyRunner r, PlayerChatEvent e ->
    def recip = e.recipients
    def msg = e.message

    if (msg.startsWith('@')) {
        int sp = msg.indexOf(' ')
        if (sp == 1) {
            // all players in your plot
            recip.clear()
            def players = r.server.onlinePlayers.findAll { it.data.plot == e.player.data.plot }
            recip.addAll(players)
            e.message = msg[sp + 1..-1]
        }
        else if (sp > 1) {
            // send to specified players
            recip.clear()
            def atName = msg[1..sp-1]
            atName.split('@').each { toName ->
                def toPlayer = p(toName)
                if (!toPlayer) {
                    e.player.sendMessage "Unable to find '$toName'"
                }
                else {
                    recip << toPlayer
                }
            }
            if (!recip) {
                e.cancelled = true
            }
            else {
                recip << e.player
                e.message = msg[sp + 1..-1]
            }
        }
    }
}