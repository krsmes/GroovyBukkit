msg 'args='+args
def (length, height, width, material, material_data) = args.toList()

msg 'args='+[length,height,width,material,material_data]
// check parameters
if (!length) return
if (!height) height = 1
if (!width) width = 1
if (material==null) material = p.itemInHand.type.block ? p.itemInHand : p  // use block in hand or block standing on
if (material_data==null) material_data = material instanceof Integer ? 0 : material
msg 'args=' + [length, height, width, material, material_data]

// calculate ranges
def hrange = (height instanceof Range) ? height : (height < 0) ? (0..(height+1)) : (1..height)
def wrange = (width instanceof Range) ? (width.from-1..width.to-1) : (width < 0) ? (-2..width-1) : (-1..width-2)
def lrange = (length < 0) ? (-2..length-1) : (0..length-1)

// identify material and material data
def mat = m(material)
def matdata = mdata(material_data)
msg 'matdata='+matdata

hrange.each { h ->
	def b1 = b + h
	wrange.each { w ->
		def b2 = b1 + (fR + w)
		lrange.each { l ->
			def b3 = b2 + (f + l)
			b3.type = mat
			if (matdata) b3.data = (byte)matdata
		}
	}
}
