GroovyBukkit
============

A CraftBukkit Plugin for running scripts written in [Groovy](http://groovy.codehaus.org/).

Compilation
-----------

Maven to handle dependencies.

* Install [Maven](http://maven.apache.org/download.html)
* Check out this repository

	mvn clean package

Note: at the moment this plugin uses a fork of Bukkit to allow unregistering of listeners.  For it
to work properly Bukkit should be built from the 'all' branch of https://github.com/krsmes/Bukkit


Installation
------------
* Copy the .jar file from target/ to your craftserver folder's plugins/ directory
* Create a scripts/ directory peer to the plugins/ directory (optional)
* Download a copy of Groovy and copy embedded/groovy-all-1.x.jar to your craftserver folder


Running
-------

Groovy will have to be added to the classpath for running craftbukkit/mineserver:

	java -Xmx1g -cp groovy-all-1.7.6.jar:craftbukkit.jar org.bukkit.craftbukkit.Main nogui

(Creating a start.sh script is recommended)

At start up an info message should be displayed:

	[INFO] Groovy Plugin 0.1.x enabled


Using
-----

Within the Minecraft client use the commands /g to execute a command or script

* /g - allows you to run a piece of inline groovy script:

	/g s.time

* To execute a script, just call that script as if it were a method

	/g morning()
	/g morning(2)

The scripts must in the scripts/ directory must have a .groovy extension.  Arguments passed
to the script are available in the variable 'args'

scripts/morning.groovy:
	// very simple example, resets time to morning
	def day = ((int) s.time / 24000) + (args ? args [0].toInteger (): 0)
	s.time = day * 24000
	"Morning of day $day"

The value of the last statement executed is send to the player as a chat message.


API
---

Being a dynamic language (with open access to all protected and private data) scripts
can easily be written to get to almost anywhere in the minecraft/craftbukkit code.
Some entry points have been made available to scripts:

	log -- a java.util.logging.Logger instance for 'Minecraft'
	data -- a map of data specific to the user (persists across commands until server restart)
	global -- a map of data global to all users (persists until server restart)
	g -- the GroovyBukkit script runner instance
	s -- the server (org.bukkit.Server)
	w -- the world (org.bukkit.World)
	p -- the current player (org.bukkit.player.Player)
	spawn -- the world's spawn location (org.bukkit.Location)

	l -- the current player's location (org.bukkit.Location)
	pitch -- the current players pitch (-90..90)
	yaw -- the current players yaw (normalized to 0..359)
	f -- the direction the current player is facing (org.bukkit.block.BlockFace)
	v -- the current player's vector (org.bukkit.util.Vector)
	x, y, z -- the current player integer/block location

	b -- the block under the player
	bFwd -- the block forward of b (in the direction the player is facing)
	highY -- the y of the highest block for the players x/z location
	bY -- a list of vertical blocks at the players location (0..128)

And some custom methods are available for use:

	register(uniqueName, closureMap) -- register a map of event handler closures
	register(methodName, closure) -- register a single event handler closure
	unregister(uniqueName/methodName)  -- unregister named event handler(s)

	loc(x, y, z) -- create a Location instance with int or double values
	loc(x, z) -- create a Location instance using the highest Y at the given x/z

	to(loc) -- teleport to the given location
	facing(loc) -- use the 'yaw' of the location to return a BlockFace instance

	m(value) -- return a Material instance from the given value, value can be Material, number, or string

	stack(item) -- create an ItemStack, item can be a Material, number, or string
	stack(item, qty) -- create an ItemStack of the given quantity

	inv(pos, item, qty=1) -- set player's inventory pos to item, default quantity is 1
	give(item, qty=1) -- give player item, default quantity is 1
	give(player, item, qty=1) -- give given player item, default quantity is 1


*Special Data*
--------------

If you add a 'scripts' entry to the user 'data' map it will be used as the
location to find scripts to run (if it is not set, scripts are assumed to be
in the scripts/ directory from under where the server was started).

	/g data.scripts='/path/to/my/scripts'

The really special part of this is the ability to specify an http:// location:

	/g data.scripts='http://192.168.1.99/~path/minecraft/'

This will cause /gg commands for the player to retrieve remote scripts:

	/g morning()

Would look for http://192.168.1.99/~path/minecraft/morning.groovy and execute it.


Example
-------
If you are below ground and want to teleport yourself above ground:
	/g p.teleportTo(g.loc(w,x,highY,z))

List the types of all the blocks below you:
	/g bY[y..0].type

Send you a message whenever a block ignites:
	/g g.register 'onBlockIgnite' { p.sendMessage "${it.cause} lit ${it.block}" }

Cancel entities from exploding:
	/g g.register 'onEntityExplode' {it.cancelled=true}

Figure out what kind of biome you are in:
	/g b.biome


Notes
-----
* /g commands auto import most of the org.bukkit subpackages


To Do
-----
Add permissions/security.
