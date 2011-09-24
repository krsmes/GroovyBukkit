import org.bukkit.block.Block
import org.bukkit.material.Directional
import org.bukkit.block.BlockFace

def (depth, height, width, filename) = args.toList()

def dRange = (depth < 0) ? (-2..depth - 1) : (0..depth - 1)
def hRange = (height instanceof Range) ? height : (height < 0) ? (0..(height + 1)) : (1..height)
def wRange = (width instanceof Range) ? (width.from - 1..width.to - 1) : (width < 0) ? (0..width + 1) : (0..width - 1)

def dataValueChar = ' abcdefghijklmno'
def dataValueDirChar = { facing ->
    facing == fRgt ? '>' : facing == fLft ? '<' : facing == fBck ? 'v' : facing == fac ? '^' : facing == BlockFace.DOWN ? '!' : ' '
}
def encodeBlock = { Block block ->
    if (block.typeId) {
        def dataChar = (block.state.data instanceof Directional) ? dataValueDirChar(block.state.data.facing) : dataValueChar[block.data]
        String.format('%02X%s', block.typeId, dataChar)
    }
    else '   '
}


def layers = []
hRange.each { h ->
    def hBlk = at + h
    def rows = []
    dRange.each { d ->
        def dBlk = hBlk + (fac * d)
        def row = new StringBuilder(256)
        wRange.each { w ->
            def wBlk = dBlk + (fRgt * w)
            row << encodeBlock(wBlk)
        }
        rows << row
    }
    layers << rows.reverse()
}

me.sendMessage "${hRange.from}-${hRange.to}"
if (hRange.from < 0) { layers = layers.reverse() }

StringBuilder result = new StringBuilder(4096)
layers.each {
    result << '---\n'
    result << it.join('\n')
    result << '\n'
}

new File(g.plugin.tempFolder, filename).text = result.toString()

"Copied ${layers.size()} layers"
