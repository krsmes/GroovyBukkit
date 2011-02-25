/*
Command: give

Arguments:
    material
    player material
    qty material
    player qty material

Examples:

    give 1
        give self one stone

    give 5 stone
        give self 5 stone

    give krsmes 64 arrow
        give krsmes 64 arrows

 */
command 'give', { player, args ->
	// give player qty material
	// give qty material
	// give material
	if (args) {
		def rec = player
		def qty = 1
		def mat
		def pattern = ''
		args.each { pattern += (it ==~ /\d+/) ? '#' : 'X' }
		log pattern
		switch (pattern) {
			case '#':
				mat = m(args[0].toInteger())
				break
			case 'X':
				mat = m(args[0])
				break
			case 'X#':
				rec = p(args[0])
				mat = m(args[1].toInteger())
				break
			case 'XX':
				rec = p(args[0])
				mat = m(args[1])
				break
			case '#X':
				qty = args[0].toInteger()
				mat = m(args[1])
				break
			case '##':
				qty = args[0].toInteger()
				mat = m(args[1].toInteger())
				break
			case 'X##':
				rec = p(args[0])
				qty = args[1].toInteger()
				mat = m(args[2].toInteger())
				break
			case 'X#X':
				rec = p(args[0])
				qty = args[1].toInteger()
				mat = m(args[2])
				break
			default:
				player.sendMessage "/give to qty material"
		}
		log "give $rec, $mat, $qty"
		give rec, i(mat, qty)
	}
}

