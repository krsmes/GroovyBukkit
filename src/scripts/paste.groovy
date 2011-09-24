import org.bukkit.block.*
import org.bukkit.material.*
import org.bukkit.Material


//def plan = load(args[0])

def plan = new File(args[0]).text
if (!plan) return "Unable to load ${args[0]}"

def heightOfs = args.length > 1 ? args[1].toInteger() : 0

def determineMaterial = { code ->
    if (code == '  ') return Material.AIR
    def result = null
    if (code.matches('[0-9A-F][0-9A-F]')) result = Integer.parseInt(code, 16)
    result ? m(result) : null
}

def dataValueChar = ' abcdefghijklmno'
def determineMaterialData = { mat, code ->
    if (!code || code == ' ') return 0
    if (code.matches('[a-o]')) return dataValueChar.indexOf(code)
    if (code in ['^', 'v', '<', '>']) {
        def matData = mat.getNewData((byte) 0)
        if (matData instanceof Directional) {
            matData.facingDirection = code == 'v' ? fBck : code == '<' ? fLft : code == '>' ? fRgt : fac
            return matData.data
        }
        else if (matData instanceof Door) {
            def corner = 0
            if (fac == BlockFace.WEST) corner = code == 'v' ? 0 : code == '<' ? 1 : code == '>' ? 3 : 2
            else if (fac == BlockFace.EAST) corner = code == 'v' ? 2 : code == '<' ? 3 : code == '>' ? 1 : 0
            else if (fac == BlockFace.NORTH) corner = code == 'v' ? 1 : code == '<' ? 0 : code == '>' ? 2 : 3
            else if (fac == BlockFace.SOUTH) corner = code == 'v' ? 3 : code == '<' ? 2 : code == '>' ? 0 : 1
            return corner
        }
    }
    0
}

def postProcess = []
def postProcessMaterials = [Material.TORCH, Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON]
def layers = plan.split('---')
def layernum = 0
def start = at + heightOfs

layers.each { layer ->
    def rows = layer.split('\n').toList()
    if (rows) {
        def layerRepeat = Integer.valueOf(rows.remove(0) ?: 1)

        (1..layerRepeat).each {
            def layerBlk = start + layernum++

            rows.reverse().eachWithIndex { row, rownum ->
                def rowlen = row.length()

                if (rowlen > 0) {
                    def cur = layerBlk + (fac * rownum)
                    int cols = Math.ceil(rowlen / 3)

                    (0..cols - 1).each { colnum ->
                        def idx = colnum * 3
                        def mat = determineMaterial(row[idx..idx+1])
                        if (mat != null) {
                            def matdata = 0
                            if (rowlen > idx + 1) {
                                matdata = determineMaterialData(mat, row[idx + 2])
                            }
                            if ((mat == Material.WOODEN_DOOR || mat == Material.IRON_DOOR_BLOCK) && ((cur - 1).type == mat)) {
                                matdata |= 0x8
                            }
                            if (mat in postProcessMaterials) {
                                postProcess << [cur, mat, matdata]
                            }
                            else {
                                cur.setTypeIdAndData(mat.id, (byte) matdata, false)
                            }
                        }
                        cur += fRgt
                    }
                }
            }
        }
    }
}

future(50) {
    postProcess.each { blkMatData ->
        blkMatData[0].setTypeIdAndData(blkMatData[1].id, (byte) blkMatData[2], false)
        blkMatData[0].state.update()
    }
}


"Pasted ${layers.size()-1} layers"