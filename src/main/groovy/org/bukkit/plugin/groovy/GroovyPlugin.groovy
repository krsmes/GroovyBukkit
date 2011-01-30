package org.bukkit.plugin.groovy

import java.util.logging.Logger

import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.entity.Player
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.block.BlockFace
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.util.Vector
import org.bukkit.World
import org.bukkit.Material
import org.bukkit.inventory.ItemStack


class GroovyPlugin extends JavaPlugin
{
	static Logger log = Logger.getLogger("Minecraft")

	def _playerData = [:]
	def globalData = [:]


	GroovyPlugin(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader)
	}


	void onEnable() {
		log.info("${description.name} ${description.version} enabled")

		def asTypeList = List.metaClass.&asType
		List.metaClass.asType = { Class c ->
			if (c == Vector.class) return new Vector(delegate[0], delegate[1], delegate[2])
			asTypeList(c)
		}

		Block.metaClass.asType = { Class c ->
			if (c == Vector.class) return new Vector(delegate.x, delegate.y, delegate.z)
			if (c == Location.class) return delegate.location
		}
		Block.metaClass.plus = { rel ->
			if (rel instanceof BlockFace) return delegate.getFace(rel)
			if (rel instanceof Integer) return delegate.getRelative(0, (int)rel, 0)
			if (rel instanceof Vector) return delegate.getRelative(rel.blockX, rel.blockY, rel.blockZ)
			throw java.lang.IllegalArgumentException("Block.plus does not recognize ${rel.class}")
		}
		Block.metaClass.minus = { rel ->
			if (rel instanceof BlockFace) return delegate.getFace(rel, -1)
			if (rel instanceof Integer) return delegate.getRelative(0, (int)-rel, 0)
			if (rel instanceof Vector) return delegate.getRelative(-rel.blockX, -rel.blockY, -rel.blockZ)
			(delegate as Vector) - (rel as Vector)
		}

		BlockFace.metaClass.plus = { int amt ->
			amt++
			new Vector(delegate.modX*amt, delegate.modY*amt, delegate.modZ*amt)
		}

		BlockFace.metaClass.asType = { Class c ->
			if (c == Vector.class) return new Vector(delegate.modX, delegate.modY, delegate.modZ)
		}

		Location.metaClass.asType = { Class c ->
			if (c == Vector.class) return new Vector(delegate.x, delegate.y, delegate.z)
		}

		Entity.metaClass.asType = { Class c ->
			if (c == Vector.class) return new Vector(delegate.location.x, delegate.location.y, delegate.location.z)
		}

		Vector.metaClass.plus = { amt -> delegate.add(amt) }
		Vector.metaClass.minus = { amt -> delegate.subtract(amt) }

		World.metaClass.getAt = { pos ->
			def v = pos as Vector
			if (pos instanceof Entity) v.y = v.blockY-1 // block under entity
			delegate.getBlockAt(v.blockX, v.blockY, v.blockZ)
		}
		World.metaClass.putAt = { pos, b ->
			def v = pos as Vector
			if (pos instanceof Entity) v.y = v.blockY - 1 // block under entity
			Block block = delegate.getBlockAt(v.blockX, v.blockY, v.blockZ)
			if (b instanceof Vector || b instanceof Location || b instanceof Entity) b = delegate[b]
			if (b instanceof Material) block.type = b
			else if (b instanceof Integer) block.typeId = b
			else if (b instanceof ItemStack) {
				block.type = b.type
				block.data = b.data?.data
			}
			else if (b instanceof Block) {
				block.type = b.type
				block.data = b.data
			}
		}
	}


	void onDisable() {
		log.info("${description.name} ${description.version} disabled")
	}


	boolean onCommand(Player player, Command cmd, String commandLabel, String[] args) {
		try {
			def result = "command_${cmd.name}"(player, getPlayerData(player), args?.toList())
			if (result) player.sendMessage result.toString()
			return true
		}
		catch (e) {
			player.sendMessage e.message
			log.severe(e.message)
			e.printStackTrace()
		}
		return false
	}


//
// commands
//

	def command_g = { Player player, def data, def args ->
		def runner = data.runner
		if (!runner) {
			runner = new GroovyRunner(this, player, data)
			data.runner = runner
		}
		else {
			runner.player = player
		}
		runner.runScript(args.join(' '))
	}


//
// data
//

	def getPlayerData = { Player player ->
		def name = player.name
		def result = _playerData[name]
		if (!result) {
			result = [:]
			_playerData[name] = result
		}
		result
	}


}
