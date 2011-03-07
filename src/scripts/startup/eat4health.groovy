[
    'hour change': {
        if (it.hour in [0, 8, 16]) {
            it.world.players.each {
                it.health -= 1
            }
        }
    }
]
