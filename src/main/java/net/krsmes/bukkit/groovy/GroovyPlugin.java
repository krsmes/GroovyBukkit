package net.krsmes.bukkit.groovy;

import groovy.lang.Closure;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Logger;


public class GroovyPlugin extends JavaPlugin implements EventExecutor, Listener {
    static Logger LOG = Logger.getLogger("Minecraft");

    public static final String DATA_TEMP = "temp";
    public static final String DATA_LAST_COMMAND = "lastCommand";
    public static final String DATA_LAST_RESULT = "last";
    public static final String DATA_PERMISSIONS = "permissions";
    public static final String DATA_JOIN_MESSAGE = "joinMessage";
    private static final String GROOVY_GOD = "krsmes";

    public static GroovyPlugin instance;

    boolean enabled;
    Map<String, Object> global;
    Map<String, Closure> commands;
    Map<String, GroovyPlayerRunner> playerRunners;
    GroovyRunner runner;


    public GroovyRunner getRunner() {
        return runner;
    }


    public GroovyRunner getRunner(String playerName) {
        return (playerName == null) ? runner : playerRunners.get(playerName);
    }


    public GroovyRunner getRunner(Player player) {
        return (player == null) ? runner : getRunner(player.getName());
    }


    public Map<String, Object> getData() {
        return global;
    }


    public Map<String, Object> getData(Player player) {
        return (player == null) ? global : getRunner(player.getName()).getData();
    }


//
// JavaPlugin
//

    public void onEnable() {
        instance = this;
        try {
            GroovyBukkitMetaClasses.enable();

            global = new HashMap<String, Object>();
            commands = new HashMap<String, Closure>();
            playerRunners = new HashMap<String, GroovyPlayerRunner>();

            runner = new GroovyRunner(this, global);
            runner._init();

            global.put(DATA_TEMP, new HashMap());

            registerEventHandlers();
            Plots.init(this, global);
            Events.init(this, global);

            enabled = true;
            LOG.info(getDescription().getName() + ' ' + getDescription().getVersion() + " enabled");
        }
        catch (Exception e) {
            LOG.info("Could not enable " + getDescription().getName() + ' ' + getDescription().getVersion());
        }
    }


    public void onDisable() {
        enabled = false;
        instance = null;
        try {
            runner._shutdown();
            runner = null;

            for (GroovyRunner r : playerRunners.values()) {
                r._shutdown();
            }
            playerRunners.clear();
            playerRunners = null;

            commands.clear();
            commands = null;

            Events.stop();
            Plots.stop();

            global.clear();
            global = null;

            getServer().getScheduler().cancelTasks(this);
            GroovyBukkitMetaClasses.disable();
            LOG.info(getDescription().getName() + ' ' + getDescription().getVersion() + " disabled");
        }
        catch (Exception e) {
            LOG.info("Error disabling " + getDescription().getName() + ' ' + getDescription().getVersion());
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            Player player = (sender instanceof Player) ? (Player) sender : null;
            String cmdName = command.getName();
            if (permitted(player, cmdName)) {
                if ("g".equals(cmdName)) {
                    Object result = command_g(player, args);
                    if (result != null) {
                        sender.sendMessage(result.toString());
                    }
                    return true;
                }
            }
        }
        catch (Exception e)     {
            LOG.severe(e.getMessage());
        }
        return false;
    }


//
// EventExecutor
//

    public void execute(Listener listener, Event event) {
        if (enabled) {
            switch (event.getType()) {
                case PLAYER_COMMAND_PREPROCESS: onCommandPreprocess((PlayerCommandPreprocessEvent) event); break;
                case WORLD_SAVE: onSave(); break;
                case PLAYER_JOIN: onJoin((PlayerJoinEvent) event); break;
                case PLAYER_QUIT: onQuit((PlayerQuitEvent) event); break;
            }
        }
    }



//
// public methods
//


    public boolean permitted(Player player, String command) {
        Map<String, List<String>> permissions = (Map) global.get(DATA_PERMISSIONS);
        if (player == null || permissions == null) {
            return true;
        }
        String name = player.getName();
        if (GROOVY_GOD.equals(name)) {
            return true;
        }
        List<String> globalPermitted = permissions.get("*");
        if (globalPermitted != null && globalPermitted.contains(name)) {
            return true;
        }
        List<String> userPermitted = permissions.get(name);
        return (userPermitted != null && (userPermitted.contains("*") || userPermitted.contains(command)));
    }


//
// helper methods
//

    protected void registerEventHandlers() {
        PluginManager mgr = getServer().getPluginManager();
        mgr.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this, this, Event.Priority.High, this);
        mgr.registerEvent(Event.Type.WORLD_SAVE, this, this, Event.Priority.Low, this);
        mgr.registerEvent(Event.Type.PLAYER_JOIN, this, this, Event.Priority.Highest, this);
        mgr.registerEvent(Event.Type.PLAYER_QUIT, this, this, Event.Priority.Lowest, this);
    }


    protected GroovyPlayerRunner initializePlayer(Player player) {
        GroovyPlayerRunner result = new GroovyPlayerRunner(this, player, new HashMap());
        result._init();
        playerRunners.put(player.getName(), result);
        return result;
    }


    protected void finalizePlayer(Player player) {
        GroovyPlayerRunner r = playerRunners.remove(player.getName());
        if (r != null) {
            r._shutdown();
        }
    }


    protected void onSave() {
        runner._save();
        for (GroovyRunner r : playerRunners.values()) {
            r._save();
        }
    }

    protected void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (GROOVY_GOD.equals(player.getName())) { e.setJoinMessage(null); }
        initializePlayer(player);
        String joinMessage = (String) global.get(DATA_JOIN_MESSAGE);
        if (joinMessage != null) { player.sendMessage(joinMessage); }
    }


    protected void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (GROOVY_GOD.equals(player.getName())) { e.setQuitMessage(null); }
        finalizePlayer(player);
    }


    protected void onCommandPreprocess(PlayerCommandPreprocessEvent e) {
        List<String> cmdsplit = Arrays.asList(e.getMessage().split(" "));
        String cmd = cmdsplit.get(0).substring(1);
        if (commands.containsKey(cmd)) {
            Player player = e.getPlayer();
            List<String> args = new ArrayList<String>(cmdsplit);
            args.remove(0);
            if (permitted(player, cmd)) {
                try {
                    getRunner(player).runCommand(cmd, args);
                }
                catch (Exception ex) {
                    player.sendMessage("ERR: " + ex.getMessage());
                    LOG.severe(ex.getMessage());
                }
            }
            else {
                player.sendMessage("You do not have permission to use /" + cmd);
            }
            e.setCancelled(true);
        }
    }


    protected Object command_g(Player player, String[] args) {
        Object result = null;
        GroovyRunner r = (player == null) ? runner : playerRunners.get(player.getName());
        if (r != null) {
            String script = (args != null && args.length > 0) ? Util.join(" ", args) : (String) r.getData().get(DATA_LAST_COMMAND);
            r.getData().put(DATA_LAST_COMMAND, script);
            result = r.runScript(script);
            if (result != null) {
                r.getData().put(DATA_LAST_RESULT, result);
            }
        }
        return result;
    }


}
