register 'blah', 'player move', { e ->
	def xx = (int) e.to.x
	def zz = (int) e.to.z

	def minX = ((int) spawn.x) - 10
	def maxX = minX + 20

	def minZ = ((int) spawn.z) - 10
	def maxZ = minZ + 20

	if (xx < minX || xx > maxX || zz < minZ || zz > maxZ) {
		log "... ${e.player.name} is out of range"
		future {
		    log "Teleporting ${e.player.name} back to spawn ${xyz(spawn)}"
			e.player.teleportTo(spawn)
		}
	}

}
