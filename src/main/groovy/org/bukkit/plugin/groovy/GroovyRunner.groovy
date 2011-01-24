package org.bukkit.plugin.groovy

import org.bukkit.Location
import org.bukkit.entity.Player

import java.util.logging.Logger
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import org.bukkit.inventory.ItemStack
import org.bukkit.event.Event
import org.bukkit.event.Event.Priority
import org.bukkit.event.player.PlayerListener
import org.bukkit.event.block.BlockListener
import org.bukkit.event.entity.EntityListener
import org.bukkit.event.vehicle.VehicleListener
import org.bukkit.event.world.WorldListener
import org.bukkit.event.server.ServerListener
import org.bukkit.Material
import org.bukkit.DyeColor



public class GroovyRunner
{
	static Logger log = Logger.getLogger("Minecraft")

	static SCRIPT_PREFIX = """
import org.bukkit.*;import org.bukkit.block.*;import org.bukkit.entity.*;import org.bukkit.inventory.*;import org.bukkit.material.*;import org.bukkit.util.*;
"""
	static SCRIPT_LOC = 'scripts/'
	static SCRIPT_SUFFIX = '.groovy'

	GroovyShell shell
	GroovyPlugin plugin
	Player player
	def data = [:]


	GroovyRunner(GroovyPlugin plugin, Player player, def data) {
		this.plugin = plugin
		this.player = player
		this.data = data
		shell = _initShell(data)
	}

	def runScript = { def script ->
		run SCRIPT_PREFIX + script, null
	}

	def runFile = { scriptName, args ->
		def scriptLoc = data.scripts
		if (!scriptLoc) {
			scriptLoc = SCRIPT_LOC
		}
		def name = scriptLoc + scriptName + SCRIPT_SUFFIX
		try {
			URL u = name.toURL()
			def script = u.text
			if (script) {
				run script, args
			}
		}
		catch (e) {
			if (!name.startsWith('http:')) {
				def file = new File(name)
				if (file.exists()) {
					run file, args
				}
			}
		}
	}


	def run = { script, args ->
		def result = null
		if (script) {
			try {
				def vars = _runContext()
				def savedArgs = vars.args
				vars.args = args

				def gscript = shell.parse(script)
				gscript.metaClass.methodMissing = { mname, margs ->
					if (this.respondsTo(mname, margs)) {
						this.invokeMethod(mname, margs)
					}
					else {
						runFile(mname, margs)
					}
				}
				result = gscript.run()

				vars.args = savedArgs
			}
			catch (e) {
				result = e.message
			}
		}
		result
	}


	def _initShell = { data ->
		def shell = new GroovyShell()
		def vars = shell.context.variables
		vars.log = log
		vars.g = this
		vars.s = plugin.server
		vars.global = plugin.globalData
		vars.data = data
		vars.p = player
		def world = player.world
		vars.w = world
		vars.spawn = world.spawnLocation
		shell
	}


	def _runContext = {
		def vars = shell.context.variables

		def op = [:]
		plugin.server.onlinePlayers.each { op[it.name] = it }
		vars.op = op

		def world = player.world
		Location location = player.location
		vars.l = location
		vars.pitch = location.pitch

		def yaw = location.yaw % 360
		if (yaw < 0) yaw += 360
		vars.yaw = yaw
		def facing = facing(yaw)
		vars.f = facing

		Vector vector = new Vector(location.x, location.y-1.0, location.z)
		vars.v = vector

		def x = vector.blockX
		def y = vector.blockY
		def z = vector.blockZ
		vars.x = x
		vars.y = y
		vars.z = z

		def block = world.getBlockAt(x, y, z)
		vars.b = block
		vars.highY = world.getHighestBlockYAt(x, z)
		vars.bFwd = block.getRelative(facing)
		vars.bY = (0..128).collect {world.getBlockAt(x,it,z)}
		vars
	}


//
// helper methods
//

	def wool(def color, int qty = 1) {
		new ItemStack(Material.WOOL, qty, (byte)0, color instanceof Number ? (byte)color : DyeColor."${color.toString().toUpperCase().replaceAll(/[\s\.\-]/, '_')}".data)
	}


	def stack(def item) {
		stack(item, 1)
	}


	def stack(def item, int qty) {
		item = item instanceof ItemStack ? item : new ItemStack(m(item))
		if (qty > item.amount) item.amount = qty
		item
	}

	def m(def m) {
		m instanceof Material ? m : m instanceof Integer ? Material.getMaterial((int)m) : Material.getMaterial(m.toString().toUpperCase().replaceAll(/[\s\.\-]/, '_'))
	}


	def inv(int pos, def item, int qty = 1) {
	 	player.inventory.setItem(pos, stack(item, qty))
	}


	def give(def item, int qty = 1) {
		give(player, item, qty)
	}

	def give(Player p, def item, int qty = 1) {
		def inventory = player.inventory
		def stac = stack(item, qty)
		if (stac.type == Material.WOOL || stac.type == Material.INK_SACK) {
			inventory.setItem(inventory.firstEmpty(), stack(item, qty))
		}
		else {
			inventory.addItem(stack(item, qty))
		}
	}


	def to(def loc) {
		player.teleportTo(loc)
	}


	def loc(def x, def y, def z) {
		new Location(player.world, x as double, y as double, z as double)
	}


	def loc(def x, def z) {
		def w = player.world
		new Location(w, x as double, w.getHighestBlockYAt((int) x, (int) z), z as double)
	}


	def facing(def yaw) {
		yaw = yaw % 360
		if (yaw < 0) yaw += 360
		yaw <= 25 ? BlockFace.WEST : yaw < 65 ? BlockFace.NORTH_WEST : yaw <= 115 ? BlockFace.NORTH : yaw < 155 ? BlockFace.NORTH_EAST : yaw <= 205 ? BlockFace.EAST : yaw < 245 ? BlockFace.SOUTH_EAST : yaw <= 315 ? BlockFace.SOUTH : yaw < 335 ? BlockFace.SOUTH_WEST : BlockFace.WEST
	}


	def register(String methodName, Closure c) {
		register(methodName, [(methodName): c])
	}


	def register(String uniqueName, Map listener, Event.Priority priority = Priority.Normal) {
		def registered = plugin.globalData[uniqueName]
		if (registered) {
			registered.each { plugin.server.pluginManager.unregisterEvent(it) }
		}
		registered = []

		Event.Type.values().each { type ->
			def methodName = type.toString()
			methodName = 'on' + methodName.split('_').collect {it.toLowerCase().capitalize()}.join('')
			if (listener."$methodName") {
				def typedListener
				switch (type.category) {
					case Event.Category.PLAYER:
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
					registered << plugin.server.pluginManager.registerEvent(type, typedListener, priority, this)
				}
			}
		}

		plugin.globalData[uniqueName] = registered
	}


	def unregister(String uniqueName) {
		def registered = plugin.globalData[uniqueName]
		if (registered) {
			registered.each { server.pluginManager.unregisterEvent(it) }
		}
		plugin.globalData.remove(uniqueName)
	}


}