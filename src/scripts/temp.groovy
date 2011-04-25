def depth = args[0].toInteger()
def width = args[1].toInteger()
def opploc = blk + (fac*depth) + (fRgt*width)
def opptop = l(opploc.x, opploc.z)
def curtop = l(blk.x, blk.z)
future { me.teleport(here+fac+fRgt) }
future {
    w[curtop].type = m('fence')
    w[opptop].type = m('fence')
}
