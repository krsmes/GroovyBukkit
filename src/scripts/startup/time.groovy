command 'sunrise', { runner, args ->
    runner.world.time = 22000
    "Sunrise of day ${Math.round(w.fullTime / 24000)}"
}

command 'morning', { runner, args ->
    runner.world.time = 0
    "Morning of day ${Math.round(w.fullTime / 24000)}"
}

command 'noon', { runner, args ->
    runner.world.time = 6000
    "Noon of day ${Math.round(w.fullTime / 24000)}"
}

command 'sunset', { runner, args ->
    runner.world.time = 12000
    "Dusk of day ${Math.round(w.fullTime / 24000)}"
}

command 'night', { runner, args ->
    runner.world.time = 14000
    "Night of day ${Math.round(w.fullTime / 24000)}"
}

