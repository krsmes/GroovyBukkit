package net.krsmes.bukkit.groovy

import org.junit.Test
import org.bukkit.entity.Player


@SuppressWarnings("GroovyAssignabilityCheck")
class GroovyPluginTest {

    def player1 = [
        getName: {'tester1'}
    ] as Player


    @Test
    void shouldBePermittedWhenNoPermissionsExist() {
        def gp = new GroovyPlugin()
        gp.@enabled = true
        gp.global = [:]

        assert gp.permitted(player1, 'warp')
    }


    @Test
    void shouldBePermittedWhenGloballyPermitted() {
        def gp = new GroovyPlugin()
        gp.@enabled = true
        gp.global = [(gp.DATA_PERMISSIONS): [
            '*': ['warp']
        ]]

        assert gp.permitted(player1, 'warp')
    }


    @Test
    void shouldBePermittedWhenPlayerPermitted() {
        def gp = new GroovyPlugin()
        gp.@enabled = true
        gp.global = [(gp.DATA_PERMISSIONS): [
            'tester1': ['warp']
        ]]

        assert gp.permitted(player1, 'warp')
    }

}
