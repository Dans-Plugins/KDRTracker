package dansplugins.kdrtracker;

import dansplugins.kdrtracker.commands.HelpCommand;
import dansplugins.kdrtracker.services.ConfigService;
import dansplugins.kdrtracker.services.StorageService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;
import preponderous.ponder.minecraft.bukkit.services.CommandService;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Daniel McCoy Stephenson
 *
 * Pins KDRTracker#isVersionMismatched, which decides on an existing install
 * whether config.yml is rewritten at start-up (performCompatibilityChecks):
 * a mismatch stamps the running version into the file and copies in any
 * bundled option it lacks. Also pins the usage-reporting notice, what
 * onEnable does when the player records cannot be read, and that onCommand
 * routes a sub-command however its base command was typed. The plugin is mocked
 * because a real JavaPlugin cannot be constructed outside a server; only the
 * method under test runs for real, against a real YamlConfiguration.
 */
class KDRTrackerTest {
    private static final String PLUGIN_VERSION = "v1.2.3";

    private KDRTracker kdrTracker;
    private YamlConfiguration config;

    @BeforeEach
    void setUp() {
        kdrTracker = mock(KDRTracker.class);
        config = new YamlConfiguration();
        when(kdrTracker.getConfig()).thenReturn(config);
        when(kdrTracker.getVersion()).thenReturn(PLUGIN_VERSION);
        when(kdrTracker.isVersionMismatched()).thenCallRealMethod();
    }

    @Test
    void isVersionMismatched_whenTheConfigHasTheRunningVersion_isFalse() {
        config.set("version", PLUGIN_VERSION);
        assertFalse(kdrTracker.isVersionMismatched());
    }

    @Test
    void isVersionMismatched_whenTheConfigHasAnotherVersion_isTrue() {
        config.set("version", "v0.1.0");
        assertTrue(kdrTracker.isVersionMismatched());
    }

    @Test
    void isVersionMismatched_ignoresCase() {
        config.set("version", "V1.2.3");
        assertFalse(kdrTracker.isVersionMismatched());
    }

    @Test
    void isVersionMismatched_whenTheConfigHasNoVersion_isFalse() {
        // A config.yml without a version line is not rewritten on this account;
        // it is still rewritten if it lacks the usage-reporting block.
        assertFalse(kdrTracker.isVersionMismatched());
    }

    @Test
    void isVersionMismatched_whenTheRunningVersionIsUnknown_isFalse() {
        when(kdrTracker.getVersion()).thenReturn(null);
        config.set("version", "v0.1.0");
        assertFalse(kdrTracker.isVersionMismatched());
    }

    @Test
    void usageReportingNotice_namesTheConfiguredEndpoint() {
        String notice = KDRTracker.usageReportingNotice("KDRTracker", "https://trace.example.org");

        assertTrue(notice.contains("https://trace.example.org"));
        assertFalse(notice.contains("trace.danielstephenson.dev"));
    }

    @Test
    void onEnable_whenThePlayerRecordsCannotBeRead_disablesThePluginAndSetsNothingUp() throws Exception {
        StorageService storageService = mock(StorageService.class);
        when(storageService.load()).thenReturn(false);
        ConfigService configService = mock(ConfigService.class);
        setField("storageService", storageService);
        setField("configService", configService);
        Server server = mock(Server.class);
        PluginManager pluginManager = mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(kdrTracker.getServer()).thenReturn(server);
        when(kdrTracker.getLogger()).thenReturn(mock(java.util.logging.Logger.class));
        doCallRealMethod().when(kdrTracker).onEnable();

        kdrTracker.onEnable();

        verify(pluginManager).disablePlugin(kdrTracker);
        verifyNoInteractions(configService);
        verify(server, never()).getScheduler();
    }

    @ParameterizedTest
    @ValueSource(strings = {"kdrt", "kdrtracker", "kdrtracker:kdrt", "kdrtracker:kdrtracker"})
    void aSubCommand_reachesItsCommand_whicheverWayTheBaseCommandIsTyped(String typed) throws Exception {
        // Everything between the typed line and HelpCommand is real except the plugin itself.
        CommandService commandService = new CommandService(new HashSet<>(Arrays.asList("kdrt", "kdrtracker")));
        commandService.initialize(new ArrayList<AbstractPluginCommand>(Arrays.asList(new HelpCommand())), "That command wasn't found.");
        setField("commandService", commandService);
        when(kdrTracker.onCommand(any(), any(), any(), any())).thenCallRealMethod();
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("kdrt.help")).thenReturn(true);

        // SimpleCommandMap's timings reach for the static Bukkit server.
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class, RETURNS_DEEP_STUBS)) {
            SimpleCommandMap commandMap = new SimpleCommandMap(mock(Server.class));
            commandMap.register("kdrtracker", new CommandForwardedToThePlugin("kdrt"));
            commandMap.register("kdrtracker", new CommandForwardedToThePlugin("kdrtracker"));

            assertTrue(commandMap.dispatch(sender, typed + " help"));
        }

        verify(sender).sendMessage(ChatColor.AQUA + "/kdrt help - View a list of helpful commands.");
    }

    /** Hands a command to the plugin the way Bukkit's PluginCommand does. */
    private class CommandForwardedToThePlugin extends Command {
        CommandForwardedToThePlugin(String name) {
            super(name);
        }

        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            return kdrTracker.onCommand(sender, this, commandLabel, args);
        }
    }

    private void setField(String name, Object value) throws ReflectiveOperationException {
        // These are final and set by the constructor, which a mock never runs.
        Field field = KDRTracker.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(kdrTracker, value);
    }
}
