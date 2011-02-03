import org.bukkit.util.Vector

msg "on x: ${b.x}, y:${b.y}, z:${b.z}, t:${b.type}"

def head = vec(l.x, l.y+1.5, l.z)
def yawR = Math.toRadians(-yaw)
def pitchR = Math.toRadians(-pitch)
def yawSin = Math.sin(yawR)
def yawCos = Math.cos(yawR)
def pitchSin = Math.sin(pitchR)
def pitchCos = Math.cos(pitchR)

def relx = (yawSin * pitchCos)
def rely = pitchSin
def relz = (yawCos * pitchCos)

def lat
def cntr = 0.0
def incr = 0.01

while (!lat && cntr < 100) {
	cntr += incr
	lv = vec(cntr * relx, cntr * rely, cntr * relz) + head
	def blk = w[lv]
	//msg "x:${blk.x}, y:${blk.y}, z:${blk.z}, t:${blk.type}"
	if (blk.typeId > 0) lat = blk
}


lat