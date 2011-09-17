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
import org.bukkit.TreeType

class GroovyAPI {
	static Logger _log = Logger.getLogger("Minecraft")
    static gdebug = false
    static random = new Random()

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
			e = e.findAll {cls.isInstance(it) && !it.dead}
		}
        e
	}


    List e(def loc, double distance = 16.0, String name = null) {
        loc = l(loc)
        e(name).findAll { dist(loc,it) <= distance }
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
		def result = server.onlinePlayers.find {it.name.equalsIgnoreCase(playerName)}
        if (!result) result = server.onlinePlayers.find {it.name.toLowerCase().startsWith(playerName)}
        (Player) result
	}

    // this can spawn just about anything, give a creaturetype, entity class, material, itemstack, or string representing one of those
    def make(def loc, def thing, int qty = 1, Closure c = null) {
        loc = l(loc)
        if (thing instanceof CreatureType || thing instanceof Class) { /* no-op */ }
        else if (thing instanceof Number || thing instanceof Material || thing instanceof Block || thing instanceof Location)
            thing = i(thing, qty)
        else if (Entity.isInstance(thing))
            thing = thing.class
        else {
            thing = thing.toString().capitalize()
            def temp = CreatureType.fromName(thing)
            if (temp) thing = temp
            else
                try { thing = thing.toUpperCase() as CreatureType }
                catch (e1) {
                    try { thing = ('org.bukkit.entity.' + thing) as Class }
                    catch (e2) { thing = i(thing, qty) }
                }
        }
        // make it (will always return list of entities)
        if (thing instanceof ItemStack) {
            def ent = world.dropItem(loc, thing)
            if (c) c(ent)
            [ent]
        }
        else if (thing instanceof CreatureType) (1..qty).collect {
            def ent = world.spawnCreature(loc, thing)
            if (c) c(ent)
            ent
        }
        else if (thing instanceof Class) (1..qty).collect {
            def ent = world.spawn(loc, thing)
            if (c) c(ent)
            ent
        }
    }


    def drop(def loc, def item, int qty = 1) {
        loc = l(loc)
        item = i(item, qty)
        world.dropItem(loc, item)
    }


    def tree(def loc, def type = TreeType.TREE) {
        loc = l(loc)
        world.generateTree(loc, type as TreeType)
    }


    def explosion(def loc, float power = 4.0, boolean fire = false) {
        loc = l(loc)
        world.createExplosion(loc.x, loc.y, loc.z, power, fire)
    }


    def lightning(def loc) {
        loc = l(loc)
        world.strikeLightning(loc)
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


    static blockEach(def loc, int dist, Closure c) {
    }

    static blockEach(World world, def min, def max, Closure c) {
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


    static Area area(int x1, int x2, int z1, int z2) {
        new Area(x1, x2, z1, z2)
    }

    static Area area(loc1, loc2) {
        new Area(loc1, loc2)
    }

    static Plots plots() {
        Plots.instance
    }

    static reg(String name, Closure c) {
        BlockClosures.instance.registerClosure(name, c)
    }

    static reg(Block b, c) {
        BlockClosures.instance.registerBlock(b, c)
    }

    static unreg(Block b) {
        BlockClosures.instance.unregisterBlock(b)
    }


//
// helper methods
//

    static int rnd(int i) {
        random.nextInt(i)
    }

    static double rnd(double d) {
        random.nextDouble() * d
    }

    static boolean rndbool() {
        random.nextBoolean()
    }

	static def stringToType(s) {
		s.toString().toUpperCase().replaceAll(/[\s\.\-]/, '_')
	}

}
