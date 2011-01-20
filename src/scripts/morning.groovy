// very simple example, resets time to morning
def day = ((int) s.time / 24000) + (args ? args [0].toInteger (): 0)
s.time = day * 24000
"Morning of day $day"
