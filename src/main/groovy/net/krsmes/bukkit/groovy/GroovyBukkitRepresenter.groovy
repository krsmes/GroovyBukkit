package net.krsmes.bukkit.groovy

import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.representer.Represent
import org.yaml.snakeyaml.representer.Representer

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.craftbukkit.entity.CraftPlayer


class GroovyBukkitRepresenter extends Representer
{

	GroovyBukkitRepresenter() {
		def rep = [
			representData: { data ->
				String value = null
				if (data instanceof Location) {
					value = "l($data.x, $data.y, $data.z, $data.yaw, $data.pitch)"
				}
				else if (data instanceof Vector) {
					value = "v($data.x, $data.y, $data.z)"
				}
				else if (data instanceof Material) {
					value = "m(\"${data.name()}\")"
				}
				else if (data instanceof ItemStack) {
					value = "i(\"${data.type.name()}\", ${data.data?.data?:0}, ${data.amount})"
				}
				else if (data instanceof Player) {
					value = "p(\"$data.name\")"
				}
                else if (data instanceof Area) {
                    value = "area($data.minX, $data.maxX, $data.minZ, $data.maxZ)"
                }
                representScalar(new Tag('!g'), value)
			}
		] as Represent

		representers[Location.class] = rep
		representers[Vector.class] = rep
		representers[Material.class] = rep
		representers[ItemStack.class] = rep
		representers[CraftPlayer.class] = rep
	}

}
