package dansplugins.kdrtracker;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Daniel McCoy Stephenson
 *
 * Pins KDRTracker#isVersionMismatched, which decides on an existing install
 * whether config.yml is rewritten at start-up (performCompatibilityChecks):
 * a mismatch stamps the running version into the file and copies in any
 * bundled option it lacks. The plugin is mocked because a real JavaPlugin
 * cannot be constructed outside a server; only the method under test runs
 * for real, against a real YamlConfiguration.
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
}
