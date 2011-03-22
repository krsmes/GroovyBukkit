package net.krsmes.bukkit.groovy

import org.bukkit.event.Event
import org.bukkit.event.Event.Priority
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.yaml.snakeyaml.Yaml
import org.bukkit.entity.Player


class GroovyRunner extends GroovyAPI implements EventExecutor {

	static SCRIPT_PREFIX = """
import org.bukkit.*;import org.bukkit.block.*;import org.bukkit.entity.*;import org.bukkit.inventory.*;import org.bukkit.material.*;import org.bukkit.event.*;import org.bukkit.util.*;
"""

    GroovyPlugin plugin
    GroovyShell shell
    Map vars

    def global
	def data = [:]
	def listeners = [:]
    def registeredTypes = []  // seeing we can't unregister listeners, this list keeps track of what types we've registered
    Player player = null


	GroovyRunner(GroovyPlugin plugin, def data) {
		super(plugin.server)
		this.plugin = plugin
        this.global = plugin.runner?.data ?: data
		this.data = data
		_initShell(data)
	}


	def _init() {
		def dir = new File(_initScriptsLoc)
		if (!dir.exists()) { dir.mkdirs() }
		// load data
		def datafile = new File(dir, 'data.yml')
		if (datafile.exists()) {
            log "Loading $datafile"
            def yamldata = loadyaml(datafile.text)
            if (yamldata) data.putAll yamldata
		}
		// run scripts
		dir.eachFileMatch(groovy.io.FileType.FILES, ~/.*\.groovy/) { f ->
			log "Initializing ${f.name}"
			def result = run(f.text)
			if (result instanceof Map) {
				listen(f.name, result)
			}
		}
		this
	}

    void _shutdown() {
        def listenerNames = listeners.keySet() as List
        listenerNames.each { unlisten(it) }
        _save()
    }

    void _save() {
        // save data
        def datafile = new File(_initScriptsLoc, 'data.yml')
        if (!datafile.exists()) datafile.createNewFile()
        log "Storing $datafile"
        try {
            def savableData = [:]
            data.each{ k, v -> if (k!='last' && k!='temp' && v instanceof Serializable) savableData.put(k,v) }
            datafile.text = dumpyaml(savableData)
        }
        catch (e) {
            log "Unable to store: $data\n$e.message"
        }
    }


    def get_initScriptsLoc() {
		scriptLoc + 'startup/'
	}


	def runScript = { def script ->
		run SCRIPT_PREFIX + script, null
	}


	def runFile = { scriptName, args ->
		def script = load(scriptName + GroovyPlugin.SCRIPT_SUFFIX)
		if (script) {
			run script, args
		}
	}


	def run = { script, args=[] ->
		def result = null
		if (script) {
			try {
				_updateVars()
				def savedArgs = vars.args
				vars.args = args

				result = _parse(script).run()

				vars.args = savedArgs
			}
			catch (e) {
				result = e.message
				e.printStackTrace()
			}
		}
		result
	}

	def _parse = { script ->
		def gscript = shell.parse(script)
        _log.fine("parse: $script")

		gscript.metaClass.methodMissing = { mname, margs ->
            _log.fine("missingMethod $mname: $margs")
            // try method on this class
			if (this.respondsTo(mname, margs)) {
                _log.fine("missingMethod: invoke")
                try {
				    return this.invokeMethod(mname, margs)
                }
                catch (groovy.lang.MissingMethodException e) {}
			}
            // try plugin command
            if (isCommand(mname)) {
                _log.fine("missingMethod: command")
                return runCommand(mname, margs)
            }
            else {
                // try script file
                _log.fine("missingMethod: script")
                return runFile(mname, margs)
            }
		}

		gscript.metaClass.propertyMissing = { pname ->
			if (data.containsKey(pname)) return data[pname]
			def globalData = plugin.runner?.data
			if (globalData?.containsKey(pname)) return globalData[pname]
			server.onlinePlayers.find { it.name.startsWith(pname) }
		}

		gscript
	}


	def _initShell(data) {
		shell = new GroovyShell()
		vars = shell.context.variables
		vars.g = this
		vars.s = server
		vars.global = global
		vars.data = data
        shell
	}


	void _updateVars() {
		vars.w = world
		vars.spawn = world.spawnLocation

		def pl = [:]
		plugin.server.onlinePlayers.each { pl[it.name] = it }
		vars.pl = pl
	}


//
// Bukkit helpers
//


	def make(String name, int qty = 1) {
		make(name, world.spawnLocation, qty)
	}


//
// yaml
//


	def dumpyaml(d) {
		Yaml yaml = new Yaml(new GroovyBukkitRepresenter())
		yaml.dump(d)
	}


	def loadyaml(d) {
		Yaml yaml = new Yaml(new GroovyBukkitConstructor(this))
		yaml.load(d)
	}


//
// listener registration
//

	void listen(String uniqueName, def type, Closure c) {
		listen(uniqueName, [(type):c])
	}


	def listen(String uniqueName, Map typeClosureMap, Event.Priority priority = Priority.Normal) {
		unlisten(uniqueName)

		def listener = [toString: {uniqueName}] as Listener
		def typedListeners = [:]
		typeClosureMap.each { def type, closure ->
            def eventType
            try {
                eventType = (type instanceof Event.Type) ? type : Event.Type.valueOf(stringToType(type))
            }
            catch (e) {
                eventType = Event.Type.CUSTOM_EVENT
            }
			if (closure instanceof Closure) {
                def typeName = (eventType == Event.Type.CUSTOM_EVENT ? type : eventType.toString()).toUpperCase()
				typedListeners[typeName] = closure
                if (!registeredTypes.contains(eventType)) {
                    // only register for any given type once (until the pluginManager can support unregistering)
				    plugin.server.pluginManager.registerEvent(eventType, listener, this, priority, plugin)
                    registeredTypes << eventType
                }
			}
		}
		if (typedListeners.size() > 0) {
			this.listeners[uniqueName] = typedListeners
			log "Registered '$uniqueName' with ${typedListeners.size()} listener(s): ${typedListeners.keySet()}"
		}
	}


	void unlisten(String uniqueName) {
		if (listeners.containsKey(uniqueName)) {
			def listeners = listeners.remove(uniqueName)
			listeners?.clear()
			log "Unregistered '$uniqueName'"
		}
	}


	void execute(Listener listener, Event e) {
		def name = listener.toString()
		if (listeners.containsKey(name)) {
			def glisteners = listeners[name]
            def key = e.eventName.toUpperCase()
			if (glisteners.containsKey(key)) {
                Closure glistener = glisteners[key]
                if (e.respondsTo('getPlayer'))
                    plugin.getRunner(e.player).execute(glistener, e)
				else
				    execute(glistener, e)
			}
		}
	}

    void execute(Closure closureListener, Event e) {
        if (closureListener.maximumNumberOfParameters == 1) {
            closureListener(e)
        }
        else {
            closureListener(this, e)
        }
    }


//
// futures
//

    void future(Closure c) {
        plugin.futures.add(0, c)  // pop() comes off the end so always add to the beginnig for fifo
    }


//
// commands
//

	void command(String cmd, Closure c) {
		plugin.commands[cmd] = c
	}

    def isCommand(command) {
        plugin.commands.containsKey(command)
    }

    def runCommand(command, args=null) {
        def closure = plugin.commands[command]
        if (closure && plugin.permitted(player, command)) {
            _log.info("${player ?: 'console'}: $command> $args")
            def result = closure(this, args)
            if (result) {
                _log.info("${player ?: 'console'}: $command< $result")
                if (player) player.sendMessage result.toString()
            }
        }
    }



    @Override protected void finalize() {
		println "${this}.finalize()"
		super.finalize()
	}

}