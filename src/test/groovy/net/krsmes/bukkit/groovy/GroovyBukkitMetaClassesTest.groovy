package net.krsmes.bukkit.groovy

import org.junit.Test
import org.junit.Before
import org.bukkit.craftbukkit.inventory.CraftInventory
import net.minecraft.server.TileEntityChest
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class GroovyBukkitMetaClassesTest {

    @Before
    void enable() {
        GroovyBukkitMetaClasses.enable()
    }


    @Test
    void checkVectorMetaClass() {
        def v = new Vector(1, 2, 3)
        assert v.toString() == 'Vec[xyz=1.00:2.00:3.00]'

        v = Vector.fromString('Vec[xyz=3.00:2.0:1]')
        assert v.x == 3.0d
        assert v.y == 2.0d
        assert v.z == 1.0d

        def v1 = v + ([1,2,3] as Vector)
        assert v1.x == 4.0d
        assert v1.y == 4.0d
        assert v1.z == 4.0d
        assert v.x == 3.0d
        assert v.y == 2.0d
        assert v.z == 1.0d

        def v2 = v1 - [1, 2, 3]
        assert v2.x == 3.0d
        assert v2.y == 2.0d
        assert v2.z == 1.0d
    }


    @Test
    void checkItemStackMetaClass() {
        def is = new ItemStack(3,2,(short)1)
        assert is.toString() == 'Stk[type=3,data=1,qty=2]'

        is = ItemStack.fromString('Stk[type=1,data=2,qty=3]')
        assert is.typeId == 1
        assert is.durability == 2
        assert is.amount == 3
    }


    @Test
    void checkInventoryMetaClass() {
        def ci = new CraftInventory(new TileEntityChest())
        assert ci.toString() == "Inv[;;;;;;;;;;;;;;;;;;;;;;;;;;]"

        def i1 = new ItemStack(3, 2)
        def i2 = new ItemStack(1, 64)

        ci << i1
        assert ci[0] == i1
        ci[1] = i2
        def list = ci as List
        assert list.findAll{it} == [i1, i2]
        assert ci.toString() == 'Inv[Stk[type=3,data=0,qty=2];Stk[type=1,data=0,qty=64];;;;;;;;;;;;;;;;;;;;;;;;;]'

        ci = new CraftInventory(new TileEntityChest())
        ci.fromString('Inv[Stk[type=3,data=0,qty=2];Stk[type=1,data=0,qty=64]]')
        list = ci as List
        assert list.findAll{it} == [i1, i2]
    }
}
