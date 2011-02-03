import net.minecraft.server.EntityCow
import net.minecraft.server.World

def wH = w.handle
def cow = new EntityCow(wH)
wH.a(cow)

cow.bukkitEntity.teleportTo(me)
cow.bukkitEntity
