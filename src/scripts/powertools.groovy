import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.Event
import org.bukkit.*
import org.bukkit.event.block.Action
import org.bukkit.block.Block

listen 'powertools', [

    (Event.Type.PLAYER_INTERACT): { PlayerInteractEvent e ->
        debug "temp: $e.eventName ($e.player.name): item=$e.item, action=$e.action, clickedBlock=$e.clickedBlock, blockFace=$e.blockFace"
        if (e.clickedBlock?.type in [Material.WOODEN_DOOR, Material.CHEST, Material.WORKBENCH]) return

        else if (e.item.type == Material.BUCKET && e.action == Action.RIGHT_CLICK_BLOCK) {
            future { addClickedBlockToInventory(e.player, e.clickedBlock, 16) }
        }

        else if (e.item.type == Material.STICK) {
            if (e.action == Action.LEFT_CLICK_AIR) future { jumpToTarget(e.player) }
            if (e.action == Action.LEFT_CLICK_BLOCK) future { incrementData(e.clickedBlock) }
        }

        else if (e.item.type.isBlock() && e.action == Action.LEFT_CLICK_BLOCK) {
            future{ changeClickedBlockToItem(e.clickedBlock, e.item) }
        }
    }

]


def addClickedBlockToInventory(player, block, qty) {
    def stack = block.state.data.toItemStack(qty)
    player.inventory.addItem(stack)
}


def jumpToTarget(player) {
    def target = player.getTargetBlock(null, 128)
    if (target) {
        def above = target + 1
        if (above.typeId == 0) {
            player.teleport(above.location)
        }
    }
}


def incrementData(Block block) {
    block.data += 1
    if (block.data > 15) block.data = 0
}


def changeClickedBlockToItem(block, item) {
    block.type = item.type
    block.data = item.data.data
}