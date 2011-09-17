import org.bukkit.util.Vector

def ent = args[0]
def hEnt = ent.handle
def target = args.length > 1 ? args[1] : me
def hTarget = target.handle
def radius = args.length > 2 ? args[2] : 128.0f

//hEnt.e = false
hEnt.target = hTarget

if (!(ent as Vector).isInSphere(target as Vector, 15.0)) {
	Thread.start {
		msg 'Thread Start'
		while (!(ent as Vector).isInSphere(target as Vector, 15.0)) {
			hEnt.pathEntity = w.handle.findPath(hEnt, hTarget, radius)
			sleep 250
		}
		msg 'Thread Done'
	}
}

"$ent following $target"
