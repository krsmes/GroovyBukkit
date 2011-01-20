package org.bukkit.plugin.groovy

import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger
import org.bukkit.event.player.PlayerListener
import org.bukkit.event.block.BlockListener
import org.bukkit.event.entity.EntityListener
import org.bukkit.event.vehicle.VehicleListener
import org.bukkit.event.world.WorldListener
import org.bukkit.event.server.ServerListener
import org.bukkit.entity.Player


class GroovyPlugin extends JavaPlugin
{
	static Logger log = Logger.getLogger("Minecraft")

	def playerListener = new GroovyPlayerListener(this)

	def _playerData = [:]

	def globalData = [:]


	GroovyPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader)
		registerEvents()
	}


	void onEnable() {
		log.info("${description.name} ${description.version} enabled")
	}


	void onDisable() {
		log.info("${description.name} ${description.version} disabled")
	}


	void registerEvents() {
		server.pluginManager.registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
	}


	def setPlayerVariable = { Player player, def name, def value ->
		getPlayerData(player)."$name" = value
	}

	def getPlayerVariable = { Player player, def name ->
		getPlayerData(player)."$name"
	}

	def getPlayerData = { Player player ->
		def name = player.name
		def result = _playerData[name]
		if (!result) {
			result = [:]
			_playerData[name] = result
		}
		result
	}



	def register(String uniqueName, Map listener, Event.Priority priority = Priority.Normal) {
		def registered = globalData[uniqueName]
		if (registered) {
			registered.each { server.pluginManager.unregisterEvent(it) }
		}
		registered = []

		Event.Type.values().each { type ->
			def methodName = type.toString()
			methodName = 'on' + methodName.split('_').collect {it.toLowerCase().capitalize()}.join('')
			if (listener."$methodName") {
				def typedListener
				switch (type.category) {
					case Event.Category.PLAYER :
						typedListener = listener as PlayerListener
						break
					case Event.Category.BLOCK:
						typedListener = listener as BlockListener
						break
					case Event.Category.LIVING_ENTITY:
						typedListener = listener as EntityListener
						break
					case Event.Category.VEHICLE:
						typedListener = listener as VehicleListener
						break
					case Event.Category.WORLD:
						typedListener = listener as WorldListener
						break
					case Event.Category.SERVER:
						typedListener = listener as ServerListener
						break
				}

				if (typedListener) {
					log.info("Registering GroovyBukkit event listener $type for $methodName")
					registered << server.pluginManager.registerEvent(type, typedListener, priority, this)
				}
			}
		}

		globalData[uniqueName] = registered
	}


	def unregister(String uniqueName) {
		def registered = globalData[uniqueName]
		if (registered) {
			registered.each { server.pluginManager.unregisterEvent(it) }
		}
		globalData.remove(uniqueName)
	}


}
