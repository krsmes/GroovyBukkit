import org.bukkit.event.Event
import org.bukkit.Material
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.Event.Result
import org.bukkit.block.Sign
import org.bukkit.block.ContainerBlock
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.block.Block
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.block.BlockDispenseEvent

[
    (Event.Type.BLOCK_PLACE): { BlockPlaceEvent e ->
        def block = e.block
        if (block.state instanceof Sign && e.player.sneaking) {
            block.state.setLine(0, e.player.name)
            Block attached = block.getFace(block.state.data.attachedFace)
            if (attached.state instanceof ContainerBlock) {
                if (attached.type == Material.DISPENSER) {
                    def contents = attached.state.inventory.contents
                    contents.find{it}?.with {
                        block.state.setLine(1, 'dispensing')
                        block.state.setLine(2, "$type")
                        block.state.setLine(3, "($typeId)")
                    }
                }
                else if (attached.type == Material.CHEST) {
                    block.state.setLine(1, 'dropbox')
                    def contents = attached.state.inventory.contents
                    def accept = contents[0,9,18].findAll{it}.typeId.join(',')
                    if (accept) {
                        block.state.setLine(2, 'accepting')
                        block.state.setLine(3, accept)
                    }
                }
            }
        }
    },

    (Event.Type.PLAYER_INTERACT): { PlayerInteractEvent e ->
        if (e.item?.type == Material.SIGN && e.player.sneaking) {
            e.useInteractedBlock = Result.DENY
            e.useItemInHand = Result.ALLOW
        }
        else if (e.action == Action.RIGHT_CLICK_BLOCK && e.clickedBlock.state instanceof ContainerBlock) {
            e.clickedBlock.findAttached { it.type == Material.WALL_SIGN }?.with {
                e.useInteractedBlock = state.getLine(0) == e.player.name ? Result.ALLOW : Result.DENY
            }
        }
    },

    (Event.Type.BLOCK_DAMAGE): { BlockDamageEvent e ->
        def block = e.block
        if (block.state instanceof ContainerBlock) {
            block.findAttached { it.type == Material.WALL_SIGN }?.with {
                e.cancelled = (e.player.name != state.getLine(0))
            }
        }
        else if (block.type == Material.WALL_SIGN && block.getFace(block.state.data.attachedFace).state instanceof ContainerBlock) {
            e.cancelled = (e.player.name != block.state.getLine(0))
            if (e.cancelled) {
                // send block update on signs so the text is restored
                future 50, { block.state.update() }
            }
        }
    },

    (Event.Type.BLOCK_DISPENSE): { BlockDispenseEvent e ->
        def block = e.block
        future 20, {
            block.findAttached { it.type == Material.WALL_SIGN }?.with {
                if (state.getLine(1) == 'dispensing') {
                    def next = block.state.inventory.contents.find {it}
                    if (next) {
                        state.setLine(2, "$next.type")
                        state.setLine(3, "($next.typeId)")
                    }
                    else {
                        state.setLine(2, "(EMPTY)")
                        state.setLine(3, "")
                    }
                    state.update()
                }
            }
        }
    },

    (Event.Type.ITEM_SPAWN): { ItemSpawnEvent e ->
        def ent = e.entity
        future 20, {
            ent.location.findBlock(1) { it.type == Material.CHEST }?.with {
                def itemStack = ent.itemStack
                def dropinbox = false
                findAttached { it.type == Material.WALL_SIGN }?.with {
                    def accept = state.getLine(3)
                    def acceptIds = accept.split(',')*.toInteger()
                    dropinbox = !accept || (itemStack.typeId in acceptIds)
                }
                if (dropinbox) {
                    state.inventory.addItem(itemStack)
                    findAllAttached { it.type == Material.DISPENSER }.each {
                        it.state.dispense()
                    }
                    ent.remove()
                }
            }
        }
    }
]