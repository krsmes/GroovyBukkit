listen 'eat4health', 'hour change', {
    println "hour change"
    if (it.hour in [0,8,16]) {
        println "hour $it.hour"
        it.world.players.each {
            println "$it.name :: $it.health"
            it.health -= 1
            println "$it.name :: $it.health"
        }
    }
}