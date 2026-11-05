package dansplugins.kdrtracker;

import dansplugins.kdrtracker.services.ConfigService;
import dansplugins.kdrtracker.services.StorageService;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Daniel McCoy Stephenson
 *
 * Pins KDRTracker#isVersionMismatched, which decides on an existing install
 * whether config.yml is rewritten at start-up (performCompatibilityChecks):
 * a mismatch stamps the running version into the file and copies in any
 * bundled option it lacks. Also pins the usage-reporting notice and what
 * onEnable does when the player records cannot be read. The plugin is mocked
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

    private void setField(String name, Object value) throws ReflectiveOperationException {
        // These are final and set by the constructor, which a mock never runs.
        Field field = KDRTracker.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(kdrTracker, value);
    }
}
