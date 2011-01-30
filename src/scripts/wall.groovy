def (length, height, material) = args

if (!height || !length) return
def hrange = (height instanceof Range) ? height : (height < 0) ? (0..(height+1)) : (1..height)
def lrange = (1..length)
def mat = m(material)

hrange.each { h ->
	def blk = b + f + h
	lrange.each { blk.type = mat; blk += f }
}
