package client.messages;

import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import client.messages.commands.HelpCommand;
import database.DatabaseConnection;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import net.channel.ChannelServer;
import server.TimerManager;
import server.maps.MapleMap;
import tools.ClassFinder;
import tools.MockIOSession;
import tools.Pair;
import tools.StringUtil;

public class CommandProcessor implements CommandProcessorMBean {
    private static List<Pair<MapleCharacter, String>> gmlog = new LinkedList<Pair<MapleCharacter, String>>();
    private Map<String, DefinitionCommandPair> commands = new LinkedHashMap<String, DefinitionCommandPair>();
    private static CommandProcessor instance = new CommandProcessor();
    private static Runnable persister;
    private ScriptEngineFactory sef;

    static {
        persister = new PersistingTask();
        TimerManager.getInstance().register(persister, 62000);
    }

    private CommandProcessor() {
        ScriptEngineManager sem = new ScriptEngineManager();
        sef = sem.getEngineByName("javascript").getFactory();
        instance = this; // hackydihack		
        reloadCommands();
    }

    public static class PersistingTask implements Runnable {
        @Override
        public void run() {
            synchronized (gmlog) {
                Connection con = DatabaseConnection.getConnection();
                try {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO gmlog (cid, command) VALUES (?, ?)");
                    for (Pair<MapleCharacter, String> logentry : gmlog) {
                        ps.setInt(1, logentry.getLeft().getId());
                        ps.setString(2, logentry.getRight());
                        ps.executeUpdate();
                    }
                    ps.close();
                } catch (SQLException e) {
                    System.out.println("error persisting cheatlog");
                    System.out.println(e);
                }
                gmlog.clear();
            }
        }
    }

    public static void registerMBean() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            mBeanServer.registerMBean(instance, new ObjectName("client.messages:name=CommandProcessor"));
        } catch (Exception e) {
            System.out.println("Error registering CommandProcessor MBean");
        }
    }

    public static String joinAfterString(String splitted[], String str) {
        for (int i = 1; i < splitted.length; i++) {
            if (splitted[i].equalsIgnoreCase(str) && i + 1 < splitted.length) {
                return StringUtil.joinStringFrom(splitted, i + 1);
            }
        }
        return null;
    }

    public static int getOptionalIntArg(String splitted[], int position, int def) {
        if (splitted.length > position) {
            try {
                return Integer.parseInt(splitted[position]);
            } catch (NumberFormatException nfe) {
                return def;
            }
        }
        return def;
    }

    public static String getNamedArg(String splitted[], int startpos, String name) {
        for (int i = startpos; i < splitted.length; i++) {
            if (splitted[i].equalsIgnoreCase(name) && i + 1 < splitted.length) {
                return splitted[i + 1];
            }
        }
        return null;
    }

    public static Integer getNamedIntArg(String splitted[], int startpos, String name) {
        String arg = getNamedArg(splitted, startpos, name);
        if (arg != null) {
            try {
                return Integer.parseInt(arg);
            } catch (NumberFormatException nfe) {
                // swallow - we don't really care
            }
        }
        return null;
    }

    public static int getNamedIntArg(String splitted[], int startpos, String name, int def) {
        Integer ret = getNamedIntArg(splitted, startpos, name);
        if (ret == null) {
            return def;
        }
        return ret.intValue();
    }

    public static Double getNamedDoubleArg(String splitted[], int startpos, String name) {
        String arg = getNamedArg(splitted, startpos, name);
        if (arg != null) {
            try {
                return Double.parseDouble(arg);
            } catch (NumberFormatException nfe) {
                // swallow - we don't really care
            }
        }
        return null;
    }

    public boolean processCommand(MapleClient c, String line) {
        return instance.processCommandInternal(c, new ServernoticeMapleClientMessageCallback(c), c.getPlayer().getGMLevel(), line);
    }

    /* (non-Javadoc)
     * @see client.messages.CommandProcessorMBean#processCommandJMX(int, int, java.lang.String)
     */
    public String processCommandJMX(int cserver, int mapid, String command) {
        ChannelServer cserv = ChannelServer.getInstance(cserver);
        if (cserv == null) {
            return "The specified channel Server does not exist in this serverprocess";
        }
        MapleClient c = new MapleClient(new MockIOSession());
        MapleCharacter chr = MapleCharacter.getDefault(c, 26023);
        c.setPlayer(chr);
        chr.setName("/---------jmxuser-------------\\"); // (name longer than maxmimum length)
        MapleMap map = cserv.getMapFactory().getMap(mapid);
        if (map != null) {
            chr.setMap(map);
            SkillFactory.getSkill(5101004).getEffect(1).applyTo(chr);
            map.addPlayer(chr);
        }
        cserv.addPlayer(chr);
        MessageCallback mc = new StringMessageCallback();
        try {
            processCommandInternal(c, mc, 1000, command);
        } finally {
            if (map != null) {
                map.removePlayer(chr);
            }
            cserv.removePlayer(chr);
        }
        return mc.toString();
    }

    public static void forcePersisting() {
        persister.run();
    }

    public static CommandProcessor getInstance() {
        return instance;
    }

    public void reloadCommands() {
        commands.clear();
        try {
            ClassFinder classFinder = new ClassFinder();
            String[] classes = classFinder.listClasses("client.messages.commands", true);
            registerCommand(new HelpCommand()); // register the helpcommand first so it appears first in the list (LinkedHashMap)
            for (String clazz : classes) {
                Class<?> clasz = Class.forName(clazz);
                if (Command.class.isAssignableFrom(clasz)) {
                    try {
                        Command newInstance = (Command) clasz.newInstance();
                        registerCommand(newInstance);
                    } catch (Exception e) {
                        System.out.println("ERROR INSTANCIATING COMMAND CLASS");
                        System.out.println(e);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("THROW");
            System.out.println(e);
        }
        File scriptFolder = new File("scripts/command");
        for (File file : scriptFolder.listFiles()) {
            if (file.isFile() && file.canRead()) {
                FileReader fr = null;
                try {
                    ScriptEngine command = sef.getScriptEngine();
                    fr = new FileReader(file);
                    CompiledScript compiled = ((Compilable) command).compile(fr);
                    compiled.eval();
                    Command c = ((Invocable) command).getInterface(Command.class);
                    registerCommand(c);
                } catch (ScriptException e) {
                    System.out.println("THROW");
                    System.out.println(e);
                } catch (IOException e) {
                    System.out.println("THROW");
                    System.out.println(e);
                } finally {
                    if (fr != null) {
                        try {
                            fr.close();
                        } catch (IOException e) {
                            System.out.println("ERROR CLOSING");
                            System.out.println(e);
                        }
                    }
                }
            }
        }
    }

    private void registerCommand(Command command) {
        CommandDefinition[] definition = command.getDefinition();
        for (CommandDefinition def : definition) {
            commands.put(def.getCommand(), new DefinitionCommandPair(command, def));
        }
    }

    public void dropHelp(MapleCharacter chr, MessageCallback mc, int page) {
        List<DefinitionCommandPair> allCommands = new ArrayList<DefinitionCommandPair>(commands.values());
        int startEntry = (page - 1) * 20;
        mc.dropMessage("Command Help Page: --------" + page + "---------");
        for (int i = startEntry; i < startEntry + 20 && i < allCommands.size(); i++) {
            CommandDefinition commandDefinition = allCommands.get(i).getDefinition();
            if (chr.hasGmLevel(commandDefinition.getRequiredLevel())) {
                dropHelpForDefinition(mc, commandDefinition);
            }
        }
    }

    private void dropHelpForDefinition(MessageCallback mc, CommandDefinition commandDefinition) {
        mc.dropMessage(commandDefinition.getCommand() + " " + commandDefinition.getParameterDescription() + ": " + commandDefinition.getHelp());
    }

    /* (non-Javadoc)
     * @see client.messages.CommandProcessorMBean#processCommandInstance(client.MapleClient, java.lang.String)
     */
    private boolean processCommandInternal(MapleClient c, MessageCallback mc, int gmLevel, String line) {
        MapleCharacter player = c.getPlayer();
        if (line.charAt(0) == '!') {
            String[] splitted = line.split(" ");
            if (splitted.length > 0 && splitted[0].length() > 1) {
                DefinitionCommandPair definitionCommandPair = commands.get(splitted[0].substring(1));
                if (definitionCommandPair != null && gmLevel >= definitionCommandPair.getDefinition().getRequiredLevel()) {
                    synchronized (gmlog) {
                        gmlog.add(new Pair<MapleCharacter, String>(player, line));
                    }
                    try {
                        definitionCommandPair.getCommand().execute(c, mc, splitted);
                    } catch (IllegalCommandSyntaxException e) {
                        mc.dropMessage("IllegalCommandSyntaxException:" + e.getMessage());
                        dropHelpForDefinition(mc, definitionCommandPair.getDefinition());
                    } catch (Exception e) {
                        mc.dropMessage("An error occured: " + e.getClass().getName() + " " + e.getMessage());
                        System.out.println("COMMAND ERROR");
                        System.out.println(e);
                    }
                    return true;
                } else {
                    if (definitionCommandPair == null && c.getPlayer().getGMLevel() > 0) {
                        mc.dropMessage("Command " + splitted[0] + " does not exist or you do not have the required priviledges");
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

class DefinitionCommandPair {
    private Command command;
    private CommandDefinition definition;

    public DefinitionCommandPair(Command command, CommandDefinition definition) {
        super();
        this.command = command;
        this.definition = definition;
    }

    public Command getCommand() {
        return command;
    }

    public CommandDefinition getDefinition() {
        return definition;
    }
}
