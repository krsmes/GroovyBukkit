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
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.inventory.ItemStack


class GroovyPlugin extends JavaPlugin
{
	static Logger log = Logger.getLogger("Minecraft")

	def _playerData = [:]
	def globalData = [:]


	GroovyPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader)
	}


	void onEnable() {
		log.info("${description.name} ${description.version} enabled")
	}


	void onDisable() {
		log.info("${description.name} ${description.version} disabled")
	}


	boolean onCommand(Player player, Command cmd, String commandLabel, String[] args) {
		try {
			def result = "command_${cmd.name}"(player, getPlayerData(player), args?.toList())
			if (result) player.sendMessage result.toString()
			return true
		}
		catch (e) {
			player.sendMessage e.message
			log.severe(e.message)
		}
		return false
	}


//
// commands
//

	def command_g = { Player player, def data, def args ->
		def runner = data.runner
		if (!runner) {
			runner = new GroovyRunner(this, player, data)
			data.runner = runner
		}
		runner.runScript(args.join(' '))
	}


//
// data
//

	def getPlayerData = { Player player ->
		def name = player.name
		def result = _playerData[name]
		if (!result) {
			result = [:]
			_playerData[name] = result
		}
		result
	}


}
