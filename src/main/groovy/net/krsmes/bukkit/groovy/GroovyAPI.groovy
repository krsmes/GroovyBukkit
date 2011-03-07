package net.krsmes.bukkit.groovy

import java.util.logging.Logger

import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import org.bukkit.Material
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import org.bukkit.World
import org.bukkit.Server
import org.bukkit.entity.LivingEntity

class GroovyAPI {
	static Logger _log = Logger.getLogger("Minecraft")

	def scriptLoc = GroovyPlugin.SCRIPT_LOC

	World world
	Server server

	GroovyAPI() {
		throw new Exception("not implemented")
	}


	GroovyAPI(Server server) {
		this(server, server.worlds[0])
	}


	GroovyAPI(Server server, World world) {
		this.server = server
		this.world = world
	}


//
// Bukkit helpers
//

// world specific


	Location l(def x, def z) {
		new Location(world, x as double, world.getHighestBlockYAt((int) x, (int) z), z as double)
	}


	Location l(def x, def y, def z) {
		new Location(world, x as double, y as double, z as double)
	}


	Location l(def x, def y, def z, def yaw, def pitch) {
		new Location(world, x as double, y as double, z as double, yaw as float, pitch as float)
	}


	Location l(def unknown) {
		(unknown as Vector)?.toLocation(world)
	}


	List e(String name = null) {
		def e = world.entities
		if (name) {
			def cls = Class.forName("org.bukkit.entity.${name.capitalize()}")
			e.findAll {cls.isInstance(it)}
		}
		else {
			e
		}
	}


	void msg(def players, Object... args) {
		def to = []
		if (players instanceof String) to << p(players)
		if (players instanceof List) players.each {
			to << (it instanceof Player) ? it : p(it)
		}
		def message = args.toList().join(', ')
		if (to && message) {
			to.each { it.sendMessage message }
		}
	}


	Player p(def name) {
		def playerName = name.toString()
		server.onlinePlayers.find {it.name == playerName}
	}


// static


	static void log(message) {
		_log.info(message.toString())
	}


	static l(Location loc) { loc }
	static l(Entity ent) { ent.location }


	static ItemStack i(def item) {
		i(item, 1)
	}


	static ItemStack i(def item, int qty) {
		item = item instanceof ItemStack ? item : new ItemStack(m(item))
		if (qty > item.amount) item.amount = qty
		item
	}


	static ItemStack i(def item, byte data, int qty) {
		item = item instanceof ItemStack ? item : new ItemStack(m(item))
		if (data) item.data = item.type.getNewData(data)
		if (qty > item.amount) item.amount = qty
		item
	}


	static Material m(def m) {
		if (m instanceof Location || m instanceof Entity) {
			def loc = l(m)
			m = loc.world[loc]
		}
		m instanceof Material ? m :
			m instanceof Integer ? Material.getMaterial((int) m) :
				m instanceof ItemStack ? m.type :
					m instanceof Block ? m.type :
						Material.getMaterial(stringToType(m))
	}


	static Byte md(def m) {
		if (m instanceof Location || m instanceof Entity) {
			def loc = l(m)
			m = loc.world[loc]
		}
		def result = m instanceof Integer ? m :
			m instanceof Block ? m.data :
				m instanceof ItemStack ? m.data?.data : 0
		result ?: 0
	}


	static List xyz(def unknown) {
		def vec = (unknown as Vector)
		vec ? [vec.blockX, vec.blockY, vec.blockZ] : []
	}


	static Vector v(def x, def z) {
		new Vector(x, -1, z)
	}


	static Vector v(def x, def y, def z) {
		new Vector(x, y, z)
	}


	static Vector v(def unknown) {
		unknown as Vector
	}


	static BlockFace f(Entity e) {
		f(e.location)
	}


	static BlockFace f(Location l) {
		f(l.yaw)
	}


	static BlockFace f(Number yaw) {
		yaw %= 360
		if (yaw < 0) yaw += 360
		yaw <= 25 ? BlockFace.WEST : yaw < 65 ? BlockFace.NORTH_WEST : yaw <= 115 ? BlockFace.NORTH : yaw < 155 ? BlockFace.NORTH_EAST : yaw <= 205 ? BlockFace.EAST : yaw < 245 ? BlockFace.SOUTH_EAST : yaw <= 315 ? BlockFace.SOUTH : yaw < 335 ? BlockFace.SOUTH_WEST : BlockFace.WEST
	}


	static Vector looking(def loc) {
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


	static Block lookingat(loc, maxDist = 128.0, precision = 0.01) {
        def height = (loc instanceof LivingEntity) ? loc.eyeHeight : 1.62
		loc = l(loc)
		def head = v(loc.x, loc.y + height, loc.z)
		def look = looking(loc)
		def cntr = 0.0

		Block blk = null
		def type
		def pos
		while (cntr < maxDist) {
			cntr += precision
			pos = head + v(look.x * cntr, look.y * cntr, look.z * cntr)
			blk = loc.world[pos]
			type = blk.typeId
			// stop if non-air or y is 1 or 127
			if ((type > 0 && !(type in [50,55,65,66,68,78])) || pos.blockY == 127 || pos.blockY == 1) break
		}
		blk
	}


	static def dist(def from, def to) {
		from = v(from)
		to = v(to)
		from.distance(to)
	}


	static void give(Player p, def item, int qty = 1) {
		def inventory = p.inventory
		def stac = i(item, qty)
		if (stac.type == Material.WOOL || stac.type == Material.INK_SACK) {
			inventory.setItem(inventory.firstEmpty(), i(item, qty))
		}
		else {
			inventory.addItem(i(item, qty))
		}
	}


	static def make(String name, Location loc, int qty = 1) {
		Class ent_cls = Class.forName("net.minecraft.server.Entity${name.capitalize()}")
		def wH = loc.world.handle
		def ents = []
		for (int cntr = 0; cntr < qty; cntr++) {
			def ent = ent_cls.newInstance(wH)
			ent.c(loc.x + 0.5f, loc.y + 1.0f, loc.z + 0.5f, 0.0f, 0.0f)
			wH.a(ent)
			ent.bukkitEntity.teleportTo(loc)

			ents << ent.bukkitEntity
		}
		ents ? ents.size() > 1 ? ents : ents[0] : null
	}


//
// helper methods
//


	String load(name) {
		def file = null
		if (name instanceof File) {
			file = name
		}
		else {
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


	static def stringToType(s) {
		s.toString().toUpperCase().replaceAll(/[\s\.\-]/, '_')
	}

}
