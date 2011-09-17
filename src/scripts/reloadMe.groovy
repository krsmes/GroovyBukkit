def name = args ? args[0] : me.name

me.displayName = name
me.handle.name = name

def hndl = me.handle
s.handle.b(hndl)
s.handle.moveToWorld(hndl, 0)
g.plugin.initializePlayer(me)
me.name
