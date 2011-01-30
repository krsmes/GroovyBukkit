def player = p
if (args[0] instanceof org.bukkit.entity.Player) { player=args[0]; args.remove(0) }
player.sendMessage args.join(';')