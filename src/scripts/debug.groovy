// presents all available scripting variables
log.info("global=$global")  // map available to all scripts/players
log.info("data=$data")  // map unique per player
log.info("pl=$pl")  // map of all online players
log.info("w=$w")  // current world
log.info("loc=$loc")  // current location
log.info("pitch=$pitch")  // current pitch (-90..90)
log.info("yaw=$yaw")  // normalized yaw (0..359)
log.info("fac=$fac")  // BlockFacing direction based on yaw
log.info("vec=$vec")  // vector of block player is standing on
log.info("x=$x")  // blockX (int)
log.info("y=$y")  // blockY (int)
log.info("z=$z")  // blockZ (int)
log.info("blk=$blk")  // block player is standing on
