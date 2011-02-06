def (creature, qty, follow) = args.toList()

(1..qty).each {
	def e = make(creature, 1, follow)
	attract(e, follow)
}
