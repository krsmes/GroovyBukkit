import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.Event
import org.bukkit.*
import org.bukkit.event.block.Action

listen 'temp', [
    (Event.Type.PLAYER_INTERACT): { PlayerInteractEvent e ->
        debug "temp: $e.eventName ($e.player.name): item=$e.item, action=$e.action, clickedBlock=$e.clickedBlock, blockFace=$e.blockFace"
        if (e.action ==  Action.RIGHT_CLICK_BLOCK && e.item.type == Material.BUCKET) {
            debug "temp: future"
            future {
                def stack = e.clickedBlock.state.data.toItemStack(16)
                debug "temp: addItem($stack)"
                e.player.inventory.addItem(stack)
            }
        }
    }

]