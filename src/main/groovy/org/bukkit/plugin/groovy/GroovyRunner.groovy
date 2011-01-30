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
import org.bukkit.entity.Entity
import org.bukkit.block.Block


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
				e.printStackTrace()
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
		def world = player.world
		vars.w = world
		vars.spawn = world.spawnLocation
		shell
	}


	def _runContext = {
		def vars = shell.context.variables
		Location location = player.location
		def world = player.world

		def op = [:]
		plugin.server.onlinePlayers.each { op[it.name] = it }
		vars.op = op

		vars.p = player
		vars.l = location
		vars.pitch = location.pitch

		def yaw = location.yaw % 360
		if (yaw < 0) yaw += 360
		vars.yaw = yaw
		def f = facing(yaw)
		def fR = facing(yaw+90)
		def fL = facing(yaw-90)
		def fB = facing(yaw+180)
		vars.f = f
		vars.fR = fR
		vars.fL = fL
		vars.fB = fB

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
		vars.bF = block + f
		vars.bR = block + fR
		vars.bL = block + fL
		vars.bB = block + fB
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
		if (m instanceof Location || m instanceof Vector || m instanceof Entity) m = player.world[m]  // get block
		m instanceof Material ? m :
			m instanceof Integer ? Material.getMaterial((int)m) :
			m instanceof ItemStack ? m.type :
			m instanceof Block ? m.type :
			Material.getMaterial(m.toString().toUpperCase().replaceAll(/[\s\.\-]/, '_'))
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


	def to(def l) {
		player.teleportTo(loc(l))
	}


	def loc(def x, def y, def z) {
		new Location(player.world, x as double, y as double, z as double)
	}


	def loc(def x, def z) {
		def w = player.world
		new Location(w, x as double, w.getHighestBlockYAt((int) x, (int) z), z as double)
	}

	def loc(def unknown) {
		if (unknown instanceof Location) return unknown
		(unknown as Vector)?.toLocation(player.world)
	}


	def vec(def x, def y, def z) {
		new Vector(x, y, z)
	}

	def vec(List args) {
		new Vector(args[0], args[1], args[2])
	}

	def vec(Location l) {
		new Vector(l.blockX, l.blockY, l.blockZ)
	}

	def vec(Entity e) {
		vec(e.location)
	}

	def vec(int val) {
		new Vector(val, val, val)
	}

	def facing(Entity e) {
		facing(e.location)
	}

	def facing(Location l) {
		facing(l.yaw)
	}

	def facing(Number yaw) {
		yaw = yaw % 360
		if (yaw < 0) yaw += 360
		yaw <= 25 ? BlockFace.WEST : yaw < 65 ? BlockFace.NORTH_WEST : yaw <= 115 ? BlockFace.NORTH : yaw < 155 ? BlockFace.NORTH_EAST : yaw <= 205 ? BlockFace.EAST : yaw < 245 ? BlockFace.SOUTH_EAST : yaw <= 315 ? BlockFace.SOUTH : yaw < 335 ? BlockFace.SOUTH_WEST : BlockFace.WEST
	}


	def register(String methodName, Closure c) {
		register(methodName, [(methodName): c])
	}

	static EVENT_TYPE_METHOD_NAME = [
		(Event.Type.BLOCK_CANBUILD): 'onBlockCanBuild',
		(Event.Type.BLOCK_DAMAGED): 'onBlockDamage',
		(Event.Type.BLOCK_RIGHTCLICKED): 'onBlockRightClick',
		(Event.Type.BLOCK_PLACED): 'onBlockPlace',
		(Event.Type.REDSTONE_CHANGE): 'onBlockRedstoneChange',
		(Event.Type.ENTITY_DAMAGEDBY_BLOCK): 'onEntityDamageByBlock',
		(Event.Type.ENTITY_DAMAGEDBY_ENTITY): 'onEntityDamageByEntity',
		(Event.Type.ENTITY_DAMAGEDBY_PROJECTILE): 'onEntityDamageByProjectile',
		(Event.Type.PLUGIN_ENABLE): 'onPluginEnabled',
		(Event.Type.PLUGIN_DISABLE): 'onPluginDisable',
		(Event.Type.VEHICLE_COLLISION_BLOCK): 'onVehicleBlockCollision',
		(Event.Type.VEHICLE_COLLISION_ENTITY): 'onVehicleEntityCollision'
	]

	def register(String uniqueName, Map listener, Event.Priority priority = Priority.Normal) {
		def registered = plugin.globalData[uniqueName]
		if (registered) {
			registered.each { plugin.server.pluginManager.unregisterEvent(it) }
		}
		registered = []

		Event.Type.values().each { type ->
			def methodName = EVENT_TYPE_METHOD_NAME[type]
			if (!methodName) {
				methodName = type.toString()
				methodName = 'on' + methodName.split('_').collect {it.toLowerCase().capitalize()}.join('')
			}
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
					registered << plugin.server.pluginManager.registerEvent(type, typedListener, priority, plugin)
				}
			}
		}

		plugin.globalData[uniqueName] = registered
	}


	def unregister(String uniqueName) {
		def registered = plugin.globalData[uniqueName]
		if (registered) {
			registered.each { plugin.server.pluginManager.unregisterEvent(it) }
		}
		plugin.globalData.remove(uniqueName)
	}


	// temporary until Bukkit implements
	def entities(name=null) {
		def h = player.world.handle
		def e = h.b.bukkitEntity
		if (name) {
			def cls = Class.forName("org.bukkit.entity.$name")
			e.findAll {cls.isInstance(it)}
		}
		else {
			e
		}
	}

	def create(name, l=player.location) {
		def h = player.world.handle
		l = loc(l)

		def e_cls = Class.forName('net.minecraft.server.EntityList')
		def w_cls = Class.forName('net.minecraft.server.World')
		def ent = e_cls.getDeclaredMethod('a', String.class, w_cls).invoke(null, name, h)
		ent.c(l.x + 0.5f, l.y + 1.0f, l.z + 0.5f, 0.0f, 0.0f)
		h.a(ent)

		ent.bukkitEntity.teleportTo(l)
		ent.bukkitEntity
	}

}