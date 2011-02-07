package org.bukkit.plugin.groovy

import org.bukkit.craftbukkit.block.CraftBlock
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.util.Vector
import org.bukkit.inventory.ItemStack


class GroovyBukkitMetaClasses
{


	static enable() {
		def asTypeList = List.metaClass.&asType
		List.metaClass.asType = { Class c ->
			if (c == Vector.class) return new Vector(delegate[0], delegate[1], delegate[2])
			asTypeList(c)
		}

		CraftBlock.metaClass.asType = { Class c ->
			if (c == Vector.class) return new Vector(delegate.x, delegate.y, delegate.z)
			if (c == Location.class) return delegate.location
		}
		CraftBlock.metaClass.plus = { rel ->
			if (rel instanceof BlockFace) return delegate.getFace(rel)
			if (rel instanceof Integer) return delegate.getRelative(0, (int) rel, 0)
			if (rel instanceof Vector) return delegate.getRelative(rel.blockX, rel.blockY, rel.blockZ)
			throw java.lang.IllegalArgumentException("Block.plus does not recognize ${rel.class}")
		}
		CraftBlock.metaClass.minus = { rel ->
			if (rel instanceof BlockFace) return delegate.getFace(rel, -1)
			if (rel instanceof Integer) return delegate.getRelative(0, (int) -rel, 0)
			if (rel instanceof Vector) return delegate.getRelative(-rel.blockX, -rel.blockY, -rel.blockZ)
			(delegate as Vector) - (rel as Vector)
		}
		CraftBlock.metaClass.toString = {->
			"Blk[xyz=${delegate.x}:${delegate.y}:${delegate.z} type=${delegate.type}(${delegate.typeId}) data=${delegate.data}]"
		}

		BlockFace.metaClass.plus = { int amt ->
			amt++
			new Vector(delegate.modX * amt, delegate.modY * amt, delegate.modZ * amt)
		}
		BlockFace.metaClass.multiply = { int amt ->
			new Vector(delegate.modX * amt, delegate.modY * amt, delegate.modZ * amt)
		}

		BlockFace.metaClass.asType = { Class c ->
			if (c == Vector.class) return new Vector(delegate.modX, delegate.modY, delegate.modZ)
		}

		Location.metaClass.asType = { Class c ->
			if (c == Vector.class) return delegate.toVector()
		}
		Location.metaClass.plus = { ofs ->
			if (ofs instanceof Integer) ofs = new Vector(0, ofs, 0)
			((delegate as Vector) + (ofs as Vector)).toLocation(delegate.world, delegate.yaw, delegate.pitch)
		}
		Location.metaClass.block = {-> delegate.world[delegate] }
		Location.metaClass.toString = {->
			String.format('Loc[xyz=%.2f:%.2f:%.2f, pitch=%.1f, yaw=%.1f]', delegate.x, delegate.y, delegate.z, delegate.pitch, delegate.yaw % 360)
		}


		Entity.metaClass.asType = { Class c ->
			if (c == Vector.class) return new Vector(delegate.location.x, delegate.location.y, delegate.location.z)
		}
		LivingEntity.metaClass.asType = { Class c ->
			if (c == Vector.class) return new Vector(delegate.location.x, delegate.location.y, delegate.location.z)
		}
		HumanEntity.metaClass.asType = { Class c ->
			if (c == Vector.class) return new Vector(delegate.location.x, delegate.location.y, delegate.location.z)
		}
		Player.metaClass.asType = { Class c ->
			if (c == Vector.class) return new Vector(delegate.location.x, delegate.location.y, delegate.location.z)
		}


		Vector.metaClass.plus = { amt -> delegate.add(amt) }
		Vector.metaClass.minus = { amt -> delegate.subtract(amt) }
		Vector.metaClass.toString = {->
			String.format('Vec[xyz=%.1f:%.1f:%.1f]', delegate.x, delegate.y, delegate.z)
		}


		World.metaClass.getAt = { pos ->
			def v = pos as Vector
			if (pos instanceof Entity) v.y = v.blockY - 1 // block under entity
			(v.y < 0.0) ? delegate.getHighestBlockYAt(v.blockX, v.blockZ) : delegate.getBlockAt(v.blockX, v.blockY, v.blockZ)
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


		Inventory.metaClass.getAt = { int idx -> delegate.getItem(idx) }
		Inventory.metaClass.putAt = { int idx, ItemStack is -> delegate.setItem(idx, is) }
		Inventory.metaClass.leftShift = { def is -> delegate.addItem(is) }
		Inventory.metaClass.rightShift = { def is -> delegate.removeItem(is) }
		Inventory.metaClass.asType = { Class c ->
			if (c == List.class) return delegate.contents.toList()
		}

	}


	static disable() {

	}

}
