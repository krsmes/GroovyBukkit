import org.bukkit.block.*
import org.bukkit.material.*
import org.bukkit.Material

def planName = args ? args[0] : me.name

def plan = new File(g.plugin.tempFolder, planName).text
if (!plan) return "Unable to load ${planName}"

def heightOfs = args.length > 1 ? args[1].toInteger() : 0

def determineMaterial = { code ->
    if (code == '  ') return Material.AIR
    def result = null
    if (code.matches('[0-9A-F][0-9A-F]')) result = Integer.parseInt(code, 16)
    result ? m(result) : null
}

def determineFace = { code -> code == 'v' ? fBck : code == '<' ? fLft : code == '>' ? fRgt : code == '^' ? fac : code == '!' ? BlockFace.DOWN : BlockFace.UP }

def dataValueChar = ' abcdefghijklmno'

def determineMaterialData = { mat, code ->
    if (code.matches('[a-o]')) return dataValueChar.indexOf(code)
    if (code in ['^', 'v', '<', '>', '!', ' ']) {
        if (mat == Material.LEVER) {
            def facing = determineFace(code)
            println facing
            def result = facing == BlockFace.SOUTH ? 0x1 : facing == BlockFace.NORTH ? 0x2 : facing == BlockFace.WEST ? 0x3 : facing == BlockFace.EAST ? 0x4 : 0x5
            println result
            return result
        }
        def matData = mat.getNewData((byte) 1)
        if (matData instanceof Directional) {
            matData.facingDirection = determineFace(code)
            return matData.data
        }
        if (matData instanceof Door) {
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
def postProcessMaterials = [Material.TORCH, Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON, Material.PISTON_BASE, Material.PISTON_STICKY_BASE, Material.LEVER]
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
                            cur.setTypeIdAndData(mat.id, (byte) matdata, false)
                        }
                        cur += fRgt
                    }
                }
            }
        }
    }
}

// redo these to make sure they attached properly
future(10) {
    postProcess.each { blk, mat, matdata ->
        blk.setTypeIdAndData(mat.id, (byte) matdata, true)
        if (mat == Material.REDSTONE_TORCH_ON) (blk+1).state.update()
    }
}


"Pasted ${layers.size()-1} layers"