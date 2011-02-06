// sample registration of an event listener... very simple, no Type or Listener class to worry about
// the 'onmove' name is an arbitrary unique value that allows this script to be rerun without
// adding duplicate listeners (previous listeners are removed)

register('onmove', [
	'player move': { m ->
		m.player.sendMessage "${m.player.name} moved to ${m.to}"
	}
])
