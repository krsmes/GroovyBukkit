import org.bukkit.event.Event
import org.bukkit.event.player.*
import net.krsmes.bukkit.groovy.GroovyRunner

def playerStatsToString = { stats ->
    "Stats[onlineTime(${stats.totalOnlineTime / 60000}, last=${stats.lastOnlineTime / 60000}), logins($stats.loginCount, last=$stats.lastLogin, first=$stats.firstLogin), kicks($stats.kickCount), respawns($stats.respawnCount), chats($stats.chatCount)]"
}

def getPlayerStats(player) {
    playerStats = player.data.stats
    if (!playerStats) {
        playerStats = newPlayerStats()
        player.data.stats = playerStats
    }
    playerStats
}


def newPlayerStats() {
    def now = new Date()
    [
        firstLogin: now,
        lastIP: null,
        lastQuit: null,
        lastOnlineTime: 0,
        totalOnlineTime: 0,
        loginCount: 0,
        lastLogin: null,
        respawnCount: 0,
        lastRespawn: null,
        chatCount: 0,
        lastChat: null,
        lastChatMessage: null,
        kickCount: 0,
        lastKick: null,
        rank: 0
    ]
}


def processAtMessage(msg, runner, event) {
    def recip = event.recipients
    int sp = msg.indexOf(' ')
    if (sp == 1) {
        // all players in your plot
        recip.clear()
        def players = runner.server.onlinePlayers.findAll { it.data.plot == e.player.data.plot }
        recip.addAll(players)
        event.message = msg[sp + 1..-1]
    }
    else if (sp > 1) {
        // send to specified players
        recip.clear()
        def name = msg[1..sp - 1]
        name.split('@').each { toName ->
            def toPlayer = runner.p(toName)
            if (!toPlayer) {
                event.player.sendMessage "Unable to find '$toName'"
            }
            else {
                recip << toPlayer
            }
        }
        if (!recip) {
            event.cancelled = true
        }
        else {
            recip << event.player
            event.message = msg[sp + 1..-1]
        }
    }
}


def processStarMessage(msg, runner, event) {
    def recip = event.recipients
    int sp = msg.indexOf(' ')
    if (sp == 1) {
        // all players on your team
    }
    else if (sp > 1) {
        // all players on named team
        def name = msg[1..sp - 1]
    }
    // TODO
}


command 'stats', { runner, args ->
    def player = runner.player
    def stats = getPlayerStats(args ? runner.p(args[0]) : player)
    if (stats) {
        player.sendMessage "First login: $stats.firstLogin"
        player.sendMessage "Last login: $stats.lastLogin"
        player.sendMessage "Last online time: ${stats.lastOnlineTime / 60000} m, total: ${stats.totalOnlineTime / 60000} m"
        player.sendMessage "$stats.loginCount logins, $stats.kickCount kicks, $stats.respawnCount respawns, $stats.chatCount chats"
        "$name ${runner.p(name)?.online ? 'has been online for '+((System.currentTimeMillis() - stats.lastLogin.time) / 60000)+' minutes' : 'has been offline since '+stats.lastQuit}"
    }
    else "Unable to find stats"
}


[
    (Event.Type.PLAYER_LOGIN): { PlayerLoginEvent e ->
        getPlayerStats(e.player).with {
            if (!rank) rank = 0
            loginCount++
            lastLogin = new Date()
        }
    },

    (Event.Type.PLAYER_JOIN): { PlayerJoinEvent e ->
        getPlayerStats(e.player).with {
            e.player.sendMessage "$loginCount logins, ${Math.round(totalOnlineTime / 60000)} minutes, $kickCount kicks, $respawnCount respawns, see '/stats'"
        }
    },

    (Event.Type.PLAYER_QUIT): { PlayerQuitEvent e ->
        def now = new Date()
        getPlayerStats(e.player).with {
            lastQuit = now
            lastOnlineTime = now.time - lastLogin.time
            totalOnlineTime += lastOnlineTime
        }
    },

    (Event.Type.PLAYER_KICK): { PlayerKickEvent e ->
        getPlayerStats(e.player).with {
            kickCount++
            lastKick = new Date()
        }
    },

    (Event.Type.PLAYER_RESPAWN): { PlayerRespawnEvent e ->
        getPlayerStats(e.player).with {
            respawnCount++
            lastRespawn = new Date()
        }
    },

    (Event.Type.PLAYER_CHAT): { GroovyRunner r, PlayerChatEvent e ->
        getPlayerStats(e.player).with {
            chatCount++
            lastChat = new Date()
            lastChatMessage = e.message
        }

        def msg = e.message
        if (msg.startsWith('@')) {
            processAtMessage(msg, r, e)
        }
        else if (msg.startsWith('*')) {
            processStarMessage(msg, r, e)
        }
    }
]

