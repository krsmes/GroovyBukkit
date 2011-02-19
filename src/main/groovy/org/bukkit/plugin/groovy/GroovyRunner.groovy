package org.bukkit.plugin.groovy

import org.bukkit.event.Event
import org.bukkit.event.Event.Priority
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.yaml.snakeyaml.Yaml


class GroovyRunner extends GroovyAPI implements EventExecutor {

	static SCRIPT_PREFIX = """
import org.bukkit.*;import org.bukkit.block.*;import org.bukkit.entity.*;import org.bukkit.inventory.*;import org.bukkit.material.*;import org.bukkit.event.*;import org.bukkit.util.*;
"""

	GroovyShell shell
	GroovyPlugin plugin

	def data = [:]
	def listeners = [:]


	GroovyRunner(GroovyPlugin plugin, def data) {
		super(plugin.server)
		this.plugin = plugin
		this.data = data
		shell = _initShell(data)
	}


	void _shutdown() {
		def listenerNames = listeners.keySet() as List
        listenerNames.each { unlisten(it) }
		// save data
		def datafile = new File(initScriptsLoc, 'data.yml')
		if (!datafile.exists()) datafile.createNewFile()
        log "Storing $datafile"
        try {
            data.remove('last')
		    datafile.text = dumpyaml(data)
        }
        catch (e) {
            log "Unable to store: $data\n$e.message"
        }
	}


	def init() {
		def dir = new File(initScriptsLoc)
		if (!dir.exists()) { dir.mkdirs() }
		// load data
		def datafile = new File(dir, 'data.yml')
		if (datafile.exists()) {
            log "Loading $datafile"
			data.putAll loadyaml(datafile.text)
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


	def getInitScriptsLoc() {
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
				def vars = _runContext()
				def savedArgs = vars.args
				vars.args = args

				result = _parse(script, vars).run()

				vars.args = savedArgs
			}
			catch (e) {
				result = e.message
				e.printStackTrace()
			}
		}
		result
	}

	def _parse = { script, vars ->
		def gscript = shell.parse(script)

		gscript.metaClass.methodMissing = { mname, margs ->
			if (this.respondsTo(mname, margs)) {
				this.invokeMethod(mname, margs)
			}
			else {
				runFile(mname, margs)
			}
		}

		gscript.metaClass.propertyMissing = { pname ->
			if (data.containsKey(pname)) return data[pname]
			def globalData = plugin.runner.data
			if (globalData.containsKey(pname)) return globalData[pname]
			server.onlinePlayers.find { it.name.startsWith(pname) }
		}

		gscript
	}


	def _initShell(data) {
		def shell = new GroovyShell()
		def vars = shell.context.variables
		vars.g = this
		vars.s = server
		vars.global = plugin.runner?.data ?: data
		vars.data = data
		shell
	}


	Map _runContext() {
		def vars = shell.context.variables

		vars.w = world
		vars.spawn = world.spawnLocation

		def pl = [:]
		plugin.server.onlinePlayers.each { pl[it.name] = it }
		vars.pl = pl

		vars
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
			if (!(type instanceof Event.Type)) type = Event.Type."${stringToType(type)}"
			if (closure instanceof Closure) {
				typedListeners[type] = closure
				plugin.server.pluginManager.registerEvent(type, listener, this, priority, plugin)
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
			def listeners = listeners[name]
			if (listeners.containsKey(e.type)) {
				listeners[e.type](e)
			}
		}
	}


	void command(String cmd, Closure c) {
		plugin.commands[cmd] = c
	}


	@Override protected void finalize() {
		println "${this}.finalize()"
		super.finalize()
	}

}