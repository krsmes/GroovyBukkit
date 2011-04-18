import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.Event
import org.bukkit.*
import org.bukkit.event.block.Action
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.util.BlockIterator
import org.bukkit.block.BlockFace


command 'ptools', { runner, args ->

    if (args && args.size() == 1) {
        runner.data.powertools = (args[0] == 'on')
        runner.player.sendMessage("PowerTools ${runner.data.powertools?'ON':'OFF'}")
    }
}



def killBlock(Block block) {
    if (block.y > 1) block.type = Material.AIR
}


def pickupBlock(Player player, Block block) {
    player.itemInHand = block.state.data.toItemStack(1)
    if (player.sneaking) killBlock(block)
}


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


def duplicateBlock(Block block, BlockFace face) {
    def dup = block + face
    dup.type = block.type
    dup.data = block.data
}


def stickClick(Player player, runner, Block block) {
    addStickClick(runner, block)
    player.sendMessage("$ChatColor.DARK_PURPLE${f(player)} $block.x,$block.y,$block.z:$block.typeId $block.state.data")
    def blocks = new BlockIterator(player, 25).toList()
    def msg = blocks[1..-1].collect { it.typeId in [0,1,2,3,4,8,9,12,13,24,60,78,79] ? it.type.toString().toLowerCase() : it.type.toString() }.join(', ')
    player.sendMessage("$ChatColor.GRAY$msg")
}


def addStickClick(runner, Block block) {
    List stickClicks = runner.data.stickClicks
    if (!stickClicks) { stickClicks = []; runner.data.stickClicks = stickClicks }
    stickClicks.add(0, block.location)
    if (stickClicks.size() > 8) stickClicks.pop()
}



[

    (Event.Type.PLAYER_INTERACT): { runner, PlayerInteractEvent e ->
        debug "powertools(${runner.data.powertools ? 'ON' : 'OFF'}): $e.eventName ($e.player.name): item=$e.item, action=$e.action, clickedBlock=$e.clickedBlock, blockFace=$e.blockFace"

        if (!runner.data.powertools || e.clickedBlock?.type in [Material.WOODEN_DOOR, Material.CHEST, Material.WORKBENCH]) return

        else if (e.item == null) {
            if (e.action == Action.LEFT_CLICK_BLOCK && e.player.sneaking) future { killBlock(e.clickedBlock) }
            else if (e.action == Action.RIGHT_CLICK_BLOCK) future { pickupBlock(e.player, e.clickedBlock) }
        }

        else if (e.item.type == Material.STICK) {
            if (e.action == Action.RIGHT_CLICK_AIR) future { jumpToTarget(e.player) }
            else if (e.action == Action.RIGHT_CLICK_BLOCK) future { stickClick(e.player, runner, e.clickedBlock) }
            else if (e.action == Action.LEFT_CLICK_BLOCK && !e.player.sneaking) future { incrementData(e.clickedBlock) }
            else if (e.action == Action.LEFT_CLICK_BLOCK) future { duplicateBlock(e.clickedBlock, e.blockFace) }
        }

        else if (e.item.type.isBlock()) {
            if (e.action == Action.LEFT_CLICK_BLOCK) future { changeClickedBlockToItem(e.clickedBlock, e.item) }
            else if (e.action == Action.RIGHT_CLICK_BLOCK && e.player.sneaking) e.player.itemInHand.amount += 1
        }
    }

]
