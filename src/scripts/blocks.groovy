import org.bukkit.util.Vector

def (v1, v2, callback) = args.toList()

v1 = v(v1)
v2 = v(v2)
def min = Vector.getMinimum(v1, v2)
def max = Vector.getMaximum(v1, v2)

(min.blockX..max.blockX).each { xx ->
	(min.blockZ..max.blockZ).each { zz ->
		(min.blockY..max.blockY).each { yy ->
			callback(w[v(xx,yy,zz)])
		}
	}
}
