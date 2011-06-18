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
import org.bukkit.block.BlockState
import org.bukkit.inventory.ItemStack
import org.bukkit.entity.Item
import groovy.transform.Field
import net.krsmes.bukkit.groovy.GroovyRunner

def approvedDispense = []

def setupContainerSign = { GroovyRunner r, ContainerBlock container, Sign sign ->
    def contents = container.inventory.contents
    switch (container.type) {
        case Material.DISPENSER:
            sign.setLine(1, 'dispensing')
            def next = contents.find {it}
            if (next) {
                sign.setLine(2, "$next.typeId")
                sign.setLine(3, "$next.type")
            }
            else {
                sign.setLine(2, "(EMPTY)")
                sign.setLine(3, "")
            }
            break
        case Material.CHEST:
            r.reg(container.block, 'captureItem')
            sign.setLine(1, 'dropbox')
            def accept = contents[0, 9, 18].findAll {it}.typeId.join(',')
            if (accept) {
                sign.setLine(2, 'accepting')
                sign.setLine(3, accept)
            }
            break
    }
    sign.update()
}


def captureItemInContainer = { ContainerBlock container, Item item ->
    def itemStack = item.itemStack
    def dropinbox = false
    container.block.findAttached { it.type == Material.WALL_SIGN }?.with {
        def accept = state.getLine(3)
        def acceptIds = accept ? accept.split(',')*.toInteger() : []
        dropinbox = !acceptIds || (itemStack.typeId in acceptIds)
    }
    if (dropinbox) {
        container.inventory.addItem(itemStack)
        container.block.findAllAttached { it.type == Material.DISPENSER }.each {
            approvedDispense.add(it)
            it.state.dispense()
            approvedDispense.remove(it)
        }
        item.remove()
    }
    dropinbox
}

// register 'captureItem' closure to be used with container blocks
reg('captureItem') { GroovyRunner r, Block blk ->
    r.e(blk, 2, 'item').each { captureItemInContainer(blk.state, it) }
}


[
    (Event.Type.BLOCK_PLACE): { GroovyRunner r, BlockPlaceEvent e ->
        def block = e.block
        if (block.state instanceof Sign && e.player.sneaking) {
            block.state.setLine(0, e.player.name)
            Block attached = block.getFace(block.state.data.attachedFace)
            if (attached.state instanceof ContainerBlock) {
                setupContainerSign(r, attached.state, block.state)
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
            // don't allow containers with attached signs to be destroyed except by player on sign
            block.findAttached { it.type == Material.WALL_SIGN }?.with {
                e.cancelled = (e.player.name != state.getLine(0))
            }
        }
        else if (block.type == Material.WALL_SIGN && block.getFace(block.state.data.attachedFace).state instanceof ContainerBlock) {
            // don't allow signs attached to containers to be destroyed except by player on sign
            e.cancelled = (e.player.name != block.state.getLine(0))
            if (e.cancelled) {
                // send block update on signs so the text is restored
                future 50, { block.state.update() }
            }
        }
    },

    (Event.Type.BLOCK_DISPENSE): { BlockDispenseEvent e ->
        def block = e.block
        def sign = block.findAttached { it.type == Material.WALL_SIGN }
        if (sign) {
            // if sign on dispenser, only dispense 'approvedDispense' (from item being dropped in container)
            // this prevents a button being placed nearby and stealing the contents
            if (block in approvedDispense) {
                future 20, { setupContainerSign(block.state, sign.state) }
            }
            else e.cancelled = true
        }
    },

    (Event.Type.ITEM_SPAWN): { ItemSpawnEvent e ->
        def ent = e.entity
        future 20, {
            ent.location.findBlock(1) { it.type == Material.CHEST }?.with {
                captureItemInContainer(it.state, ent)
            }
        }
    }
]