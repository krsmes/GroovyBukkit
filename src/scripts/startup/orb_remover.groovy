import net.krsmes.bukkit.groovy.GroovyRunner
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.entity.EntityDeathEvent

def orbRemove(GroovyRunner runner) { runner.e('experienceOrb')*.remove() }

[
        (PlayerRespawnEvent): { GroovyRunner r, PlayerRespawnEvent e ->
            orbRemove(r)
        },

        (EntityDeathEvent): { GroovyRunner r, EntityDeathEvent e ->
            orbRemove(r)
            r.future(20, true) { orbRemove(r) }
        }

]