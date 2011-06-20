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
import org.bukkit.entity.Item
import net.krsmes.bukkit.groovy.GroovyRunner

def approvedDispense = []

def setupContainerSign = { GroovyRunner r, ContainerBlock container, Sign sign ->
    def contents = container.inventory.contents
    switch (container.type) {
        case Material.DISPENSER:
            def next = contents.find {it}
            if (next) {
                if (sign.getLine(1) != 'dispensing') {
                    sign.setLine(1, 'dispensing')
                    sign.setLine(2, "${next.amount}x${next.typeId}")
                    sign.setLine(3, "$next.type")
                }
            }
            else {
                sign.setLine(2, "(EMPTY)")
                sign.setLine(3, "")
            }
            break
        case Material.CHEST:
            r.reg(container.block, 'captureItem')
            // one item per sign (meaning, up to 4)
            def signNum = container.block.findAllAttached{it.type==Material.WALL_SIGN}.size() - 1  // include sign being configured
            contents[signNum]?.with {
                sign.setLine(1, 'accepting')
                sign.setLine(2, "${amount}x${typeId}")
                sign.setLine(3, "$type")
            }
            break
    }
    sign.update()
}


def captureItemInContainer = { ContainerBlock container, Item item ->
    if (item.dead) return false
    def itemStack = item.itemStack
    def dispense = 0
    def acceptTypes = container.block.findAllAttached { it.type == Material.WALL_SIGN }?.collectEntries { signBlock ->
        println "signBlock: $signBlock"
        def accept = signBlock.state.getLine(2)?.split('x')
        println "accept: $accept"
        accept.length==2 ? [(accept[1]):accept[0]] : [:]
    }
    if (acceptTypes) {
        println "acceptType: $acceptTypes"
        // see if item is of acceptable type
        acceptTypes.find {it.key.toInteger() == itemStack.typeId}?.with {
            // calculate dispense count
            dispense = (int) itemStack.amount / value.toInteger()
        }
    }
    else {
        println "accept anything"
        // accept anything
        dispense = 1
    }
    if (dispense) {
        // capture item
        container.inventory.addItem(itemStack)
        item.remove()
        // trigger dispensers
        container.block.findAllAttached { it.type == Material.DISPENSER }.each { dispenser ->
            approvedDispense.add(dispenser)
            def multiplier = 1
            dispenser.findAttached { it.type == Material.WALL_SIGN }?.with {
                def dispenseInfo = state.getLine(2)?.split('x')
                if (dispenseInfo) multiplier = dispenseInfo[0]?.toInteger()
            }
            (dispense * multiplier).times { dispenser.state.dispense() }
            approvedDispense.remove(dispenser)
        }
    }
    dispense
}

// register 'captureItem' closure to be used with container blocks
reg('captureItem') { GroovyRunner r, Block blk ->
    r.e(blk, 2, 'item').each {
        captureItemInContainer(blk.state, it)
    }
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

    (Event.Type.BLOCK_DAMAGE): { GroovyRunner r, BlockDamageEvent e ->
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
                r.future 50, { block.state.update() }
            }
        }
    },

    (Event.Type.BLOCK_DISPENSE): { GroovyRunner r, BlockDispenseEvent e ->
        def block = e.block
        def sign = block.findAttached { it.type == Material.WALL_SIGN }
        if (sign) {
            // if sign on dispenser, only dispense 'approvedDispense' (from item being dropped in container)
            // this prevents a button being placed nearby and stealing the contents
            if (block in approvedDispense) {
                r.future(20) { setupContainerSign(r, block.state, sign.state) }
            }
            else e.cancelled = true
        }
    },

    (Event.Type.ITEM_SPAWN): { GroovyRunner r, ItemSpawnEvent e ->
        def ent = e.entity
        r.future 5, {
            ent.location.findBlock(1) { it.type == Material.CHEST }?.with {
                captureItemInContainer(it.state, ent)
            }
        }
    }
]