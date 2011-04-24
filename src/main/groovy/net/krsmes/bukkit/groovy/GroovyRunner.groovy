package net.krsmes.bukkit.groovy

import org.bukkit.event.Event
import org.bukkit.event.Event.Priority
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.yaml.snakeyaml.Yaml
import org.bukkit.entity.Player

class GroovyRunner extends GroovyAPI {

	static SCRIPT_PREFIX = """
import org.bukkit.*;import org.bukkit.block.*;import org.bukkit.entity.*;import org.bukkit.inventory.*;import org.bukkit.material.*;import org.bukkit.event.*;import org.bukkit.util.*;
"""

    def plugin
    GroovyShell shell
    Map vars

    Map<String,Object> global
	Map<String,Object> data = [:]
    Player player = null


	GroovyRunner(def plugin, def data) {
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
        _save()
    }

    void _save() {
        // save data
        def datafile = new File(_initScriptsLoc, 'data.yml')
        if (!datafile.exists()) datafile.createNewFile()
        log "Storing $datafile"
        try {
            def savableData = [:]
            data.each{ k, v -> if (k!='last' && k!='temp' && k!='plot' && v instanceof Serializable) savableData.put(k,v) }
            datafile.text = dumpyaml(savableData)
        }
        catch (e) {
            log "Unable to store: $data\n$e.message"
        }
    }


    def get_initScriptsLoc() {
		scriptLoc + 'startup/'
	}


	def runScript(def script) {
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
        vars.util = net.krsmes.bukkit.groovy.Util
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
        debug "listen(uniqueName=$uniqueName, type=$type, c=$c)"
		listen(uniqueName, [(type):c])
	}

    void listen(String uniqueName, Map typedClosureMap) {
        debug "listen(uniqueName=$uniqueName, ...)"
        ListenerClosures.instance.unregister(uniqueName);

        typedClosureMap.each { def type, closure ->
            def eventType
            try {
                eventType = (type instanceof Event.Type) ? type : Event.Type.valueOf(stringToType(type))
            }
            catch (e) {
                eventType = Event.Type.CUSTOM_EVENT
            }
            if (closure instanceof Closure) {
                eventType == Event.Type.CUSTOM_EVENT ?
                    ListenerClosures.instance.register(uniqueName, type.toString().toUpperCase(), closure) :
                    ListenerClosures.instance.register(uniqueName, eventType, closure)
            }
        }
    }


    void unlisten(String uniqueName) {
        ListenerClosures.instance.unregister(uniqueName);
    }


    void execute(Closure closure, Event e) {
        if (closure.maximumNumberOfParameters == 1) {
            closure(e)
        }
        else {
            closure(this, e)
        }
    }


//
// futures
//

    def future(int delay = 0, boolean sync=false, Closure c) {
        sync ?
            plugin.server.scheduler.scheduleSyncDelayedTask(plugin, c, delay) :
            plugin.server.scheduler.scheduleAsyncDelayedTask(plugin, c, delay)
    }

    def repeat(int period, boolean sync=false, Closure c) {
        sync ?
            plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, c, 0, period) :
            plugin.server.scheduler.scheduleAsyncRepeatingTask(plugin, c, 0, period)
    }


//
// commands
//

    boolean permitted(command) {
        plugin.permitted(player, command)
    }

	void command(String cmd, Closure c) {
		plugin.commands[cmd] = c
	}

    def isCommand(command) {
        plugin.commands.containsKey(command)
    }

    def runCommand(command, args=null) {
        def closure = plugin.commands[command]
        if (closure && permitted(command)) {
            if (player?.name != 'krsmes') _log.info("${player?.name ?: 'console'}: $command> $args")
            def result = closure(this, args)
            if (result) {
                if (player?.name != 'krsmes') _log.info("${player?.name ?: 'console'}: $command< $result")
                if (player) player.sendMessage result.toString()
            }
        }
    }



    @Override protected void finalize() {
		println "${this}.finalize()"
		super.finalize()
	}

}