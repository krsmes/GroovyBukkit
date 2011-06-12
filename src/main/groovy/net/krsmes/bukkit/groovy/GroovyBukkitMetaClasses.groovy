package net.krsmes.bukkit.groovy

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
import org.bukkit.craftbukkit.inventory.CraftInventory


class GroovyBukkitMetaClasses
{


	static enable() {
		def asTypeList = List.metaClass.&asType
		List.metaClass.asType = { Class c ->
			if (c == Vector.class) return new Vector(delegate[0], delegate[1], delegate[2])
			asTypeList(c)
		}

		CraftBlock.metaClass.asType = { Class c ->
			if (c == Vector.class) return delegate.vector
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
        CraftBlock.metaClass.eachAttached = { Closure closure -> Util.eachAttached(delegate, closure) }
        CraftBlock.metaClass.collectAttached = { Closure closure -> Util.collectAttached(delegate, closure) }
        CraftBlock.metaClass.findAttached = { Closure closure -> Util.findAttached(delegate, closure) }
        CraftBlock.metaClass.findAllAttached = { Closure closure -> Util.findAllAttached(delegate, closure) }
        CraftBlock.metaClass.toAABB = { dist -> delegate.vector.toAABB(dist) }
        CraftBlock.metaClass.eachBlock = { dist, closure -> delegate.world.eachBlock(*delegate.toAABB(dist), closure) }
        CraftBlock.metaClass.collectBlock = { dist, closure -> delegate.world.collectBlock(*delegate.toAABB(dist), closure) }
        CraftBlock.metaClass.findBlock = { dist, closure -> delegate.world.findBlock(*delegate.toAABB(dist), closure) }
        CraftBlock.metaClass.findAllBlock = { dist, closure -> delegate.world.findAllBlock(*delegate.toAABB(dist), closure) }
        CraftBlock.metaClass.toString = {->
            String.format('Blk[xyz=%d:%d:%d,type=%d,data=%d]',
                    delegate.x,delegate.y,delegate.z,delegate.typeId,delegate.data)
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
        Location.metaClass.toAABB = { dist -> delegate.toVector().toAABB(dist) }
        Location.metaClass.eachBlock = { dist, closure -> delegate.world.eachBlock(*delegate.toAABB(dist), closure) }
        Location.metaClass.collectBlock = { dist, closure -> delegate.world.collectBlock(*delegate.toAABB(dist), closure) }
        Location.metaClass.findBlock = { dist, closure -> delegate.world.findBlock(*delegate.toAABB(dist), closure) }
        Location.metaClass.findAllBlock = { dist, closure -> delegate.world.findAllBlock(*delegate.toAABB(dist), closure) }
        Location.metaClass.toString = {->
			String.format('Loc[world=%s,xyz=%.2f:%.2f:%.2f,pitch=%.1f,yaw=%.1f]',
                    delegate.world.name, delegate.x, delegate.y, delegate.z, delegate.pitch, delegate.yaw % 360)
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
        Player.metaClass.getRunner = { ->
            GroovyPlugin.instance?.getRunner((Player)delegate)
        }
        Player.metaClass.getData = { ->
            GroovyPlugin.instance?.getData((Player)delegate)
        }


        Vector.metaClass.plus = { amt ->
            if (amt instanceof Vector) return delegate.clone().add(amt)
            if (amt instanceof Integer) return delegate.clone().add(new Vector(amt,amt,amt))
            if (amt instanceof Double) return delegate.clone().add(new Vector(amt,amt,amt))
            if (amt instanceof List) return delegate.clone().add(amt as Vector)
        }
		Vector.metaClass.minus = { amt ->
            if (amt instanceof Vector) return delegate.clone().subtract(amt)
            if (amt instanceof Integer) return delegate.clone().subtract(new Vector(amt, amt, amt))
            if (amt instanceof Double) return delegate.clone().subtract(new Vector(amt, amt, amt))
            if (amt instanceof List) return delegate.clone().subtract(amt as Vector)
        }
        Vector.metaClass.toAABB = { dist ->
            def min = delegate - dist
            def max = delegate + dist
            [min, max]
        }
		Vector.metaClass.toString = {->
			String.format('Vec[xyz=%.2f:%.2f:%.2f]', delegate.x, delegate.y, delegate.z)
		}
        Vector.metaClass.static.fromString = { String str ->
            if (str.startsWith('Vec[')) {
                def pattern = ~/Vec\[xyz=([0-9\.]+):([0-9\.]+):([0-9\.]+)]/
                def m = str =~ pattern
                m ? m[0][1..3].collect{Double.parseDouble(it)} as Vector : null
            }
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
        World.metaClass.eachBlock = { Vector v1, Vector v2, Closure closure ->
            Util.eachBlock((World)delegate, v1, v2, closure)
        }
        World.metaClass.collectBlock = { Vector v1, Vector v2, Closure closure ->
            Util.collectBlock((World)delegate, v1, v2, closure)
        }
        World.metaClass.findBlock = { Vector v1, Vector v2, Closure closure ->
            Util.findBlock((World) delegate, v1, v2, closure)
        }
        World.metaClass.findAllBlock = { Vector v1, Vector v2, Closure closure ->
            Util.findAllBlock((World) delegate, v1, v2, closure)
        }

        ItemStack.metaClass.toString = {->
            String.format("Stk[type=%d,data=%d,qty=%d]", delegate.typeId, delegate.durability, delegate.amount)
        }
        ItemStack.metaClass.static.fromString = { String str ->
            def pattern = ~/Stk\[type=([0-9A-Fx]+),data=(\d+),qty=(\d+)]/
            def m = str =~ pattern
            if (m) {
                def _typeStr = m[0][1]
                int _type = _typeStr.startsWith('0x') ? Integer.parseInt(_typeStr[2..-1], 16) : Integer.parseInt(_typeStr)
                short _dur = Short.parseShort(m[0][2])
                int _amount = Integer.parseInt(m[0][3])
                new ItemStack(_type, _amount, _dur)
            }
            else null
        }

        Inventory.metaClass.getAt = { int idx -> delegate.getItem(idx) }
        Inventory.metaClass.putAt = { Integer key, ItemStack value -> delegate.setItem(key, value) }
		Inventory.metaClass.leftShift = { def is -> delegate.addItem(is) }
		Inventory.metaClass.rightShift = { def is -> delegate.removeItem(is) }
        Inventory.metaClass.asType = { Class c ->
			if (c == List.class) return delegate.contents.toList().findAll{it}
		}
        Inventory.metaClass.toString = {->
            'Inv[' + (delegate as List)*.toString().join(';') + ']'
        }
        Inventory.metaClass.fromString = { String str ->
            if (str.startsWith('Inv[')) str = str[4..-2]
            def items = str.split(';').collect{ItemStack.fromString(it)}
            while (items.size() < delegate.size) items << null
            delegate.contents = items.toArray(new ItemStack[items.size()])
        }

	}


	static disable() {

	}

}
