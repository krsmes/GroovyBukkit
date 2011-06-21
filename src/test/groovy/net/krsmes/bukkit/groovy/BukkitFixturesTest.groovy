package net.krsmes.bukkit.groovy

import org.junit.Test
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player

class BukkitFixturesTest {

    @Test
    void testFixtures() {
        def server = BukkitFixtures.makeServer([name: 'testServer'])
        assert server instanceof Server
        assert server.name == 'testServer'
        assert server.getName() == 'testServer'

        def world = BukkitFixtures.makeWorld([name: 'testWorld'])
        assert world instanceof World
        assert world.getName() == 'testWorld'

        def player = BukkitFixtures.makePlayer([name: 'testPlayer'])
        assert player instanceof Player
        assert player.getName() == 'testPlayer'

        // check preconfigured server
        server = BukkitFixtures.server
        assert server.getWorlds()
        assert server.getWorld('testWorld')
        assert server.getOnlinePlayers() instanceof Player[]
        assert server.getPluginManager()
        server.getPluginManager().registerEvent(null, null, null, null, null)
    }

}
