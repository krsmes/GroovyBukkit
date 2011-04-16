

command 'copy', { runner, args ->
    def data = runner.data
    def point1 = data.stickClicks[0]
    def point2 = data.stickClicks[1]
    def world = point1.world

    def minY = Math.min(point1.blockY, point2.blockY)
    def minX = Math.min(point1.blockX, point2.blockX)
    def maxX = Math.max(point1.blockX, point2.blockX)
    def minZ = Math.min(point1.blockZ, point2.blockZ)
    def maxZ = Math.max(point1.blockZ, point2.blockZ)

    def height = args[0].toInteger()
    def maxY = minY + (height - 1)
    def width = maxX - minX + 1
    def depth = maxZ - minZ + 1

    def section = []
    section << [height, width, depth]

    (minY..maxY).each { curY ->
        (minX..maxX).each { curX ->
            (minZ..maxZ).each { curZ ->
                def curBlock = world.getBlockAt(curX, curY, curZ)
                section << [curBlock.typeId, curBlock.data]
            }
        }
    }
    data.section = section
}


command 'showsection', { runner, args ->
    def data = runner.data
    def section = data.section
    if (section) {
        def (height, width, depth) = section[0]
        def area = width * depth
        (1..height).each { curY ->
            (1..width).each { curX ->
                (1..depth).each { curZ ->
                    def idx = ((curY - 1) * area) + ((curX - 1) * depth) + curZ
                    println "$idx ($curX,$curY,$curZ): ${section[idx]}"
                }
            }
        }
    }
}

command 'paste', { runner, args ->
    def player = runner.player
    def loc = player.location
    def start = lookingat(player)
    def dir = f(loc)
    def dirR = f(loc.yaw + 90)

    def data = runner.data
    def section = data.section
    if (section) {
        def (height, width, depth) = section[0]
        def area = width * depth
        (1..height).each { curY ->
            def b1 = start + (curY - 1)
            (1..width).each { curX ->
                def b2 = b1 + (dirR + (curX - 1))
                (1..depth).each { curZ ->
                    def b3 = b2 + (dir + (curZ - 1))
                    def idx = ((curY - 1) * area) + ((curX - 1) * depth) + curZ
                    def typeData = section[idx]
                    b3.typeId = typeData[0]
                    b3.data = typeData[1]
                }
            }
        }
    }
}
