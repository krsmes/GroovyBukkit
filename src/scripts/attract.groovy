import java.lang.reflect.Field
import org.bukkit.util.Vector

net.minecraft.server.EntityCreature.metaClass.getPath = {->
	Field f = net.minecraft.server.EntityCreature.class.getDeclaredField('a')
	f.setAccessible(true)
	f.get(delegate)
}
net.minecraft.server.EntityCreature.metaClass.setPath = { val ->
	Field f = net.minecraft.server.EntityCreature.class.getDeclaredField('a')
	f.setAccessible(true)
	f.set(delegate, val)
}

def ent = args[0]
def hEnt = ent.handle
def target = args.length > 1 ? args[1] : me
def hTarget = target.handle
def radius = args.length > 2 ? args[2] : 128.0f

//hEnt.e = false
hEnt.d = hTarget

if (!(ent as Vector).isInSphere(target as Vector, 15.0)) {
	Thread.start {
		msg 'Thread Start'
		while (!(ent as Vector).isInSphere(target as Vector, 15.0)) {
			hEnt.path = w.handle.a(hEnt, hTarget, radius)
			sleep 250
		}
		msg 'Thread Done'
	}
}

"$ent following $target"
