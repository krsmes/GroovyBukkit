def (v1, v2, callback) = args.toList()

v1 = v(v1)
v2 = v(v2)
def min = Vector.getMinimum(v1, v2)
def max = Vector.getMaximum(v1, v2)

(min.x..max.x).each { xx ->
	(min.z..max.z).each { zz ->
		(min.y..max.y).each { yy ->
			callback(w[v(xx,yy,zz)])
		}
	}
}
