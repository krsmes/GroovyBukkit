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
import org.bukkit.entity.CreatureType

class GroovyAPI {
	static Logger _log = Logger.getLogger("Minecraft")
    static gdebug = false

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


    def make(String name, def loc, int qty = 1) {
        loc = l(loc)
        CreatureType creatureType = CreatureType."${stringToType(name)}"
        def ents = (1..qty).collect { world.spawnCreature(loc, creatureType) }
        ents ? ents.size() > 1 ? ents : ents[0] : null
    }


// static


	static void log(message) {
		_log.info(message.toString())
	}

    static void debug(message) {
//        _log.fine(message.toString())
        if (gdebug) println "GroovyDEBUG: $message"
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


	static Vector looking(loc) {
		loc = l(loc)
        loc.direction
	}


	static Block lookingat(LivingEntity ent, maxDist = 128.0, precision = 0.01) {
        Util.lookingAtBlock(ent, maxDist, precision);
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


    static Area area(loc1, loc2) {
        new Area(loc1, loc2)
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
