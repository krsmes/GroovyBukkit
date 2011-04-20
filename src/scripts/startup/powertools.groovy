import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.Event
import org.bukkit.*
import org.bukkit.event.block.Action
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.util.BlockIterator
import org.bukkit.block.BlockFace
import org.bukkit.event.Event.Result
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.event.block.BlockDamageEvent
/*
Command: ptools

Arguments: on|off|help

Examples:
    ptools on
        turn powertools on
    ptools off
        turn powertools off
    ptools help
        show powertools help

Usage:
    With powertools turned on (/ptools on) there are several changes to default behavior depending
    on what is in your hand:

    Empty hand:
        Left-Click on block while sneaking (Shift) -- destroy the block clicked on (you don't get to pick it up)
        Right-Click on block -- copy block clicked on to your hand
            ...while sneaking (Shift) -- pick up block clicked on

    Stick in hand:
        Right-Click air -- jump (teleport) to distant block (if there is air directly above it)
        Right-Click on block -- show information on clicked on block (and blocks behind it)
        Left-Click on block -- increment data value of block clicked on (cycles through 0..15)
            ...while sneaking (Shift) -- duplicate the block clicked on on the face clicked on

    Any block in hand:
        Left-Click on block -- change clicked on block to the type of block in your hand (including data)
        Right-Click on block (place) -- place the block but don't decrease quantity in hand (infinite block)


 */

command 'ptools', { runner, args ->

    if (args && args.size() == 1) {
        if (args[0] == 'help') {
            [
                "/ptools on|off : turn powertools on or off",
                "Hand-Sneak-Click : destroy block",
                "Hand-RightClick : copy block to hand",
                "Hand-Sneak-RightClick : pick up block",
                "Stick-RightClick-Air : teleport to target",
                "Stick-RightClick : show block info",
                "Stick-Click : cycle block data",
                "Stick-Sneak-Click : duplicate block",
                "Block-Click : change block to block in hand",
                "Block-RightClick : infinite place block"
            ].each { runner.player.sendMessage(it) }
        }
        else {
            runner.data.powertools = (args[0] == 'on')
        }
    }
    "PowerTools are ${runner.data.powertools ? 'on' : 'off'} ('/ptools help' for info)"
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
    if (!global.temp.blockIncrementType) {
        global.temp.blockIncrementType = [
                (Material.GRASS): Material.SOIL,
                (Material.SOIL): Material.DIRT,
                (Material.DIRT): Material.GRASS,

                (Material.STONE): Material.COBBLESTONE,
                (Material.COBBLESTONE): Material.MOSSY_COBBLESTONE,
                (Material.MOSSY_COBBLESTONE): Material.BRICK,
                (Material.BRICK): Material.CLAY,
                (Material.CLAY): Material.STONE,

                (Material.SAND): Material.SANDSTONE,
                (Material.SANDSTONE): Material.SOUL_SAND,
                (Material.SOUL_SAND): Material.SAND,

                (Material.ICE): Material.SNOW,
                (Material.SNOW): Material.ICE,

                (Material.WOOD): Material.GLASS,
                (Material.GLASS): Material.WOOD,

                (Material.GOLD_BLOCK): Material.IRON_BLOCK,
                (Material.IRON_BLOCK): Material.GOLD_BLOCK,

                (Material.LAPIS_BLOCK): Material.DIAMOND_BLOCK,
                (Material.DIAMOND_BLOCK): Material.LAPIS_BLOCK,

                (Material.YELLOW_FLOWER): Material.RED_ROSE,
                (Material.RED_ROSE): Material.RED_MUSHROOM,
                (Material.RED_MUSHROOM): Material.BROWN_MUSHROOM,
                (Material.BROWN_MUSHROOM): Material.YELLOW_FLOWER,

                (Material.GLOWSTONE): Material.NETHERRACK,
                (Material.NETHERRACK): Material.SPONGE,
                (Material.SPONGE): Material.NETHERRACK
        ]

        global.temp.blockDataMax = [
                (Material.COBBLESTONE_STAIRS): 3,
                (Material.WOOD_STAIRS): 3,
                (Material.LOG): 2,
                (Material.LEAVES): 2
        ]

    }
    def blockIncrementType = global.temp.blockIncrementType
    if (blockIncrementType.containsKey(block.type)) {
        block.type = blockIncrementType[block.type]
    }
    else {
        def blockDataMax = global.temp.blockDataMax
        block.data += 1
        if (block.data > 15 || (blockDataMax.containsKey(block.type) && block.data > blockDataMax[block.type])) block.data = 0
    }
}


def changeClickedBlockToItem(Block block, ItemStack item) {
    if ((item.type == Material.SNOW_BLOCK || item.type == Material.SNOW_BALL) && (block + 1).typeId == 0) {
        (block + 1).type = Material.SNOW;
    }
    else {
        block.type = item.type
        block.data = item.durability // ???
    }
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
    (Event.Type.PLAYER_JOIN): { runner, PlayerJoinEvent e ->
        if (runner.permitted('ptools')) {
            runner.player.sendMessage "You have ptools permissions, see '/ptools help'"
        }
    },

    (Event.Type.BLOCK_DAMAGE): { runner, BlockDamageEvent e ->
        if (!runner.data.powertools || e.cancelled) return

        if (runner.player.itemInHand.type == Material.STICK) e.cancelled = true
    },

    (Event.Type.PLAYER_INTERACT): { runner, PlayerInteractEvent e ->
//        println "powertools(${runner.data.powertools ? 'ON' : 'OFF'}): $e.eventName ($e.player.name): item=$e.item, action=$e.action, clickedBlock=$e.clickedBlock, blockFace=$e.blockFace, useBlock=${e.useInteractedBlock()}"

        if (!runner.data.powertools ||
                (e.clickedBlock &&
                        e.clickedBlock.type in [Material.WOODEN_DOOR, Material.CHEST, Material.WORKBENCH]
                )
            ) {
            return
        }
        else if (e.useInteractedBlock() != Result.DENY) {
            if (e.item == null) {
                if (e.action == Action.LEFT_CLICK_BLOCK && e.player.sneaking) future { killBlock(e.clickedBlock) }
                else if (e.action == Action.RIGHT_CLICK_BLOCK) future { pickupBlock(e.player, e.clickedBlock) }
            }
            else if (e.item.type.isBlock() && e.useInteractedBlock() != Result.DENY) {
                if (e.action == Action.LEFT_CLICK_BLOCK) future { changeClickedBlockToItem(e.clickedBlock, e.item) }
                else if (e.action == Action.RIGHT_CLICK_BLOCK && e.player.sneaking) e.player.itemInHand.amount += 1
            }
            else if (e.item.type == Material.STICK) {
                if (e.action == Action.LEFT_CLICK_BLOCK)
                    if (e.player.sneaking) future { duplicateBlock(e.clickedBlock, e.blockFace) }
                    else future { incrementData(e.clickedBlock) }
            }

        }

        if (e.item?.type == Material.STICK) {
            // these are allowed even if useBlock=DENY
            if (e.action == Action.RIGHT_CLICK_AIR) future { jumpToTarget(e.player) }
            else if (e.action == Action.RIGHT_CLICK_BLOCK) future { stickClick(e.player, runner, e.clickedBlock) }
        }

    }

]
