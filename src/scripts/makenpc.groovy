import redecouverte.npcspawner.BasicHumanNpc
import redecouverte.npcspawner.BasicHumanNpcList
import redecouverte.npcspawner.NpcSpawner

def (uniqueId, name) = args.toList()

def npcs = global.npcs ?: new BasicHumanNpcList()
global.npcs = npcs

if (npcs.get(uniqueId) != null) {
    me.sendMessage("This npc-id is already in use.")
}
else {
    BasicHumanNpc hnpc = NpcSpawner.SpawnBasicHumanNpc(uniqueId, name, w, here.x, here.y, here.z, here.yaw, here.pitch)
    npcs.put(uniqueId, hnpc)
    hnpc.bukkitEntity.itemInHand = i('bookshelf')
    hnpc
}
