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
	GroovyPlugin plugin

	def _playerData = [:]


	GroovyPlayerListener(GroovyPlugin instance) {
		plugin = instance;
	}


	@Override
	void onPlayerJoin(PlayerEvent event) {
		log.info("${event.player.name} joined the groovy server! :D");
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




	def pos = { def command, Player player ->
		if (command.size() == 1) {
			Location location = player.location
			def x = (int) location.x
			def y = (int) location.y - 1
			def z = (int) location.z
			if (x < 0) x--
			if (z < 0) z--

			player.sendMessage("$x, $y, $z; yaw=${(int)location.yaw}, pitch=${(int)location.pitch}")
		}
		else if (command.size() == 4) {
			try {
				double x = Double.parseDouble(command[1])
				double y = Double.parseDouble(command[2])
				double z = Double.parseDouble(command[3])

				player.teleportTo(new Location(player.world, x, y, z))
			}
			catch (NumberFormatException ex) {
				player.sendMessage("Given location is invalid")
			}
		}
		else {
			player.sendMessage("Usage: '/pos' to get current position, or '/pos x y z' to teleport to x,y,z")
		}
	}


	def g = { def command, Player player ->
		def result = runScript(player, command[1..-1].join(' '))
		if (player) {
			player.sendMessage result
		}
	}

	def gg = { def command, Player player ->
		def scriptLoc = getPlayerVariable(player, 'scripts')
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




	def runScript = { Player p, String script ->
		log.info("Executing script: $script")
		run(p, 'import org.bukkit.*;'+script)
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
			context.setVariable('data', getPlayerData(p))
			def world = p.world
			context.setVariable('w', world)

			Location location = p.location
			context.setVariable('l', location)
			context.setVariable('pitch', location.pitch)

			def yaw = location.yaw % 360
			if (yaw < 0) yaw += 360
			context.setVariable('yaw', yaw)
			BlockFace facing = yaw <= 25 ? BlockFace.WEST : yaw < 65 ? BlockFace.NORTH_WEST : yaw <= 115 ? BlockFace.NORTH : yaw < 155 ? BlockFace.NORTH_EAST : yaw <= 205 ? BlockFace.EAST : yaw < 245 ? BlockFace.SOUTH_EAST : yaw <= 315 ? BlockFace.SOUTH : yaw < 335 ? BlockFace.SOUTH_WEST : BlockFace.WEST
			context.setVariable('facing', facing)

			Vector vector = new Vector(location.x, location.y-1.0, location.z)
			context.setVariable('v', vector)

			def x = vector.blockX
			def y = vector.blockY
			def z = vector.blockZ
			context.setVariable('x', x)
			context.setVariable('y', y)
			context.setVariable('z', z)

			context.setVariable('b', world.getBlockAt(x, y, z))
			context.setVariable('highy', world.getHighestBlockYAt(x, z))
			context.setVariable('yb', (0..128).collect {world.getBlockAt(x,it,z)})
			context.setVariable('bfwd', world.getBlockAt(x+facing.modX, y+facing.modY, z+facing.modZ))

		}
		context.setVariable('plugin', plugin)
		shell
	}


}