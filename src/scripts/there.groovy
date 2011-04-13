

def start

start = System.currentTimeMillis()
for (int i = 0; i < 1000; i++) me.getTargetBlock(null, 120)
res1 = System.currentTimeMillis() - start

start = System.currentTimeMillis()
for (int i = 0; i < 1000; i++) lookingat(me, 120.0, 0.01)
res2 = System.currentTimeMillis() - start

start = System.currentTimeMillis()
for (int i = 0; i < 1000; i++) util.lookingAtBlock(me, 120.0, 0.01)
res3 = System.currentTimeMillis() - start


me.sendMessage "res1=$res1 : ${me.getTargetBlock(null, 120)}"
me.sendMessage "res2=$res2 : ${lookingat(me, 120.0, 0.01)}"
me.sendMessage "res3=$res3 : ${util.lookingAtBlock(me, 120.0, 0.01)}"
