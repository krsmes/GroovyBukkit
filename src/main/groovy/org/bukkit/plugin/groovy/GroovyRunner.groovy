package org.bukkit.plugin.groovy

import org.bukkit.Location
import org.bukkit.entity.Player

import java.util.logging.Logger
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import org.bukkit.inventory.ItemStack
import org.bukkit.event.Event
import org.bukkit.event.Event.Priority
import org.bukkit.Material
import org.bukkit.DyeColor
import org.bukkit.entity.Entity
import org.bukkit.block.Block
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.bukkit.World
import org.bukkit.TreeType


public class GroovyRunner implements EventExecutor
{
	static Logger log = Logger.getLogger("Minecraft")

	static SCRIPT_PREFIX = """
import org.bukkit.*;import org.bukkit.block.*;import org.bukkit.entity.*;import org.bukkit.inventory.*;import org.bukkit.material.*;import org.bukkit.event.*;import org.bukkit.util.*;
"""
	static SCRIPT_LOC = 'scripts/'
	static SCRIPT_SUFFIX = '.groovy'

	GroovyShell shell
	GroovyPlugin plugin
	Player player
	World world
	def data = [:]
	def futures = []
	def running = true


	GroovyRunner(GroovyPlugin plugin, Player player, def data) {
		this.plugin = plugin
		this.player = player
		this.world = player ? player.world : plugin.server.worlds[0]
		this.data = data
		shell = _initShell(data)
		_startFuturesThread()
	}

	def _startFuturesThread = { ->
		Thread.start {
			while (running) {
				while (futures) {
					try {
						futures.pop()()
						sleep 50
					}
					catch (e) {}
				}
				sleep 100
			}
		}
	}

	def runScript = { def script ->
		run SCRIPT_PREFIX + script, null
	}

	def runFile = { scriptName, args ->
		def script = load(scriptName + SCRIPT_SUFFIX)
		if (script) {
			run script, args
		}
	}

	def run = { script, args=[] ->
		def result = null
		if (script) {
			try {
				def vars = _runContext()
				def savedArgs = vars.args
				vars.args = args

				result = _parse(script, vars).run()

				vars.args = savedArgs
			}
			catch (e) {
				result = e.message
				e.printStackTrace()
			}
		}
		result
	}

	def _parse = { script, vars ->
		def gscript = shell.parse(script)

		gscript.metaClass.methodMissing = { mname, margs ->
			if (this.respondsTo(mname, margs)) {
				this.invokeMethod(mname, margs)
			}
			else {
				runFile(mname, margs)
			}
		}
		gscript.metaClass.propertyMissing = { pname ->
			if (data.containsKey(pname)) return data[pname]
			if (plugin.globalData.containsKey(pname)) return plugin.globalData[pname]
			plugin.server.onlinePlayers.find { it.name.startsWith(pname) }
		}
//		gscript.metaClass.propertyMissing = { pname, value ->
//			data[pname] = value
//		}
		gscript
	}


	def _initShell = { data ->
		def shell = new GroovyShell()
		def vars = shell.context.variables
		vars.log = log
		vars.g = this
		vars.s = plugin.server
		vars.global = plugin.globalData
		vars.w = world
		vars.spawn = world.spawnLocation
		vars.data = data
		shell
	}


	def _runContext = {
		def vars = shell.context.variables

		def pl = [:]
		plugin.server.onlinePlayers.each { pl[it.name] = it }
		vars.pl = pl

		if (player) {
			vars.me = player
			vars.inv = player.inventory
			vars.at = lookingat()

			Location location = player.location
			vars.loc = location
			vars.pitch = location.pitch
			def yaw = location.yaw % 360
			if (yaw < 0) yaw += 360
			vars.yaw = yaw

			Vector vector = v(location.x, location.y-1.0, location.z)
			vars.vec = vector
			vars.vHead = v(location.x, location.y+1.5, location.z)
			vars.vLook = looking(location)

			def x = vector.blockX
			def y = vector.blockY
			def z = vector.blockZ
			vars.x = x
			vars.y = y
			vars.z = z

			def fF = f(yaw)
			def fR = f(yaw + 90)
			def fL = f(yaw - 90)
			def fB = f(yaw + 180)
			vars.fac = fF
			vars.fRgt = fR
			vars.fLft = fL
			vars.fBck = fB

			def block = world[player]
			vars.blk = block
			vars.bFwd = block + fF
			vars.bRgt = block + fR
			vars.bLft = block + fL
			vars.bBck = block + fB
		}
		vars
	}


//
// helper methods
//


	def load = { name ->
		def file = null
		if (name instanceof File) {
			file = name
		}
		else {
			def scriptLoc = data.scripts
			if (!scriptLoc) {
				scriptLoc = SCRIPT_LOC
			}
			def fullName = scriptLoc + name
			try {
				URL u = name.toURL()
				return u.text
			}
			catch (e) {
				if (!name.startsWith('http:')) {
					file = new File(fullName)
				}
			}
		}
		if (file?.exists()) {
			return file.text
		}
	}



	def stringToType(s) {
		s.toString().toUpperCase().replaceAll(/[\s\.\-]/, '_')
	}


//
// 'api'
//


	ItemStack i(def item) {
		i(item, 1)
	}


	ItemStack i(def item, int qty) {
		item = item instanceof ItemStack ? item : new ItemStack(m(item))
		if (qty > item.amount) item.amount = qty
		item
	}

	Material m(def m) {
		if (m instanceof Location || m instanceof Vector || m instanceof Entity) m = world[m]  // get block
		m instanceof Material ? m :
			m instanceof Integer ? Material.getMaterial((int)m) :
			m instanceof ItemStack ? m.type :
			m instanceof Block ? m.type :
			Material.getMaterial(stringToType(m))
	}

	def md(def m) {
		if (m instanceof Location || m instanceof Vector || m instanceof Entity) m = world[m]  // get block
		def result = m instanceof Integer ? m :
			m instanceof Block ? m.data :
			m instanceof ItemStack ? m.data?.data : 0
		result ?: 0
	}


	void inv(int pos, def item, int qty = 1) {
	 	player.inventory.setItem(pos, i(item, qty))
	}


	void give(def item, int qty = 1) {
		give(player, item, qty)
	}

	void give(Player p, def item, int qty = 1) {
		def inventory = player.inventory
		def stac = i(item, qty)
		if (stac.type == Material.WOOL || stac.type == Material.INK_SACK) {
			inventory.setItem(inventory.firstEmpty(), i(item, qty))
		}
		else {
			inventory.addItem(i(item, qty))
		}
	}


	Location l(def x, def z) {
		new Location(world, x as double, world.getHighestBlockYAt((int) x, (int) z), z as double)
	}


	Location l(def x, def y, def z) {
		new Location(world, x as double, y as double, z as double)
	}


	Location l(def unknown) {
		if (unknown instanceof Location) return unknown
		if (unknown instanceof Entity) return unknown.location
		(unknown as Vector)?.toLocation(world)
	}


	List xyz(def unknown) {
		def vec = (unknown as Vector)
		vec ? [vec.blockX, vec.blockY, vec.blockZ] : []
	}


	Vector v(def x, def z) {
		new Vector(x,  -1, z)
	}


	Vector v(def x, def y, def z) {
		new Vector(x, y, z)
	}


	Vector v(def unknown) {
		unknown as Vector
	}


	BlockFace f(Entity e) {
		f(e.location)
	}


	BlockFace f(Location l) {
		f(l.yaw)
	}

	BlockFace f(Number yaw) {
		yaw %= 360
		if (yaw < 0) yaw += 360
		yaw <= 25 ? BlockFace.WEST : yaw < 65 ? BlockFace.NORTH_WEST : yaw <= 115 ? BlockFace.NORTH : yaw < 155 ? BlockFace.NORTH_EAST : yaw <= 205 ? BlockFace.EAST : yaw < 245 ? BlockFace.SOUTH_EAST : yaw <= 315 ? BlockFace.SOUTH : yaw < 335 ? BlockFace.SOUTH_WEST : BlockFace.WEST
	}

	Vector looking(loc=player?.location) {
		loc = l(loc)
		def yaw = loc.yaw % 360
		if (yaw < 0) yaw += 360
		def pitch = loc.pitch

		def yawR = Math.toRadians(-yaw)
		def pitchR = Math.toRadians(-pitch)
		def yawSin = Math.sin(yawR)
		def yawCos = Math.cos(yawR)
		def pitchSin = Math.sin(pitchR)
		def pitchCos = Math.cos(pitchR)

		new Vector(yawSin * pitchCos, pitchSin, yawCos * pitchCos)
	}

	Block lookingat(loc=player?.location, maxDist=128.0, precision=0.02) {
		loc = l(loc)
		def head = v(loc.x, loc.y + 1.6, loc.z)
		def look = looking(loc)
		def cntr = 0.0

		Block blk = null
		def type
		def pos
		while (cntr < maxDist) {
			cntr += precision
			pos = head + v(look.x * cntr, look.y * cntr, look.z * cntr)
			blk = world[pos]
			type = blk.typeId
			// stop if non-air, non-snow found or y is 1 or 127
			if ((type > 0 && type != 78 && type != 50) || pos.blockY == 127 || pos.blockY == 1) break
			// reduce precision the farther away (greatly decreases # of loops)
			if (precision < 0.9) precision += precision * 0.0333
		}
		blk
	}

	def dist(from, to) {
		from = l(from)
		to = l(to)
		(from as Vector).distance(to as Vector)
	}


	def msg(Object... args) {
		def players = []
		def msg = []
		args.each {
			if (it instanceof Player) { players << it } else { msg << it }
		}
		if (!players) players << player
		if (players) {
			players.each { it.sendMessage msg.join(', ') }
		}
		else {
			log.info('msg> '+ msg)
		}
	}


	List ent(name=null) {
		def e = world.entities
		if (name) {
			def cls = Class.forName("org.bukkit.entity.${name.capitalize()}")
			e.findAll {cls.isInstance(it)}
		}
		else {
			e
		}
	}


	Entity make(name, int qty = 1, loc=player?.location) {
		loc = l(loc) ?: world.spawnLocation

		Class ent_cls = Class.forName("net.minecraft.server.Entity${name.capitalize()}")
		def wH = world.handle
		def ents = []
		for (int cntr = 0; cntr<qty; cntr++) {
			def ent = ent_cls.newInstance(wH)
			ent.c(loc.x + 0.5f, loc.y + 1.0f, loc.z + 0.5f, 0.0f, 0.0f)
			wH.a(ent)
			ent.bukkitEntity.teleportTo(loc)

			ents << ent.bukkitEntity
		}
		data.lastmake = ents ? ents.size() == 1 ? ents[0] : ents : null
	}


	void to(def loc) {
		def lastloc = player.location
		def dest = l(loc) ?: lastloc
		if (dest.pitch == 0.0 && dest.yaw == 0.0) { dest.pitch = lastloc.pitch; dest.yaw = lastloc.yaw }
		player.teleportTo(dest)
		data.lastloc = lastloc
	}

	void back() {
		to(data.lastloc)
	}





	def registeredListeners = [:]


	void register(String uniqueName, def type, Closure c) {
		register(uniqueName, [(type):c])
	}


	def register(String uniqueName, Map typeClosureMap, Event.Priority priority = Priority.Normal) {
		unregister(uniqueName)

		def listener = [toString: {uniqueName}] as Listener
		def listeners = [:]
		typeClosureMap.each { def type, closure ->
			if (!(type instanceof Event.Type)) type = Event.Type."${stringToType(type)}"
			if (closure instanceof Closure) {
				listeners[type] = closure
				plugin.server.pluginManager.registerEvent(type, listener, this, priority, plugin)
			}
		}
		if (listeners.size() > 0) {
			registeredListeners[uniqueName] = listeners
			log.info("Registered '$uniqueName' with ${listeners.size()} listener(s): ${listeners.keySet()}")
		}
	}


	void unregister(String uniqueName) {
		if (registeredListeners.containsKey(uniqueName)) {
			def listeners = registeredListeners.remove(uniqueName)
			listeners?.clear()
			log.info("Unregistered '$uniqueName'")
		}
	}


	void execute(Listener listener, Event e) {
		def name = listener.toString()
		if (registeredListeners.containsKey(name)) {
			def listeners = registeredListeners[name]
			if (listeners.containsKey(e.type)) {
				listeners[e.type](e)
			}
		}
	}

	void future(Closure c) {
		futures.add(0, c)
	}

}