package net.krsmes.bukkit.groovy

import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.plugin.PluginManager
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import org.bukkit.block.Block
import org.bukkit.Material
import org.bukkit.entity.LivingEntity

class BukkitFixtures {

    static Plugin plugin
    static Server server
    static World world
    static List<Player> players
    static int playerCounter

    static {
        players = []
        world = makeWorld(
            name: 'testWorld',
            players: players
        )
        server = makeServer(
            pluginManager: makePluginManager([:]),
            worlds: [world],
            getWorld: { name ->
                self.getWorlds().find{it.name==name}
            },
            getOnlinePlayers: {
                players.toArray(new Player[players.size()])
            }
        )
        plugin = makePlugin(
            enabled: true,
            server: server
        )
    }


    static Plugin makePlugin(Map attributes = [:]) {
        makeObject(Plugin, attributes)
    }


    static Server makeServer(Map attributes = [:]) {
        makeObject(Server, attributes)
    }


    static World makeWorld(Map attributes = [:]) {
        makeObject(World, attributes)
    }


    static PluginManager makePluginManager(Map attributes = [:]) {
        makeObject(PluginManager, attributes, [
            registerEvent: { a, b, c, d, e -> }
        ])
    }


    static Player makePlayer(Map attributes = [:]) {
        def newAttributes = [
            server: server,
            world: world,
            name: 'player' + (++playerCounter)
        ]
        newAttributes.putAll(attributes)
        makeEntity(Player, newAttributes)
    }


    static Location makeLocation(Map attributes = [:]) {
        //noinspection GroovyAssignabilityCheck
        new Location(attributes.world?:world,
                attributes.x?:0.0, attributes.y?:0.0, attributes.z?:0.0,
                attributes.yaw?:0.0, attributes.pitch?:0.0)
    }


    static <T extends Entity> T makeEntity(Class<T> type = LivingEntity, Map attributes = [:]) {
        makeObject(type, attributes, [
            getLastDamageCause: {-> self.lastDamageCause },
            setLastDamageCause: { self.lastDamageCause = it }
        ])
    }


    static Block makeBlock(Map attributes) {
        def w = attributes.world ?: world
        def x = attributes.x ?: attributes.location?.blockX ?: 0
        def y = attributes.y ?: attributes.location?.blockY ?: 0
        def z = attributes.z ?: attributes.location?.blockZ ?:0
        def location = attributes.location ?: makeLocation(world:w, x:x, y:y, z:z)
        def typeId = attributes.typeId ?: attributes.type.id ?: 0
        def type = Material.getMaterial(typeId)
        makeObject(Block, attributes, [
            world: w,
            x: x, y: y, z: z,
            location: location,
            typeId: typeId,
            type: type,
            data: (byte)0
        ])
    }


    static <T> T makeObject(Class<T> type, Map... attributes) {
        def map = [:]
        map.self = map
        attributes.reverse().each { attr ->
            map.putAll attr.collectEntries { key, value ->
                def closure = (value instanceof Closure) ? value : {-> value}
                closure.delegate = map
                if (value instanceof Closure)
                    [(key): value]
                else
                    [((value instanceof Boolean ? 'is' : 'get') + key.capitalize()): {-> value}]
            }
        }
        def result = map.asType(type)
        result.metaClass.toString = {-> attributes.toString() }
        result
    }

}
