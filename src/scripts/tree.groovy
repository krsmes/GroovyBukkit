import org.bukkit.TreeType

def type = args ? args[0] : TreeType.TREE
if (!(type instanceof TreeType)) { type = TreeType."${stringToType(type)}" }

def b = at
b.type = m('dirt')
b += 1
b.typeId = 0

w.generateTree(l(b), type)
