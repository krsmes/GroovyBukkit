import org.bukkit.block.*
import org.bukkit.material.*
import org.bukkit.Material

def materials = [
        ' ': 'air',
        '*': 'snow',

        '#': 'ladder',
        '|': 'fence',

        '/': 'torch',
        '\\': 'redstone torch off',
        '+': 'redstone wire',

        '-': 'step',
        '=': 'double step',
        '!': 'mob spawner',

        '[': 'wooden door',
        ']': 'iron door block',
        '{': 'chest',
        '}': 'workbench',
        '(': 'dispenser',
        ')': 'furnace',

        '$': 'yellow flower',
        '%': 'red rose',
        ':': 'brown mushroom',
        ';': 'red mushroom',
        '"': 'crops',

        '@': 'wool',
        '&': 'web',

        'a': 'lava',
        'A': 'stationary lava',
        'b': 'brick',
        'B': 'bed block',
        'c': 'cobblestone',
        'C': 'cobblestone stairs',
        'd': 'dirt',
        'D': 'diode block off',
        'e': 'water',
        'E': 'stationary water',
        'f': 'soil',
        'F': 'Fire',
        'g': 'grass',
        'G': 'glass',
        'h': 'stone button',
        'H': 'lever',
        'i': 'snow block',
        'I': 'ice',
        'j': 'jack o lantern',
        'J': 'jukebox',
        'k': 'bookshelf',
        'K': 'cake block',
        'l': 'leaves',
        'L': 'log',
        'm': 'mossy cobblestone',
        'M': 'sugar cane block',
        'n': 'sand',
        'N': 'sandstone',
        'o': 'obsidian',
        'O': 'note block',
        'p': 'pumpkin',
        'P': 'portal',
        'q': 'lapis ore',
        'Q': 'bedrock',
        'r': 'redstone ore',
        'R': 'netherrack',
        's': 'stone',
        'S': 'soil',
        't': 'sapling',
        'T': 'tnt',
        'u': 'stone plate',
        'U': 'wood plate',
        'v': 'gravel',
        'V': 'gold ore',
        'w': 'wood',
        'W': 'wood stairs',
        'x': 'coal ore',
        'X': 'iron ore',
        'y': 'clay',
        'Y': 'diamond ore',
        'z': 'soul sand',
        'Z': 'glowstone'
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
        else if (matData instanceof Door) {
            def corner= 0
            if (fac == BlockFace.WEST) corner = dataStr == 'v' ? 0 : dataStr == '<' ? 1 : dataStr == '>' ? 3 : 2
            else if (fac == BlockFace.EAST) corner = dataStr == 'v' ? 2 : dataStr == '<' ? 3 : dataStr == '>' ? 1 : 0
            else if (fac == BlockFace.NORTH) corner = dataStr == 'v' ? 1 : dataStr == '<' ? 0 : dataStr == '>' ? 2 : 3
            else if (fac == BlockFace.SOUTH) corner = dataStr == 'v' ? 3 : dataStr == '<' ? 2 : dataStr == '>' ? 0 : 1
            return corner
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
                            if ((mat == Material.WOODEN_DOOR || mat == Material.IRON_DOOR_BLOCK) && ((cur-1).type == mat)) {
                                matdata = matdata | 0x8
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
