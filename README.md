GroovyBukkit
============

A CraftBukkit Plugin for running scripts written in [Groovy](http://groovy.codehaus.org/).

Because of the dynamic nature of groovy, and the simplicity of adding commands and event listeners with
this API, the possibilities with this plugin are endless.

See also:

* [Google Doc](https://docs.google.com/document/pub?id=1JuFLSs1jxdaoFxYE7XVoraIR_wDvfSNqtBpXbLn5gPM)
* [Bukkit Forum](http://forums.bukkit.org/threads/dev-admn-edit-gen-misc-chat-tp-groovybukkit-v0-6-5-2-groovy-scripting-module-1150.1267/)


Compilation
-----------

Maven to handle dependencies.

* Install [Maven](http://maven.apache.org/download.html)
* Check out this repository

	mvn clean package

(Note: this plugin no longer requires a forked build of CraftBukkit)


Installation
------------
* Copy the .jar file from target/ to your craftbukkit folder's plugins/ directory
* Create a scripts/ directory under plugins/GroovyBukkit
* Download a copy of Groovy and copy embedded/groovy-all-1.8.2.jar to your craftbukkit folder

Additionally, to see what is possible, it is recommended you copy all the scripts from src/scripts to your
scripts/ directory, including those under scripts/startup to scripts/startup under your bukkit server.

Scripts in scripts/startup/ run automatically at server startup (and reload).

A properly configured setup will look something like:

    craftbukkit.jar
    groovy-all-1.8.4.jar
    plugins/
        GroovyBukkit.jar
        GroovyBukkit/
            config.yml
            scripts/
                fill.groovy
                stairsup.groovy
                ...
                startup/
                    time_changer.groovy
                    players.groovy
                    plot_commands.groovy
                    power_tools.groovy
                    ...


Running
-------

Groovy will have to be added to the classpath for running craftbukkit/mineserver:

    Mac/Linux:
	java -Xmx1g -cp groovy-all-1.8.4.jar:craftbukkit.jar org.bukkit.craftbukkit.Main nogui
    Windows:
	java -Xmx1g -cp groovy-all-1.8.4.jar;craftbukkit.jar org.bukkit.craftbukkit.Main nogui

(Creating a start.sh/start.bat script is recommended)

At start up an info message should be displayed:

    [INFO]
    [INFO] Registered 'GroovyPlugin' with X listener(s): [...]
	[INFO] Groovy Plugin 0.7.x enabled


Using
-----

Within the Minecraft client use the commands /g to execute a command or script

* /g - allows you to run a piece of inline groovy script:

	/g w.time

* To execute a script, just call that script as if it were a method

	/g morning()
	/g morning(2)

The scripts must in the scripts/ directory must have a .groovy extension.  Arguments passed
to the script are available in the variable 'args'

scripts/morning.groovy:
	// very simple example, resets time to morning
	def day = ((int) s.time / 24000) + (args ? args [0].toInteger (): 0)
	w.fullTime = day * 24000
	"Morning of day $day"

The value of the last statement executed (the result) is send to the player as a chat message.

Scripts in scripts/startup/ are executed when the server is started.  If these scripts return a
map [:] as a result it is assumed to be an event listener map (see 'listen(...)' below) and those
event listeners will be registered.


Permissions
-----------

Permission must be granted to use the /g command.  For the first user this must be done from the
server console (ops aren't even granted by default).  From the console type:
    g permit('playername', 'g')

You'll probably want to grant that use permission to use the 'permit' command too:
    g permit('playername', 'permit')

This way playername can grant permissions from within minecraft:
    /permit anotherplayer g

Once you start using permissions and creating additional commands, you can easily assign either
all players to a permission:
    /permit * command
or all permissions to a player:
    /permit someplayer *


API
---

Being a dynamic language (with open access to all protected and private data) scripts
can easily be written to get to almost anywhere in the minecraft/craftbukkit code.
Some entry points have been made available to scripts:

	g -- () the GroovyBukkit script runner instance
    s -- (org.bukkit.Server) current server instance
    global -- (Map [:]) of global data
    data -- (Map [:]) of player-specific data
    w -- (org.bukkit.World) current world instance
    spawn -- (org.bukkit.Location) world.spawnLocation
    pl -- (Map) online players

	me -- (org.bukkit.player.Player) the current player
	inv -- (org.bukkit.inventory.PlayerInventory) the player's inventory
    here -- (org.bukkit.Location) the current player's location
    at -- (org.bukkit.block.Block) the block the player is looking at
	x, y, z -- the current player integer/block location

	fac -- (org.bukkit.block.BlockFace) the direction the current player is facing
    fRgt, fLft, fBck -- (org.bukkit.block.BlockFace) player's right, left, and back directions

	blk -- (org.bukkit.block.Block) the block under the player
	bFwd, bRgt, bLft, bBck -- the block forward, right, left, or back of the player

And some custom methods are available for use:

    Location l(x,z) -- return the location using highest block y
    Location l(x,y,z)
    Location l(x,y,z,yaw,pitch)
    Location l(obj) -- return the location of the given object (entity, location, vector, etc.)
    [] e() -- return a list of all entities
    [] e(name) -- return a list of all entities with the given name ('Pig', 'Spider', etc)
    msg(player, text)
    msg([player1, ...], text)
    Player p(name) -- find player with 'name', note: 'name' does not have to be complete, it can be just the first few letters

    log(message)
    ItemStack i(material) -- create a 1 item stack
    ItemStack i(material, qty) -- create an item stack with the given quantity
    ItemStack i(material, data, qty)
    Material m(obj) -- return a Material from the given parameter (3, 'Stone', a block, a location of a block, etc)
    Byte md(obj) -- like m(obj) but return the data byte

    [] xyz(obj) -- return a list containing the x, y, and z of the location of the object
    Vector v(x,z)
    Vector v(x,y,z)
    Vector v(obj)

    BlockFace f(entity) -- get the direction (BlockFace) the entity is facing
    BlockFace f(location) -- get the direction (BlockFace) the location is facing
    BlockFace f(yaw) -- get the direction (BlockFace) from the given yaw

    Vector looking(location)
    Block lookingat(location)
    Block lookingat(location, distance)
    Block lookingat(location, distance, precision)

    dist(from, to) -- give the distance (in blocks) between two locations

    give(player, item)
    give(player, item, qty)

    make(name) -- create a named entity at spawn
    make(name, location) -- create a named entity at the given location
    make(name, location, qty)

    future(closure) -- execute the given closure on a separate thread

    listen(uniqueName, type, closure) -- call the given closure when event type occurs
    listen(uniqueName, [type:closure]) -- call the given closures when event types occurs
    unlisten(uniqueName) -- stop listening for events registered for uniqueName

    command(name, closure) -- create a new command /name and call the closure when it is used


MetaClass support (see groovy documentation about metaclasses):
    Block
        + BlockFace -- the block on the BlockFace of the current block
        + int -- adds int to this block's y location and returns that block
        + Vector -- adds vector's x,y,z to this block's location
        - BlockFace
        - int
        - Vector
        as Vector
        as Location

    BlockFace
        + int -- returns a Vector using the BlockFace.modX,modY,modZ multiplied by int
        - int
        as Vector

    Location
        + int -- adds int to location's Y
        + Vector
        as Vector

    Entity
        as Vector

    World
        getAt(pos) -- return a Block at the given pos (location, vector, entity, etc)
        putAt(pos, b) -- set the block at the given pos, b is an item, block, material, or location of a block

        this allows doing scripts like:
            /g w[someloc] = m('Stone')

    Inventory
        getAt -- for "inv[9]" support
        putAt -- for "inv[9]=i('diamond',16)" support
        <<
        >>
        as List



*Special Data*
--------------

If you add a 'scripts' entry to the user 'data' map it will be used as the
location to find scripts to run (if it is not set, scripts are assumed to be
in the scripts/ directory from under where the server was started).

	/g data.scripts='/path/to/my/scripts'

The really special part of this is the ability to specify an http:// location:

	/g data.scripts='http://192.168.1.99/~path/minecraft/'

This will cause /g commands for the player to retrieve remote scripts:

	/g morning()

Would look for http://192.168.1.99/~path/minecraft/morning.groovy and execute it.


Example
-------
If you are below ground and want to teleport yourself above ground:

	/g me.teleport l(x,z)

Send you a message whenever a block ignites:

	/g listen 'msgignite', 'block ignite' { p.sendMessage "${it.cause} lit ${it.block}" }

Cancel entities from exploding:

	/g listen 'preventexplode', 'entity explode' {it.cancelled=true}

Figure out what kind of biome you are in:

	/g blk.biome

Change the type of block above the block you are looking at

    /g (at+1).type=m('fence')


Scripts in startup/
-------------------
There are a set of default scripts in scripts/startup.  They are not critical to using GroovyBukkit
generically, but they do provide additional functionality and are good examples to work from.

Note: in Groovy the last statement in a function (or in a script) is what gets returned (no need to
add the 'return' keyword).  If the 'return' result of a script in the startup directory is a Map,
GroovyBukkit will assume that it is a map of event listeners and register it (with the name of the script).

* give_command
    - implementation of the '/gg' command, see comments at the top of give.groovy
* permission_changer
    - implementation of the '/permit' command, see comments at the top of permissions.groovy
* plot_commands
    - a full plot creation and protection set of commands and listeners
* power_tools
    - a set of listeners to give special powers to certain items in your hand (with a /ptools command to turn them on and off)
* players
    - listeners and '/stats' command to keep track of who logs into your server (online time, logins, respawns, etc.)
* warp_commands
    - warp releated commands (supports both private and public warps), and listner for sign warps


REMEMBER! for commands (like /warp or /gg etc) you have to give users permission to use those commands:
    /permit soandso ptools
    /permit * warp-help warp warp-back warp-create warp-delete





Notes
-----
* /g commands auto import most of the org.bukkit subpackages
* 'global' gets persisted (and loaded) to/from scripts/startup/data.yml
* 'data' gets persisted to/from scripts/PlayerName/data.yml
* look in src/scripts for many other examples
    - attract.groovy : cause an entity (cow, pig, etc) to follow you
    - debug.groovy : '/g debug()' show available variables and their values
    - debuglisteners.groovy : '/g debuglisteners()' listen to all non-noisy bukkit events (to console log)
    - fill.groovy : '/g fill DEPTH, HEIGHT, WIDTH, MATERIAL' to fill an area
    - stairsup.groovy : /g stairsup()' create steps from current location up to air
    - wool.groovy : '/g inv << wool('brown', 5)' create itemstacks of colored wool

