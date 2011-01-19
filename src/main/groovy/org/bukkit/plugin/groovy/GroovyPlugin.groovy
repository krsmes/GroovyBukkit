package org.bukkit.plugin.groovy

import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger


class GroovyPlugin extends JavaPlugin
{
	static Logger log = Logger.getLogger("Minecraft")

	def playerListener = new GroovyPlayerListener(this)


	public GroovyPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader)
		registerEvents()
	}


	public void onEnable() {
		log.info("${description.name} ${description.version} enabled")
	}


	public void onDisable() {
		log.info("${description.name} ${description.version} disabled")
	}


	private void registerEvents() {
		server.pluginManager.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
		server.pluginManager.registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
	}

}
