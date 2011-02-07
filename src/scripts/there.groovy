def head = v(here.x, here.y+1.6, here.z)
def yawR = Math.toRadians(-yaw)
def pitchR = Math.toRadians(-pitch)
def yawSin = Math.sin(yawR)
def yawCos = Math.cos(yawR)
def pitchSin = Math.sin(pitchR)
def pitchCos = Math.cos(pitchR)

def relx = (yawSin * pitchCos)
def rely = pitchSin
def relz = (yawCos * pitchCos)

def lat = null
def cntr = 0.0
def dist = 50.0
def incr = 0.02

def count = 0
while (!lat && cntr < dist) {
	count++
	cntr += incr
	lv = v(cntr * relx, cntr * rely, cntr * relz) + head
	def b = w[lv]
	//msg "x:${blk.x}, y:${blk.y}, z:${blk.z}, t:${blk.type}"
	if (b.typeId != 0 && b.typeId != 78) lat = b
	incr += incr * 0.04
}

[lat.toString(), count]