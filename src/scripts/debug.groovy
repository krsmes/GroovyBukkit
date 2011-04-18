// presents all available scripting variables
[
"g=$g",  // groovy runner
"s=$s",  // current server
"w=$w",  // current world
"spawn=$spawn",  // current world spawn location
"pl=${pl.keySet()}",  // map of all online players
"me=$me",  // current player
"here=$here",  // current location
"at=$at", // target block
"x,y,z=$x,$y,$z", // integer xyz location
"fac=$fac",  // BlockFacing direction based on yaw
"blk=$blk",  // block player is standing on
].each { me.sendMessage(it) }