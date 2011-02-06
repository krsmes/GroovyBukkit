def (v1, v2, callback) = args.toList()

v1 = v(v1)
v2 = v(v2)

(v1.x..v2.x).each { xx ->
	(v1.z..v2.z).each { zz ->
		(v1.y..v2.y).each { yy ->
			callback(w[v(xx,yy,zz)])
		}
	}
}
