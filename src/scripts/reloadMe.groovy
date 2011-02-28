if (args) {
	// reload as someone else
	def name = args[0]
	me.health = 100
	me.teleportTo(l(here.x+512, 512, here.z+512))

	future {
		sleep 100
		me.displayName = name
		me.handle.name = name
		s.handle.l.b(me.handle)
		me.health = 100
	}

	future {
		me.teleportTo(here)
		me.health = 20
	}
}
else {
	s.handle.l.b(me.handle)
}
