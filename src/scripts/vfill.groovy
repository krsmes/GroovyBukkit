import org.bukkit.util.Vector

def v1 = args[0] as Vector
def v2 = args[1] as Vector
def material = m(args[2])

def min = Vector.getMinimum(v1, v2)
def max = Vector.getMaximum(v1, v2)

(min.blockX..max.blockX).each{ xx ->
	(min.blockY..max.blockY).each{ yy ->
		(min.blockZ..max.blockZ).each{ zz ->
			w[v(xx,yy,zz)] = material }}}
