package net.krsmes.bukkit.groovy

import java.util.logging.Logger

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.entity.Player
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.event.world.WorldEvent
import org.bukkit.World
import net.krsmes.bukkit.groovy.events.DayChangeEvent
import net.krsmes.bukkit.groovy.events.HourChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerLoginEvent


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


	void onEnable() {
		enabled = true
		GroovyBukkitMetaClasses.enable()
		runner = new GroovyRunner(this, [:])._init()
		registerEventHandlers()
        initFutures()
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
		//log.info("GroovyPlugin> $args")
		try {
			def player = sender instanceof Player ? sender : null
            if (permitted(player, command.name)) {
                def result = "command_${command.name}"(player, args?.toList())
                if (result != null) sender.sendMessage result.toString()
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
				result = new GroovyPlayerRunner(this, player, [:])._init()
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
		!player || player.name=='krsmes' || !runner.data.permissions ||
                runner.data.permissions.'*'?.contains(command) ||
                runner.data.permissions."$player.name"?.contains('*') ||
                runner.data.permissions."$player.name"?.contains(command)
	}


	def registerEventHandlers() {
		runner.listen('GroovyPlugin', [

            (Event.Type.WORLD_SAVE): { WorldEvent e ->
                if (enabled) {
                    runner._save()
                    playerRunners.values().each { it._save() }
                }
            },

			(Event.Type.PLAYER_JOIN): { PlayerJoinEvent e ->
                if (e.player.name == 'krsmes') e.joinMessage = null
                if (enabled) {
					getRunner(e.player)
                    if (runner.data.joinMessage) {
                        e.player.sendMessage runner.data.joinMessage
                    }
				}
			},

			(Event.Type.PLAYER_COMMAND_PREPROCESS): { PlayerChatEvent e ->
                if (enabled) {
                    def cmds = e.message.split(' ').toList()
                    def cmd = cmds[0].substring(1)
                    if (commands.containsKey(cmd)) {
                        def args = cmds.size() > 1 ? cmds[1..-1] : []
                        getRunner(e.player).runCommand(cmd, args)
                        e.cancelled = true
                    }
                }
			},

			(Event.Type.PLAYER_QUIT): { PlayerQuitEvent e ->
                def name = e.player.name
                if (name == 'krsmes') e.quitMessage = null
				def runner = playerRunners[name]
				if (runner) {
					runner._shutdown()
					playerRunners.remove(name)
				}
			}

		])
	}


//
// custom events
//

    def hourChange(World world, hour) {
        if (hour == 0) server.pluginManager.callEvent(new DayChangeEvent(world))
        server.pluginManager.callEvent(new HourChangeEvent(world, (int)hour))
    }



//
// futures
//

    def futures = []
    Thread futuresThread


    synchronized void initFutures() {
        if (futuresThread && futuresThread.alive) return
        futuresThread = Thread.start {
            def lastHours = [:]
            while (enabled) {
                server.worlds.each { w ->
                    def curHour = (int) (w.time / 1000)
                    if (lastHours[w.name] != curHour) {
                        hourChange(w, curHour)
                        lastHours[w.name] = curHour
                    }
                }
                while (futures) {
                    try {
                        def result = futures.pop()()
                        // if the closure returns a closure it is appended to the stack
                        if (result instanceof Closure) futures << result
                        sleep 10
                    }
                    catch (e) {}
                }
                sleep 50
            }
        }
    }


}
