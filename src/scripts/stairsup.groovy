import org.bukkit.block.Block

Block j = at
while (j.typeId != 0) {
	j.typeId = 67
    def js = j.state
    js.data.facingDirection = fac.oppositeFace
    js.update()
	(1..4).each { (j+it).typeId = 0 }
	j = j + 1
	j = j + fac
}
j

