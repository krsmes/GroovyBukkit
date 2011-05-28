[
    'hour change': {
        if (it.hour in [0, 6, 12, 18]) {
            it.world.players.each {
                if (it.health > 1) it.health -= 1
            }
        }
    }
]
