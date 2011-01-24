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


Running
-------

Groovy will have to be added to the classpath for running craftbukkit/mineserver:

	java -Xmx1g -cp groovy-all-1.7.6.jar:craftbukkit.jar org.bukkit.craftbukkit.Main nogui

(Creating a start.sh script is recommended)

At start up an info message should be displayed:

	[INFO] Groovy Plugin 0.1.x enabled


Using
-----

Within the Minecraft client use the commands /g and /gg

* /g - allows you to run a piece of inline groovy script:

	/g s.time

* /gg - allows you to run script from the scripts/ directory:

	/gg morning

The scripts must in the scripts/ directory must have a .groovy extension.


API
---

Being a dynamic language (with open access to all protected and private data) scripts
can easily be written to get to almost anywhere in the minecraft/craftbukkit code.
Some entry points have been made available to scripts:

	p -- the current player (org.bukkit.player.Player)
	l -- the current player's location (org.bukkit.Location)
	v -- the current player's vector (org.bukkit.util.Vector)
	x, y, z -- the current player integer/block location
	f -- the direction the current player is facing (org.bukkit.block.BlockFace)
	s -- the server (org.bukkit.Server)
	w -- the world (org.bukkit.World)

	g -- the GroovyBukkit instance (the plugin)

	highY -- the y of the highest block for the players x/z location
	bY -- a list of vertical blocks at the players location (0..128)
	data -- a map of data specific to the user (persists across commands until server restart)
	global -- a map of data global to all users (persists until server restart)

And some methods are available via the plugin variable:

	register(uniqueName, closureMap) -- register a map of event handler closures
	register(methodName, closure) -- register a single event handler closure
	unregister(uniqueName/methodName)  -- unregister named event handler(s)

	loc(w, x, y, z) -- create a Location instance with int or double values
	loc(w, x, z) -- create a Location instance using the highest Y at the given x/z


*Special Data*
--------------

If you add a 'scripts' entry to the user 'data' map it will be used as the
location to find scripts to run (if it is not set, scripts are assumed to be
in the scripts/ directory from under where the server was started).

	/g data.scripts='/path/to/my/scripts'

The really special part of this is the ability to specify an http:// location:

	/g data.scripts='http://192.168.1.99/~path/minecraft/'

This will cause /gg commands for the player to retrieve remote scripts:

	/gg morning

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
* /gg commands that run scripts do not, the scripts will need import statements


To Do
-----
Add permissions/security.
