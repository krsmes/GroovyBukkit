import org.bukkit.material.Directional

def materials = [
        ' ': 'air',
        '*': 'snow',
        '&': 'web',
        '@': 'wool',
        '#': 'ladder',
        '/': 'torch',
        '|': 'fence',
        '+': 'redstone wire',
        '-': 'step',
        '=': 'double step',
        '!': 'mob spawner',

        'c': 'cobblestone',
        'C': 'cobblestone stairs',
        'd': 'dirt',
        'g': 'grass',
        'G': 'glass',
        'i': 'ice',
        'l': 'log',
        'L': 'leaves',
        'm': 'mossy cobblestone',
        'n': 'sand',
        'N': 'sandstone',
        'o': 'obsidian',
        's': 'stone',
        'S': 'soil',
        'T': 'tnt',
        'w': 'wood',
        'W': 'wood stairs',
        'x': 'bedrock',
        'y': 'clay'

]

def plan = load(args[0])

def determineMaterialData = { mat, dataStr ->
	if (!dataStr || dataStr == ' ') return 0
	if (dataStr in ['0'..'9','a'..'f','A'..'F']) return Integer.valueOf(dataStr, 16)
	if (dataStr in ['^','v','<','>']) {
		def matData = mat.getNewData((byte)0)
		if (matData instanceof Directional) {
			matData.facingDirection = dataStr == 'v' ? fBck : dataStr == '<' ? fLft : dataStr == '>' ? fRgt : fFwd
			return matData.data
		} 
	}
	0
}

def layers = plan.split('--')
def layernum = 0

layers.each { layer ->
    def rows = layer.split('\n').toList()
    if (rows) {
        def layerRepeat = Integer.valueOf(rows.remove(0)?:1)

        (1..layerRepeat).each {
            def start = at + layernum++

            rows.reverse().eachWithIndex { row, rownum ->
                def rowlen = row.length()

                if (rowlen > 0) {
                    def cur = start + (fac * rownum)
                    int cols = Math.ceil(rowlen / 2)

                    (0..cols-1).each { colnum ->
                    	def idx = colnum * 2
                        def mat = materials[row[idx]]
                        if (mat) {
                        	mat = m(mat)
                        	def matdata = 0
                        	if (rowlen > idx+1) {
                        		matdata = determineMaterialData(mat, row[idx+1])	
                        	}	
                        	cur.setTypeIdAndData(mat.id, (byte)matdata, false)
                        }
                        cur += fRgt
                    }
                }
            }
        }
    }
}
