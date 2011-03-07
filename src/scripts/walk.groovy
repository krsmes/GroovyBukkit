def l1=l(at)+1
def n = global.npcs?."${args[0]}"

me.sendMessage "$n"

def path = w.handle.a(n.mcEntity, (int)l1.x, (int)l1.y, (int)l1.z, 50.0f)
//def path = w.handle.a(n.mcEntity, me.handle, 50.0f)

path.b.each { point ->
    future {
        n.moveTo(point.a, point.b, point.c, 0.0f, 0.0f)
        sleep 100
    }
}

''
