package org.bukkit.plugin.groovy

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerListener
import java.util.logging.Logger
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector



public class GroovyPlayerListener extends PlayerListener
{
	static Logger log = Logger.getLogger("Minecraft")

	static SCRIPT_PREFIX = """
import org.bukkit.*;import org.bukkit.block.*;import org.bukkit.entity.*;import org.bukkit.inventory.*;import org.bukkit.material.*;import org.bukkit.util.*;
"""

	GroovyPlugin plugin


	GroovyPlayerListener(GroovyPlugin instance) {
		plugin = instance;
	}


	@Override
	void onPlayerCommand(PlayerChatEvent event) {
		def command = event.message.split(' ').toList()
		Player player = event.player

		def action = command[0].substring(1)
		try {
			"$action"(command, player)
			event.cancelled = true
		}
		catch (e) {}
	}


//
// commands
//

	def g = { def command, Player player ->
		def result = runScript(player, command[1..-1].join(' '))
		if (player) {
			player.sendMessage result
		}
	}

	def gg = { def command, Player player ->
		def scriptLoc = plugin.getPlayerVariable(player, 'scripts')
		if (!scriptLoc) {
			scriptLoc = 'scripts/'
		}
		def scriptName = scriptLoc + command[1] + '.groovy'
		def result = runFile(player, scriptName, command.size()>2?command[2..-1]:null)
		player.sendMessage result
	}


//
// helper functions
//



	def runScript = { Player p, String script ->
		log.info("Executing script: $script")
		run(p, SCRIPT_PREFIX+script)
	}

	def runFile = { Player p, String scriptName, def args ->
		try {
			URL u = scriptName.toURL()
			def script = u.text
			if (script) {
				log.info("Executing url: $scriptName")
				run(p, script, args)
			}
			else {
				"unable to run $scriptName"
			}
		}
		catch (e) {
			if (scriptName.startsWith('http:')) {
				"unable to find url $scriptName"
			}
			else {
				def file = new File(scriptName)
				if (file.exists()) {
					log.info("Executing file: $file")
					run(p, file, args)
				}
				else {
					"unable to find file $scriptName"
				}
			}
		}
	}

	def run = { Player p, def script, def args = null ->
		def result
		try {
			def shell = newShell(p)
			shell.context.setVariable('args', args)
			result = shell.evaluate(script)
		}
		catch (e) {
			result = e.message
		}
		result ? result.toString() : 'null'
	}


	def newShell = { Player p ->
		def shell = new GroovyShell()
		def context = shell.context
		context.setVariable('log', log)
		context.setVariable('s', plugin.server)

		def op = [:]
		plugin.server.onlinePlayers.each { op[it.name] = it }
		context.setVariable('op', op)

		context.setVariable('p', p)
		if (p) {
			context.setVariable('global', plugin.globalData)
			context.setVariable('data', plugin.getPlayerData(p))
			def world = p.world
			context.setVariable('w', world)

			Location location = p.location
			context.setVariable('l', location)
			context.setVariable('pitch', location.pitch)

			def yaw = location.yaw % 360
			if (yaw < 0) yaw += 360
			context.setVariable('yaw', yaw)
			BlockFace facing = yaw <= 25 ? BlockFace.WEST : yaw < 65 ? BlockFace.NORTH_WEST : yaw <= 115 ? BlockFace.NORTH : yaw < 155 ? BlockFace.NORTH_EAST : yaw <= 205 ? BlockFace.EAST : yaw < 245 ? BlockFace.SOUTH_EAST : yaw <= 315 ? BlockFace.SOUTH : yaw < 335 ? BlockFace.SOUTH_WEST : BlockFace.WEST
			context.setVariable('f', facing)

			Vector vector = new Vector(location.x, location.y-1.0, location.z)
			context.setVariable('v', vector)

			def x = vector.blockX
			def y = vector.blockY
			def z = vector.blockZ
			context.setVariable('x', x)
			context.setVariable('y', y)
			context.setVariable('z', z)

			context.setVariable('b', world.getBlockAt(x, y, z))
			context.setVariable('highY', world.getHighestBlockYAt(x, z))
			context.setVariable('bY', (0..128).collect {world.getBlockAt(x,it,z)})
		}
		context.setVariable('g', plugin)
		shell
	}


}