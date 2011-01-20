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


class GroovyPlugin extends JavaPlugin
{
	static Logger log = Logger.getLogger("Minecraft")

	def playerListener = new GroovyPlayerListener(this)


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



	def register(Map listener, def priority = Priority.Normal) {
		Event.Type.values().each { type ->
			def methodName = type.toString()
			methodName = 'on' + methodName.split('_').collect {it.toLowerCase().capitalize()}.join('')
			if (listener."$methodName") {
				log.info("registering event listener $type for $methodName")
				switch (type.category) {
					case Event.Category.PLAYER :
						server.pluginManager.registerEvent(type, listener as PlayerListener, priority, this)
						break
					case Event.Category.BLOCK:
						server.pluginManager.registerEvent(type, listener as BlockListener, priority, this)
						break
					case Event.Category.LIVING_ENTITY:
						server.pluginManager.registerEvent(type, listener as EntityListener, priority, this)
						break
					case Event.Category.VEHICLE:
						server.pluginManager.registerEvent(type, listener as VehicleListener, priority, this)
						break
					case Event.Category.WORLD:
						server.pluginManager.registerEvent(type, listener as WorldListener, priority, this)
						break
					case Event.Category.SERVER:
						server.pluginManager.registerEvent(type, listener as ServerListener, priority, this)
						break
				}
			}
		}
	}




}
