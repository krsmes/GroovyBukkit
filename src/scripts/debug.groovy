// presents all available scripting variables
log.info("global=$global")  // map available to all scripts/players
log.info("data=$data")  // map unique per player
log.info("op=$op")  // map of all online players
log.info("w=$w")  // current world
log.info("l=$l")  // current location
log.info("pitch=$pitch")  // current pitch (-90..90)
log.info("yaw=$yaw")  // normalized yaw (0..359)
log.info("facing=$facing")  // BlockFacing direction based on yaw
log.info("v=$v")  // vector of block player is standing on
log.info("x=$x")  // blockX (int)
log.info("y=$y")  // blockY (int)
log.info("z=$z")  // blockZ (int)
log.info("b=$b")  // block player is standing on
log.info("highy=$highy")  // highest y for the x/z of player's location
log.info("yb=...")  // list (0..128) of the entire column of block for the player's location
