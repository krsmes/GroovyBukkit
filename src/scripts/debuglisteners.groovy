import org.bukkit.event.Event
import org.bukkit.event.player.*
import org.bukkit.event.block.*
import org.bukkit.event.server.*
import org.bukkit.event.world.*
import org.bukkit.event.entity.*
import org.bukkit.event.vehicle.*
import org.bukkit.event.painting.*
import org.bukkit.event.weather.*


def locstr = { it?String.format('Loc[xyz=%.2f:%.2f:%.2f]', it.x, it.y, it.z):'Loc[null] ' }
def vecstr = { it?String.format('Vec[xyz=%.2f:%.2f:%.2f]', it.x, it.y, it.z):'Vec[null] ' }
def blkstr = { it?"Blk[xyz=$it.x:$it.y:$it.z $it.type:$it.data($it.typeId)]":'Blk[null]' }
def entstr = { it?"Ent[$it(${locstr(it.location)})]":'Ent[null]' }
def vehstr = { it?"Veh[$it(${locstr(it.location)})]":'Veh[null]' }
def itmstr = { it?"Itm[${it.amount}x${it.type}:${it.typeId}]":'Itm[null]' }

listen "debug", [

	(Event.Type.PLAYER_JOIN):           { PlayerJoinEvent it        -> log "$it.eventName ($it.player.name): joinMessage=$it.joinMessage" },
	(Event.Type.PLAYER_LOGIN):          { PlayerLoginEvent it       -> log "$it.eventName ($it.player.name): result=$it.result, kickMessage=$it.kickMessage" },
	(Event.Type.PLAYER_PRELOGIN):       { PlayerPreLoginEvent it    -> log "$it.eventName ($it.name): result=$it.result, kickMessage=$it.kickMessage" },
	(Event.Type.PLAYER_RESPAWN):        { PlayerRespawnEvent it     -> log "$it.eventName ($it.player.name): ${locstr(it.respawnLocation)}" },
	(Event.Type.PLAYER_KICK):           { PlayerKickEvent it        -> log "$it.eventName ($it.player.name): reason=$it.reason, leaveMessage=$it.leaveMessage" },
	(Event.Type.PLAYER_CHAT):           { PlayerChatEvent it        -> log "$it.eventName ($it.player.name): $it.message" },
	(Event.Type.PLAYER_COMMAND_PREPROCESS): { PlayerCommandPreprocessEvent it -> log "$it.eventName ($it.player.name): $it.message" },
	(Event.Type.PLAYER_QUIT):           { PlayerQuitEvent it        -> log "$it.eventName ($it.player.name): quitMessage=$it.quitMessage" },
	//PLAYER_MOVE (noisy)
    (Event.Type.PLAYER_ANIMATION):      { PlayerAnimationEvent it   -> log "$it.eventName ($it.player.name): $it.animationType" },
	(Event.Type.PLAYER_TOGGLE_SNEAK):   { PlayerToggleSneakEvent it -> log "$it.eventName ($it.player.name)" },
	(Event.Type.PLAYER_INTERACT):       { PlayerInteractEvent it    -> log "$it.eventName ($it.player.name): item=$it.item, action=$it.action, clickedBlock=${blkstr(it.clickedBlock)}, blockFace=$it.blockFace" },
	(Event.Type.PLAYER_INTERACT_ENTITY): { PlayerInteractEntityEvent it -> log "$it.eventName ($it.player.name): rightClicked=${entstr(it.rightClicked)}" },
	(Event.Type.PLAYER_EGG_THROW):      { PlayerEggThrowEvent it    -> log "$it.eventName ($it.player.name): $it.numHatches $it.hatchType $it.hatching " },
	(Event.Type.PLAYER_TELEPORT):       { PlayerTeleportEvent it    -> log "$it.eventName ($it.player.name): ${locstr(it.from)} to ${locstr(it.to)}" },
	(Event.Type.PLAYER_PORTAL):         { PlayerPortalEvent it      -> log "$it.eventName ($it.player.name): ${locstr(it.from)} to ${locstr(it.to)}" },
	(Event.Type.PLAYER_ITEM_HELD):      { PlayerItemHeldEvent it    -> log "$it.eventName ($it.player.name): now $it.newSlot (${itmstr(it.player.inventory.getItem(it.newSlot))}) was $it.previousSlot (${itmstr(it.player.inventory.getItem(it.previousSlot))})" },
    (Event.Type.PLAYER_DROP_ITEM):      { PlayerDropItemEvent it    -> log "$it.eventName ($it.player.name): ${itmstr(it.itemDrop.itemStack)}" },
	(Event.Type.PLAYER_PICKUP_ITEM):    { PlayerPickupItemEvent it  -> log "$it.eventName ($it.player.name): ${itmstr(it.item.itemStack)}" },
    (Event.Type.PLAYER_BUCKET_EMPTY):   { PlayerBucketEmptyEvent it -> log "$it.eventName ($it.player.name)" },
    (Event.Type.PLAYER_BUCKET_FILL):    { PlayerBucketFillEvent it  -> log "$it.eventName ($it.player.name)" },
    (Event.Type.PLAYER_INVENTORY):      { PlayerInventoryEvent it   -> log "$it.eventName ($it.player.name): name=$it.inventory.name, size=$it.inventory.size" },
    (Event.Type.PLAYER_BED_ENTER):      { PlayerBedEnterEvent it    -> log "$it.eventName ($it.player.name): bed=${blkstr(it.bed)}" },
    (Event.Type.PLAYER_BED_LEAVE):      { PlayerBedLeaveEvent it    -> log "$it.eventName ($it.player.name): bed=${blkstr(it.bed)}" },

    (Event.Type.BLOCK_DAMAGE):          { BlockDamageEvent it       -> log "$it.eventName ${blkstr(it.block)}: by $it.player.name, instaBreak=$it.instaBreak, itemInHand=$it.itemInHand" },
	(Event.Type.BLOCK_CANBUILD):        { BlockCanBuildEvent it     -> log "$it.eventName ${blkstr(it.block)}: $it.material, buildable=$it.buildable" },
    //BLOCK_FROMTO (noisy)
	(Event.Type.BLOCK_IGNITE):          { BlockIgniteEvent it       -> log "$it.eventName ${blkstr(it.block)}: by $it.player?.name, cause=$it.cause" },
    //BLOCK_PHYSICS (noisy)
	(Event.Type.BLOCK_PLACE):           { BlockPlaceEvent it        -> log "$it.eventName ${blkstr(it.block)}: by $it.player.name, blockAgainst=${blkstr(it.blockAgainst)}, itemInHand=$it.itemInHand, canBuild=${it.canBuild()}" },
	(Event.Type.BLOCK_DISPENSE):        { BlockDispenseEvent it     -> log "$it.eventName ${blkstr(it.block)}: item=${itmstr(it.item)}, velocity=${vecstr(it.velocity)}" },
	(Event.Type.BLOCK_BURN):            { BlockBurnEvent it         -> log "$it.eventName ${blkstr(it.block)}" },
	(Event.Type.LEAVES_DECAY):          { LeavesDecayEvent it       -> log "$it.eventName ${blkstr(it.block)}" },
	(Event.Type.SIGN_CHANGE):           { SignChangeEvent it        -> log "$it.eventName ${blkstr(it.block)}: by $it.player.name, lines=${it.lines.join(',')}" },
	(Event.Type.REDSTONE_CHANGE):       { BlockRedstoneEvent it     -> log "$it.eventName ${blkstr(it.block)}: $it.oldCurrent->$it.newCurrent" },
	(Event.Type.BLOCK_BREAK):           { BlockBreakEvent it        -> log "$it.eventName ${blkstr(it.block)}: by $it.player.name" },
	(Event.Type.SNOW_FORM):             { SnowFormEvent it          -> log "$it.eventName ${blkstr(it.block)}: data=$it.data" },
	(Event.Type.BLOCK_FORM):            { BlockFormEvent it         -> log "$it.eventName ${blkstr(it.block)}: newState=$it.newState" },
	(Event.Type.BLOCK_SPREAD):          { BlockSpreadEvent it       -> log "$it.eventName ${blkstr(it.block)}: newState=$it.newState" },
	(Event.Type.BLOCK_FADE):            { BlockFadeEvent it         -> log "$it.eventName ${blkstr(it.block)}: newState=$it.newState" },

	(Event.Type.INVENTORY_OPEN):        { PlayerInventoryEvent it   -> log "$it.eventName $it.player.name: " },
	(Event.Type.INVENTORY_CLOSE):       { log "$it.eventName $it.player.name" },
	(Event.Type.INVENTORY_CLICK):       { log "$it.eventName $it.player.name" },
	(Event.Type.INVENTORY_CHANGE):      { log "$it.eventName $it.player.name" },
	(Event.Type.INVENTORY_TRANSACTION): { log "$it.eventName $it.player.name" },

	(Event.Type.PLUGIN_ENABLE):         { PluginEvent it            -> log "$it.eventName: $it.plugin.description.name" },
	(Event.Type.PLUGIN_DISABLE):        { PluginEvent it            -> log "$it.eventName: $it.plugin.description.name" },
    (Event.Type.SERVER_COMMAND):        { ServerCommandEvent it     -> log "$it.eventName: " },

    //CHUNK_LOAD (noisy)
    //CHUNK_UNLOAD (noisy)
    (Event.Type.CHUNK_POPULATED):       { ChunkPopulateEvent it     -> log "$it.eventName: $it.chunk" },
	(Event.Type.ITEM_SPAWN):            { ItemSpawnEvent it         -> log "$it.eventName: location=${locstr(it.location)},${locstr(it.entity.location)}, itemStack=${itmstr(it.entity.itemStack)}" },
	(Event.Type.SPAWN_CHANGE):          { SpawnChangeEvent it       -> log "$it.eventName: previousLocation=${locstr(it.previousLocation)}" },
	(Event.Type.WORLD_SAVE):            { WorldSaveEvent it         -> log "$it.eventName: $it.world" },
	(Event.Type.WORLD_INIT):            { WorldInitEvent it         -> log "$it.eventName: $it.world" },
	(Event.Type.WORLD_LOAD):            { WorldLoadEvent it         -> log "$it.eventName: $it.world" },
	(Event.Type.WORLD_UNLOAD):          { WorldUnloadEvent it       -> log "$it.eventName: $it.world" },
	(Event.Type.PORTAL_CREATE):         { PortalCreateEvent it      -> log "$it.eventName: $it.world" },

    (Event.Type.PAINTING_PLACE):        { PaintingPlaceEvent it     -> log "$it.eventName ${entstr(it.painting)}" },
    (Event.Type.PAINTING_BREAK):        { PaintingBreakEvent it     -> log "$it.eventName ${entstr(it.painting)}: cause=$it.cause" },
    (Event.Type.ENTITY_PORTAL_ENTER):   { EntityPortalEnterEvent it -> log "$it.eventName ${entstr(it.entity)}: location=${locstr(it.location)}" },

    //CREATURE_SPAWN (noisy)
    //ENTITY_DAMAGE (noisy)
	(Event.Type.ENTITY_DEATH):          { EntityDeathEvent it       -> log "$it.eventName ${entstr(it.entity)}: drops=$it.drops" },
    //ENTITY_COMBUST (noisy)
	(Event.Type.ENTITY_EXPLODE):        { EntityExplodeEvent it     -> log "$it.eventName ${entstr(it.entity)}: location=${locstr(it.location)}, yield=$it.yield, blockList.size=${it.blockList().size()}" },
	(Event.Type.EXPLOSION_PRIME):       { ExplosionPrimeEvent it    -> log "$it.eventName ${entstr(it.entity)}: radius=$it.radius, fire=$it.fire" },
	(Event.Type.ENTITY_TARGET):         { EntityTargetEvent it      -> log "$it.eventName ${entstr(it.entity)}: target=$it.target, reason=$it.reason" },
    (Event.Type.ENTITY_INTERACT):       { EntityInteractEvent it    -> log "$it.eventName ${entstr(it.entity)}: block=${blkstr(it.block)}" },
    (Event.Type.CREEPER_POWER):         { CreeperPowerEvent it      -> log "$it.eventName ${entstr(it.entity)}: cause=$it.cause" },
    (Event.Type.PIG_ZAP):               { PigZapEvent it            -> log "$it.eventName ${entstr(it.entity)}" },
    (Event.Type.ENTITY_TAME):           { EntityTameEvent it        -> log "$it.eventName ${entstr(it.entity)}: owner=$it.owner" },
    (Event.Type.ENTITY_REGAIN_HEALTH):  { EntityRegainHealthEvent it-> log "$it.eventName ${entstr(it.entity)}: amount=$it.amount" },

    (Event.Type.LIGHTNING_STRIKE):      { LightningStrikeEvent it   -> log "$it.eventName ${entstr(it.entity)}"},
    (Event.Type.WEATHER_CHANGE):        { WeatherChangeEvent it     -> log "$it.eventName: on=${it.toWeatherState()}" },
    (Event.Type.THUNDER_CHANGE):        { ThunderChangeEvent it     -> log "$it.eventName: on=${it.toThunderState()}" },

    (Event.Type.VEHICLE_CREATE):        { VehicleCreateEvent it     -> log "$it.eventName ${vehstr(it.vehicle)}" },
    (Event.Type.VEHICLE_DESTROY):       { VehicleDestroyEvent it    -> log "$it.eventName ${vehstr(it.vehicle)}" },
    (Event.Type.VEHICLE_DAMAGE):        { VehicleDamageEvent it     -> log "$it.eventName ${vehstr(it.vehicle)}: ${entstr(it.attacker)}, damage=$it.damage" },
    (Event.Type.VEHICLE_COLLISION_ENTITY): { VehicleEntityCollisionEvent it -> log "$it.eventName ${vehstr(it.vehicle)}: ${entstr(it.entity)}" },
    (Event.Type.VEHICLE_COLLISION_BLOCK): { VehicleBlockCollisionEvent it -> log "$it.eventName ${vehstr(it.vehicle)}: ${blkstr(it.block)}" },
    (Event.Type.VEHICLE_ENTER):         { VehicleEnterEvent it      -> log "$it.eventName ${vehstr(it.vehicle)}" },
    (Event.Type.VEHICLE_EXIT):          { VehicleExitEvent it       -> log "$it.eventName ${vehstr(it.vehicle)}" },
    //VEHICLE_MOVE (noisy)
    //VEHICLE_UPDATE (noisy)
]

if (args && args[0] == 'noisy') {
    listen "noisy", [

    	(Event.Type.PLAYER_MOVE):           { PlayerMoveEvent it        -> log "$it.eventName ($it.player.name): ${locstr(it.from)} to ${locstr(it.to)}" },

        (Event.Type.BLOCK_PHYSICS):         { BlockPhysicsEvent it      -> log "$it.eventName: ${blkstr(it.block)} changedType=$it.changedType" },
        (Event.Type.BLOCK_FROMTO):          { BlockFromToEvent it       -> log "$it.eventName ${blkstr(it.block)}: to=${blkstr(it.toBlock)}, face=$it.face" },

        (Event.Type.CHUNK_LOAD):            { ChunkLoadEvent it         -> log "$it.eventName: $it.chunk" },
        (Event.Type.CHUNK_UNLOAD):          { ChunkUnloadEvent it       -> log "$it.eventName: $it.chunk" },

    	(Event.Type.CREATURE_SPAWN):        { CreatureSpawnEvent it     -> log "$it.eventName ${entstr(it.entity)}: creatureType=$it.creatureType" },
    	(Event.Type.ENTITY_COMBUST):        { EntityCombustEvent it     -> log "$it.eventName ${entstr(it.entity)}: fireTicks=$it.entity.fireTicks" },
    	(Event.Type.ENTITY_DAMAGE):         { EntityDamageEvent it      -> log "$it.eventName ${entstr(it.entity)}: cause=$it.cause, damage=$it.damage" },

        (Event.Type.VEHICLE_MOVE):          { VehicleMoveEvent it       -> log "$it.eventName ${vehstr(it.vehicle)}: ${locstr(it.from)} to ${locstr(it.to)}" },
        (Event.Type.VEHICLE_UPDATE):        { VehicleEvent it           -> log "$it.eventName ${vehstr(it.vehicle)}" }

    ]
}