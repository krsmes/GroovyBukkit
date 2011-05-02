package net.krsmes.bukkit.groovy

import org.yaml.snakeyaml.representer.Represent
import org.yaml.snakeyaml.representer.Representer

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.CraftWorld


class GroovyBukkitRepresenter extends Representer
{

	GroovyBukkitRepresenter() {
        representers.putAll([
            (Location.class): locationRep,
            (Vector.class): vectorRep,
            (Material.class): materialRep,
            (ItemStack.class): itemStackRep,
            (CraftPlayer.class): playerRep,
            (CraftWorld.class): worldRep
        ])
	}


    def gScalar(String s) { representScalar(GroovyBukkitConstructor.G_TAG, s) }

    def playerScalar(String s) { representScalar(GroovyBukkitConstructor.PLAYER_TAG, s) }

    def worldScalar(String s) { representScalar(GroovyBukkitConstructor.WORLD_TAG, s) }


    def playerRep = [representData: { data -> playerScalar(data.name) }] as Represent
    def worldRep = [representData: { data -> worldScalar(data.name) }] as Represent

    def locationRep = [representData: { data -> gScalar(gLocation(data)) }] as Represent
    def vectorRep = [representData: { data -> gScalar(gVector(data)) }] as Represent
    def materialRep = [representData: { data -> gScalar(gMaterial(data)) }] as Represent
    def itemStackRep = [representData: { data -> gScalar(gItemStack(data)) }] as Represent


    static gLocation = { Location data -> "l($data.x, $data.y, $data.z, $data.yaw, $data.pitch)" }

    static gVector = { Vector data -> "v($data.x, $data.y, $data.z)" }

    static gMaterial = { Material data -> "m(\"${data.name()}\")" }

    static gItemStack = { ItemStack data -> "i(\"${data.type.name()}\", ${data.data?.data ?: 0}, ${data.amount})" }

}
