import net.krsmes.bukkit.groovy.GroovyRunner

def swapPlayerInventories(p1, p2) {
    def p1i = p1.inventory.toString()
    def p2i = p2.inventory.toString()
    p1.inventory.fromString(p2i)
    p2.inventory.fromString(p1i)
}


command 'inv', { GroovyRunner runner, List args ->
    def invName = args ? args[0] : 'default'
    runner.switchInv(invName)
    "Inventory switch to '$invName'"
}

command 'swap', { GroovyRunner runner, List args ->
    def player = runner.player
    def playerName = player.name
    def otherPlayerName = args ? args[0] : '*'
    def swaps = runner.global.temp.swaps ?: []
    runner.global.temp.swaps = swaps

    if (otherPlayerName == '-') {
        swaps.removeAll{it.endsWith(':'+playerName)}
    }
    else if (otherPlayerName == '*') {
        otherPlayerName = swaps.find{it.startsWith('*:')}?.substring(2)
        if (otherPlayerName) {
            def otherPlayer = runner.p(otherPlayerName)
            def swapName = '*:' + otherPlayerName
            if (otherPlayer) {
                // do the swap
                swapPlayerInventories(player, otherPlayer)
                swaps.remove(swapName)
                "Inventory swapped with $otherPlayerName"
                otherPlayer.sendMessage("Inventory swapped with $playerName")
            }
            else {
                // not online, take off the list
                swaps.remove(swapName)
                "Try again, $otherPlayerName is not online and has been removed from swap list"
            }
        }
        else {
            // add me as an 'any' swap
            swaps << '*:' + playerName
            "Waiting for someone else to swap"
        }
    }
    else {
        def otherPlayer = runner.p(otherPlayerName)
        if (otherPlayer) {
            otherPlayerName = otherPlayer.name
            // look for an existing swap by the other player
            def swapName = playerName + ':' + otherPlayerName
            if (swaps.contains(swapName)) {
                // do the swap
                swapPlayerInventories(player, otherPlayer)
                swaps.remove(swapName)
                "Inventory swapped with $otherPlayerName"
                otherPlayer.sendMessage("Inventory swapped with $playerName")
            }
            else {
                // add swap to the list (swap will happen when other player swaps with this name)
                swaps << (otherPlayerName + ':' + playerName)
                "Waiting for $otherPlayerName to swap"
                otherPlayer.sendMessage("Type /swap $playerName to swap inventory with $playerName")
            }
        }
        else {
            "$otherPlayerName not found"
        }
    }
}
