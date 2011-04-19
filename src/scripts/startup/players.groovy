import org.bukkit.event.Event
import org.bukkit.event.player.*

def playerStatsToString = { stats ->
    "Stats[onlineTime(${stats.totalOnlineTime / 60000}, last=${stats.lastOnlineTime / 60000}), logins($stats.loginCount, last=$stats.lastLogin, first=$stats.firstLogin), kicks($stats.kickCount), respawns($stats.respawnCount), chats($stats.chatCount)]"
}

def getPlayerStats(player) {
    def playerStats = null
    def stats = global.stats
    if (!stats) {
        stats = [:]
        global.stats = stats
    }
    playerStats = stats[(player instanceof String)?player:player.name]
    if (!playerStats) {
        playerStats = newPlayerStats()
        stats[player.name] = playerStats
    }
    playerStats
}


def findPlayerStats(player) {
    def playerStats = null
    if (player) {
        def stats = global.stats
        if (stats) {
            playerStats = stats[(player instanceof String) ? player : player.name]
        }
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


command 'stats', { runner, args ->
    def player = runner.player
    def name = args ? args[0] : player.name
    def stats = findPlayerStats(name)
    if (!stats && name) { stats = findPlayerStats(p(name)) }  // try online partial name matching
    if (stats) {
        player.sendMessage "First login: $stats.firstLogin"
        player.sendMessage "Last login: $stats.lastLogin"
        player.sendMessage "Last online time: ${stats.lastOnlineTime / 60000} m, total: ${stats.totalOnlineTime / 60000} m"
        player.sendMessage "$stats.loginCount logins, $stats.kickCount kicks, $stats.respawnCount respawns, $stats.chatCount chats"
        "$name ${p(name)?.online ? 'has been online for '+((System.currentTimeMillis() - stats.lastLogin.time) / 60000)+' minutes' : 'has been offline since '+stats.lastQuit}"
    }
    else "Unable to find player $name"
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

    (Event.Type.PLAYER_CHAT): { PlayerChatEvent e ->
        getPlayerStats(e.player).with {
            chatCount++
            lastChat = new Date()
            lastChatMessage = e.message
        }
    }
]

