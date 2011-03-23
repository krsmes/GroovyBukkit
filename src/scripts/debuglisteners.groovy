import org.bukkit.event.Event


def locstr = { String.format('Loc[xyz=%.2f:%.2f:%.2f]', it.x, it.y, it.z) }
def blkstr = { "Blk[xyz=$it.x:$it.y:$it.z $it.type:$it.data ($it.typeId)]" }
def entstr = { "Ent[$it(${locstr(it.location)})]" }

listen "debug", [

	(Event.Type.PLAYER_JOIN): { log "$it.eventName ($it.player.name): " },
	(Event.Type.PLAYER_LOGIN): { log "$it.eventName ($it.player.name): " },
	(Event.Type.PLAYER_RESPAWN): { log "$it.eventName ($it.player.name): ${locstr(it.respawnLocation)}" },
	(Event.Type.PLAYER_KICK): { log "$it.eventName ($it.player.name): reason=$it.reason, message=$it.leaveMessage" },
	(Event.Type.PLAYER_CHAT): { log "$it.eventName ($it.player.name): $it.message" },
	(Event.Type.PLAYER_COMMAND_PREPROCESS): { log "$it.eventName ($it.player.name): $it.message" },
	(Event.Type.PLAYER_QUIT): { log "$it.eventName ($it.player.name): " },
//	(Event.Type.PLAYER_MOVE): { log "$it.eventName ($it.player.name): ${locstr(it.from)} to ${locstr(it.to)}" },
	(Event.Type.PLAYER_ANIMATION): { log "$it.eventName ($it.player.name): $it.animationType" },
	(Event.Type.PLAYER_TOGGLE_SNEAK): { log "$it.eventName ($it.player.name)" },
	(Event.Type.PLAYER_ITEM): { log "$it.eventName ($it.player.name): ${blkstr(it.blockClicked)} clicked on $it.blockFace with $it.item" },
	(Event.Type.PLAYER_EGG_THROW): { log "$it.eventName ($it.player.name): $it.numHatches $it.hatchType $it.hatching " },
	(Event.Type.PLAYER_TELEPORT): { log "$it.eventName ($it.player.name): ${locstr(it.from)} to ${locstr(it.to)}" },
	(Event.Type.PLAYER_ITEM_HELD): { log "$it.eventName ($it.player.name): now $it.newSlot (${it.player.inventory.getItem(it.newSlot)}) was $it.previousSlot (${it.player.inventory.getItem(it.previousSlot)})" },
    (Event.Type.PLAYER_DROP_ITEM): { log "$it.eventName ($it.player.name): ${it.itemDrop.itemStack}" },
	(Event.Type.PLAYER_PICKUP_ITEM): { log "$it.eventName ($it.player.name): ${it.item.itemStack}" },


	(Event.Type.BLOCK_DAMAGED): { log "$it.eventName ${blkstr(it.block)}: by $it.player.name (damageLevel=$it.damageLevel)" },
	(Event.Type.BLOCK_CANBUILD): { log "$it.eventName ${blkstr(it.block)}: $it.material (buildable=$it.buildable)" },
	(Event.Type.BLOCK_FLOW): { log "$it.eventName ${blkstr(it.block)}: to ${blkstr(it.toBlock)} (face=$it.face)" },
	(Event.Type.BLOCK_IGNITE): { log "$it.eventName ${blkstr(it.block)}: by $it.player.name (cause=$it.cause)" },
//	(Event.Type.BLOCK_PHYSICS): { log "$it.eventName: ${blkstr(it.block)} to $it.changedType" },
	(Event.Type.BLOCK_RIGHTCLICKED): { log "$it.eventName ${blkstr(it.block)}: by $it.player.name with $it.itemInHand (direction=$it.direction)" },
	(Event.Type.BLOCK_PLACED): { log "$it.eventName ${blkstr(it.block)}: by $it.player.name against ${blkstr(it.blockAgainst)} (inHand=$it.itemInHand, canBuild=${it.canBuild()})" },
	(Event.Type.BLOCK_INTERACT): { log "$it.eventName ${blkstr(it.block)}: by ${entstr(it.entity)}" },
	(Event.Type.BLOCK_BURN): { log "$it.eventName ${blkstr(it.block)}" },
	(Event.Type.LEAVES_DECAY): { log "$it.eventName ${blkstr(it.block)}" },
	(Event.Type.SIGN_CHANGE): { log "$it.eventName ${blkstr(it.block)}: by $it.player.name (lines=${it.lines.join(',')})" },
	(Event.Type.LIQUID_DESTROY): { log "$it.eventName: ${blkstr(it.block)}" },
	(Event.Type.REDSTONE_CHANGE): { log "$it.eventName: ${blkstr(it.block)} $it.oldCurrent->$it.newCurrent" },
	(Event.Type.BLOCK_BREAK): { log "$it.eventName ${blkstr(it.block)}: by $it.player.name" },


	(Event.Type.INVENTORY_OPEN): { log "$it.eventName $it.player.name: " },
	(Event.Type.INVENTORY_CLOSE): { log "$it.eventName $it.player.name: " },
	(Event.Type.INVENTORY_CLICK): { log "$it.eventName $it.player.name: " },
	(Event.Type.INVENTORY_CHANGE): { log "$it.eventName $it.player.name: " },
	(Event.Type.INVENTORY_TRANSACTION): { log "$it.eventName $it.player.name: " },


	(Event.Type.PLUGIN_ENABLE): { log "$it.eventName: $it.plugin.description.name" },
	(Event.Type.PLUGIN_DISABLE): { log "$it.eventName: $it.plugin.description.name" },
    (Event.Type.SERVER_COMMAND): { log "$it.eventName: " },


//  (Event.Type.CHUNK_LOADED): { log "$it.eventName: $it.chunk" },
//	(Event.Type.CHUNK_UNLOADED): { log "$it.eventName: $it.chunk" },
	(Event.Type.CHUNK_GENERATION): { log "$it.eventName: " },
	(Event.Type.ITEM_SPAWN): { log "$it.eventName: " },
	(Event.Type.WORLD_SAVED): { log "$it.eventName: " },
	(Event.Type.WORLD_LOADED): { log "$it.eventName: " },


//	(Event.Type.CREATURE_SPAWN): { log "$it.eventName (${entstr(it.entity)}): " },
//	(Event.Type.ENTITY_DAMAGED): { log "$it.eventName (${entstr(it.entity)}): $it.cause ($it.damage)" },
	(Event.Type.ENTITY_DEATH): { log "$it.eventName (${entstr(it.entity)}): drops $it.drops" },
//	(Event.Type.ENTITY_COMBUST): { log "$it.eventName (${entstr(it.entity)}): fireTicks=$it.entity.fireTicks" },
	(Event.Type.ENTITY_EXPLODE): { log "$it.eventName (${entstr(it.entity)}): at ${locstr(it.location)}, yield $it.yield, blocks=${it.blockList().size()}" },
	(Event.Type.EXPLOSION_PRIMED): { log "$it.eventName (${entstr(it.entity)}): radius=$it.radius, fire=$it.fire" },
	(Event.Type.ENTITY_TARGET): { log "$it.eventName (${entstr(it.entity)}): $it.target (reason=$it.reason)" }

]