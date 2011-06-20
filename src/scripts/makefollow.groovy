def (follow, creature, qty) = args.toList()

make(follow, creature, qty).each { attract(it, follow) }
