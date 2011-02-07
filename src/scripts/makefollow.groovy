def (creature, follow, qty) = args.toList()

(1..qty).each {
	def e = make(creature, follow, 1)
	attract(e, follow)
}
