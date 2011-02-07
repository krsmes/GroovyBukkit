import org.bukkit.util.Vector

def v1 = args[0] as Vector
def v2 = args[1] as Vector
def material = m(args[2])

blocks(v1, v2) { block -> block.type = material }
