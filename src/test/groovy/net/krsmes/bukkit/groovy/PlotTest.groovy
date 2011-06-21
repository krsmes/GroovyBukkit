package net.krsmes.bukkit.groovy

import org.junit.Test
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack


class PlotTest {


    @Test
    void testAllowed() {
        def player = 'krsmes'

        def expectAllowed = { allowed, setup ->
            def plot = new Plot()
            setup(plot)
            assert plot.allowed(player) == allowed
        }

        expectAllowed(false) {}
        expectAllowed(true)  { it.open = true }
        expectAllowed(true)  { it.owner = 'krsmes' }
        expectAllowed(true)  { it.addVisitor('krsmes') }
    }

    @Test
    void testBlockDamageEvent() {
        def plot = new Plot()
        def player = [getName: {'krsmes'}] as Player
        def block = [getY: {64}, getTypeId: {1}] as Block
        def event = new BlockDamageEvent(player, block, null, false)

        def expectCancelled = { cancelled, setup ->
            setup()
            event.cancelled = false
            plot.processEvent(event)
            assert event.cancelled == cancelled
        }

        expectCancelled(false) { plot.owner = 'krsmes' }
        expectCancelled(true)  { plot.owner = 'other' }
        expectCancelled(false) { plot.addVisitor('krsmes') }
        expectCancelled(true)  { plot.setUnbreakable([1] as Set) }
        expectCancelled(false) { plot.setBreakable([1] as Set) }
        expectCancelled(false) { plot.removeVisitor('krsmes') }
        expectCancelled(true)  { plot.setBreakable([] as Set) }
    }


    @Test
    void testPlayerInteractEventWithBlock() {
        def plot = new Plot()
        def player = [getName: {'krsmes'}] as Player
        def block = [getY: {64}, getTypeId: {1}] as Block
        def event = new PlayerInteractEvent(player, null, null, block, null)

        def expectCancelled = { cancelled, setup ->
            setup()
            event.cancelled = false
            plot.processEvent(event)
            assert event.cancelled == cancelled
        }

        expectCancelled(false) { plot.owner = 'krsmes' }
        expectCancelled(true)  { plot.owner = 'other' }
        expectCancelled(false) { plot.addVisitor('krsmes') }
        expectCancelled(true)  { plot.setUninteractable([1] as Set) }
        expectCancelled(false) { plot.setInteractable([1] as Set) }
        expectCancelled(false) { plot.removeVisitor('krsmes') }
        expectCancelled(true)  { plot.setInteractable([] as Set) }
    }


    @Test
    void testPlayerInteractEventWithItemInHand() {
        def plot = new Plot()
        def player = [getName: {'krsmes'}] as Player
        def block = [getY: {64}, getTypeId: {1}] as Block
        def stack = new ItemStack(1)
        def event = new PlayerInteractEvent(player, null, stack, block, null)

        def expectCancelled = { cancelled, setup ->
            setup()
            event.cancelled = false
            plot.processEvent(event)
            assert event.cancelled == cancelled
        }

        expectCancelled(false) { plot.owner = 'krsmes' }
        expectCancelled(true)  { plot.owner = 'other' }
        expectCancelled(false) { plot.addVisitor('krsmes') }
        expectCancelled(true)  { plot.setUnplaceable([1] as Set) }
        expectCancelled(false) { plot.setPlaceable([1] as Set) }
        expectCancelled(false) { plot.removeVisitor('krsmes') }
        expectCancelled(true)  { plot.setPlaceable([] as Set) }
    }


}
