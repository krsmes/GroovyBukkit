import org.bukkit.material.Directional
import org.bukkit.Material

me.sendMessage("krsmes's version")
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

        'b': 'brick',
        'B': 'bed block',
        'c': 'cobblestone',
        'C': 'cobblestone stairs',
        'd': 'dirt',
        'f': 'fire',
        'g': 'grass',
        'G': 'glass',
        'i': 'ice',
        'j': 'jack o lantern',
        'l': 'log',
        'L': 'leaves',
        'm': 'mossy cobblestone',
        'n': 'sand',
        'N': 'sandstone',
        'o': 'obsidian',
        'p': 'pumpkin',
        's': 'stone',
        'S': 'soil',
        'T': 'tnt',
        'w': 'wood',
        'W': 'wood stairs',
        'x': 'bedrock',
        'y': 'clay',
        'z': 'soul sand',

        '[': 'wooden door',
        ']': 'iron door block',
        '{': 'chest',
        '}': 'workbench'

]

def plan = load(args[0])

def determineMaterial = {
    def result = materials[it]
    result ? m(result) : null
}

def determineMaterialData = { mat, dataStr ->
	if (!dataStr || dataStr == ' ') return 0
	if (dataStr.matches('[a-fA-F0-9]')) return Integer.valueOf(dataStr, 16)
	if (dataStr in ['^','v','<','>']) {
		def matData = mat.getNewData((byte)0)
		if (matData instanceof Directional) {
			matData.facingDirection = dataStr == 'v' ? fBck : dataStr == '<' ? fLft : dataStr == '>' ? fRgt : fac
			return matData.data
		} 
	}
	0
}

def postProcess = []
def postProcessMaterials = [Material.TORCH, Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON]
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
                        def mat = determineMaterial(row[idx])
                        if (mat) {
                        	def matdata = 0
                        	if (rowlen > idx+1) {
                        		matdata = determineMaterialData(mat, row[idx+1])	
                        	}
                            if (mat in postProcessMaterials) {
                                postProcess << [cur, mat, matdata]
                            }
                        	cur.setTypeIdAndData(mat.id, (byte) matdata, false)
                        }
                        cur += fRgt
                    }
                }
            }
        }
    }
}

postProcess.each { blkMatData ->
    println "postProcess $blkMatData"
    blkMatData[0].state.with {
        type = blkMatData[1]
        if (blkMatData[2]) data = type.getNewData((byte) blkMatData[2])
        update()
    }
}
