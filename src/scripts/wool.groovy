import org.bukkit.inventory.ItemStack
import org.bukkit.Material
import org.bukkit.DyeColor

def (color, qty) = args.toList()
if (!qty) qty = 1
color = color instanceof Number ? (Byte) color : DyeColor."${stringToType(color)}".data

new ItemStack(Material.WOOL, qty, (byte) 0, color)
