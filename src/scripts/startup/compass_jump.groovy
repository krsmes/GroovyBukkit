import groovy.transform.Field
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.util.Vector
import org.bukkit.ChatColor
import net.krsmes.bukkit.groovy.GroovyRunner

/*
    Compass in hand:
        Right-Click air -- jump (teleport) to distant block (if there is air directly above it)
        Right-Click on block -- show information on clicked on block

 */

@Field static NON_BLOCK_TYPEIDS = [0, 0x06, 0x1f, 0x20, 0x25, 0x26, 0x27, 0x28, 0x32, 0x37, 0x3b, 0x3f, 0x44, 0x45, 0x46, 0x48, 0x4b, 0x4c, 0x4d, 0x4e, 0x53, 0x5a]


def jumpToTarget(Player player, Block target) {
    if (target && target.typeId > 0) {
        def attempts = 6
        while (!(target.typeId in NON_BLOCK_TYPEIDS) && attempts--) { target += 1 }
        if (target.typeId in NON_BLOCK_TYPEIDS && (target+1).typeId in NON_BLOCK_TYPEIDS) {
            def playerLoc = player.location
            def loc = new Location(playerLoc.world, target.x, target.y, target.z, playerLoc.yaw, playerLoc.pitch)
            player.teleport(loc)
        }
        else {
            player.sendMessage "Cannot jump to target"
        }
    }
    else {
        player.sendMessage "Unable to acquire target"
    }
}


def targetInfo(Player player, Block target) {
    if (target) {
        /*
        show:
            x, y, z
            id in hex
            id in decimal
            state
            facing direction
            distance
         */
        def dist = (player.location as Vector).distance(target.location as Vector).round()
        player.sendMessage "$ChatColor.DARK_PURPLE$target.x,$target.y,$target.z 0x${Integer.toHexString(target.typeId).toUpperCase()} $target.typeId $target.state.data (${f(player)} $dist)"
    }
    else {
        player.sendMessage "Unable to acquire target"
    }
}



[
    (Event.Type.PLAYER_INTERACT): { GroovyRunner r, PlayerInteractEvent e ->
        if (e.item?.type == Material.COMPASS) {
            if (e.action == Action.LEFT_CLICK_AIR || e.action == Action.LEFT_CLICK_BLOCK) {
                def p = e.player
                future { jumpToTarget(p, e.clickedBlock ?: p.getTargetBlock(null, 128)) }
            }
            else if (e.action == Action.RIGHT_CLICK_AIR || e.action == Action.RIGHT_CLICK_BLOCK) {
                def p = e.player
                def t = e.clickedBlock
                if (t) r.data.lastClick = t.location
                future { targetInfo(p, t ?: p.getTargetBlock(null, 128)) }
            }
        }
    }
]