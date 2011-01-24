import org.bukkit.entity.Entity

def h = w.handle
def loc = args.size() > 1 ? args[1] : loc(x, y+1, z)
if (loc instanceof Entity) loc = ((Entity)loc).location

def ent = net.minecraft.server.EntityList.a(args[0], h)
ent.c(loc.x+0.5f, loc.y+1.0f, loc.z+0.5f, 0.0f, 0.0f)
h.a(ent)

ent.bukkitEntity.teleportTo(loc)
ent.bukkitEntity
