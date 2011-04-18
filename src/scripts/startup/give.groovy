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

Notes:
    'player' does not need to be a full name... for player piMasterXYZMinecraftGod you can simplify to /give pi bow
    'material' can be a number or a name... if name is used it should match names in the Material enum

 */
command 'give', { runner, args ->
	// give player qty material
	// give qty material
	// give material
	if (args) {
		def rec = runner.player
		def qty = 1
		def mat
		def pattern = ''
		args.each { pattern += (it ==~ /\d+/) ? '#' : 'X' }
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
                runner.player.sendMessage "/give to qty material"
		}
		give rec, i(mat, qty)
	}
}

