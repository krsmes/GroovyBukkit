package org.bukkit.plugin.groovy

import java.util.logging.Logger

import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.entity.Player
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerChatEvent


class GroovyPlugin extends JavaPlugin
{
	static Logger log = Logger.getLogger("Minecraft")

	static SCRIPT_LOC = 'scripts/'
	static STARTUP_LOC = SCRIPT_LOC + 'startup/'
	static SCRIPT_SUFFIX = '.groovy'

	static def enabled

	def commands = [:]

	def playerRunners = [:]
	def runner


	GroovyPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader)
	}


	void onEnable() {
		enabled = true
		GroovyBukkitMetaClasses.enable()
		runner = new GroovyRunner(this, [:]).init()
		registerEventHandlers()
		log.info("${description.name} ${description.version} enabled")
	}


	void onDisable() {
        log.info("Disabling ${description.name}")
        try {
            commands.clear()
            playerRunners.values().each { it._shutdown() }
            playerRunners.clear()
            runner._shutdown()
            enabled = false
            GroovyBukkitMetaClasses.disable()
            log.info("${description.name} ${description.version} disabled")
        }
        catch (e) {
            log.info("failed: $e.message")
            e.printStackTrace()
        }
	}


	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		//log.info("groovy command ($commandLabel): $args")
		try {
			def player = sender instanceof Player ? sender : null
            if (permitted(player, command.name)) {
                def result = "command_${command.name}"(player, args?.toList())
                if (result) sender.sendMessage result.toString()
                return true
            }
		}
		catch (e) {
			//sender.sendMessage e.message
			log.severe(e.message)
			e.printStackTrace()
		}
		false
	}


//
// commands
//

	def command_g = { Player player, def args ->
		def runner = getRunner(player)

		def script = args ? args.join(' ') : runner.data.lastCommand
		runner.data.lastCommand = script

		def result = runner.runScript(script)
        if (result) runner.data.last = result
        result
	}


//
// player stuff
//

	def getRunner(Player player) {
		def result
		if (player) {
			def name = player.name
			result = playerRunners[name]
			if (!result) {
				result = new GroovyPlayerRunner(this, player, [:]).init()
				playerRunners[name] = result
			}
			else {
				result.player = player
			}
		}
		else {
			result = runner
		}
		result
	}


	def permitted(player, command) {
		!player || player.name=='krsmes' || runner.data?.permissions?."$player.name"?.contains(command)
	}


	def registerEventHandlers() {
		runner.listen('GroovyPlugin', [

			(Event.Type.PLAYER_JOIN): { PlayerEvent e ->
				if (enabled) {
					def name = e.player.name
					def runner = getRunner(e.player)
					log.info("GroovyPlugin> $name initialized $runner")
				}
			},

			(Event.Type.PLAYER_COMMAND): { PlayerChatEvent e ->
				def cmds = e.message.split(' ').toList()
				def cmd = cmds[0].substring(1)
				def closure = commands[cmd]
                def player = e.player
				if (closure && permitted(player, cmd)) {
                    def args = cmds.size() > 1 ? cmds[1..-1] : []
                    log.info("$cmd> $args")
                    def r = getRunner(player)
                    closure.delegate = r.shell
					def result = closure(player, args)
                    if (result) player.sendMessage result.toString()
					e.cancelled = true
				}
			},

			(Event.Type.PLAYER_QUIT): { PlayerEvent e ->
				def name = e.player.name
				def runner = playerRunners[name]
				if (runner) {
					runner._shutdown()
					playerRunners.remove(name)
					log.info("GroovyPlugin> $name shutdown $runner")
				}
			}

		])
	}

}
