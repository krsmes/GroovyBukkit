def h = w.handle
def e = h.b.bukkitEntity
if (args) {
	def cls = Class.forName("org.bukkit.entity.${args[0]}")
	e.findAll {cls.isInstance(it)}
}
else {
	e
}

