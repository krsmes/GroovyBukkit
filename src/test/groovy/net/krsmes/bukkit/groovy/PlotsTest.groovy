package net.krsmes.bukkit.groovy

import org.junit.Test
import org.bukkit.entity.Entity
import org.bukkit.event.entity.ExplosionPrimeEvent
import org.junit.Before
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.entity.CreatureType
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.entity.EntityTargetEvent.TargetReason
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.event.weather.LightningStrikeEvent
import org.bukkit.entity.LightningStrike
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

/**
 * PlotsTest ...
 *
 * @author Kevin R. Smith - ksmith@pillartechnology.com
 * @since 2011-06-20
 */
public class PlotsTest {

    Plots plots
    Plot plot
    Entity inPlotEntity
    Player inPlotOwner
    Player inPlotVisitor
    Player inPlotPlayer

    @Before
    void initialize() {
        plots = Plots.enable(BukkitFixtures.plugin)
        plots.load(
            (Plots.ATTR_PLOT_PROTECTION): true
        )
        plot = new Plot('testPlot', new Area(1, 5, 1, 5))
        plots.addPlot(plot)
        inPlotEntity = BukkitFixtures.makeEntity(Entity, [location: BukkitFixtures.makeLocation(x: 1, y: 64, z: 1)])
        inPlotOwner = BukkitFixtures.makePlayer([location: BukkitFixtures.makeLocation(x: 2, y: 64, z: 2)])
        plot.owner = inPlotOwner.name
        inPlotVisitor = BukkitFixtures.makePlayer([location: BukkitFixtures.makeLocation(x: 2, y: 64, z: 2)])
        plot.addVisitor(inPlotVisitor)
        inPlotPlayer = BukkitFixtures.makePlayer([location: BukkitFixtures.makeLocation(x: 2, y: 64, z: 2)])
    }

    @Test
    void testFindPlot() {
        def plot = new Plot('testPlot', new Area(1, 5, 1, 5));
        plots.addPlot(plot);

        assert plots.findPlot('testPlot') == plot
        assert plots.findPlot('otherPlot') != plot

        assert plots.findPlot(1, 1) == plot
        assert plots.findPlot(5, 5) == plot
        assert plots.findPlot(1, 5) == plot
        assert plots.findPlot(5, 1) == plot
        assert plots.findPlot(0, 0) != plot
        assert plots.findPlot(6, 6) != plot

        assert plots.findPlot(plot, 1, 1) == plot
        assert plots.findPlot(plot, 0, 0) != plot
    }


    @Test
    void testExecute() {
        // basic 'no' features
        [
            noExplode: new ExplosionPrimeEvent(inPlotEntity, 3.0, false),
            noSpawn: new CreatureSpawnEvent(inPlotEntity, CreatureType.CHICKEN, BukkitFixtures.makeLocation(x:1,y:64,z:1), SpawnReason.NATURAL),
            noTarget: new EntityTargetEvent(inPlotEntity, inPlotVisitor, TargetReason.CLOSEST_PLAYER),
            noChat: new PlayerChatEvent(inPlotVisitor, "hey you"),
            noLightning: new LightningStrikeEvent(BukkitFixtures.world,
                    BukkitFixtures.makeEntity(LightningStrike, [location:BukkitFixtures.makeLocation(x:1,y:64,z:1)])),
            noIgnite: new BlockIgniteEvent(BukkitFixtures.makeBlock([x:1,y:63,z:1,typeId:1]), IgniteCause.FLINT_AND_STEEL, inPlotOwner),
            noDamage: new EntityDamageEvent(inPlotVisitor, DamageCause.ENTITY_ATTACK, 5)
        ].each { key, value ->
            expectCancelled(false, value) { plot."$key" = false }
            expectCancelled(true, value) { plot."$key" = true }
        }

        // should still spawn if not natural
        def event = new CreatureSpawnEvent(inPlotEntity, CreatureType.CHICKEN, BukkitFixtures.makeLocation(x: 1, y: 64, z: 1), SpawnReason.SPAWNER)
        expectCancelled(false, event) { plot.noSpawn = true }

        // should still target non-visitors
        event = new EntityTargetEvent(inPlotEntity, inPlotPlayer, TargetReason.CLOSEST_PLAYER)
        expectCancelled(false, event) { plot.noTarget = true }

        // should still damage non-visitors
        event = new EntityDamageEvent(inPlotPlayer, DamageCause.ENTITY_ATTACK, 5)
        expectCancelled(false, event) { plot.noDamage = true }
    }


    def expectCancelled = { cancelled, event, setup ->
        setup()
        event.cancelled = false
        plots.execute(plots, event)
        assert event.cancelled == cancelled
    }
}
