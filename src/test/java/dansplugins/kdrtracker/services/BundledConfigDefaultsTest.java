package dansplugins.kdrtracker.services;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the mechanism ConfigService relies on for usage reporting: the bundled
 * config.yml is registered by Bukkit as the defaults for the on-disk file, and
 * the one-argument getters fall through to it. A config.yml written by a
 * version before the usage-reporting block existed is only rewritten when the
 * plugin version changes, so this is what keeps reporting attributed on an
 * upgraded server. The two-argument getters do NOT fall through, which is why
 * ConfigService must never use them for these keys.
 */
class BundledConfigDefaultsTest {

    private static final String TRACE_ENDPOINT = "https://trace.danielstephenson.dev";

    private YamlConfiguration bundledDefaults() throws Exception {
        try (InputStream configYml = getClass().getClassLoader().getResourceAsStream("config.yml")) {
            assertNotNull(configYml, "config.yml should be bundled in the jar");
            return YamlConfiguration.loadConfiguration(new InputStreamReader(configYml, StandardCharsets.UTF_8));
        }
    }

    /** What an installation from before the block looks like on disk. */
    private YamlConfiguration onDiskConfigPredatingTheBlock() throws Exception {
        YamlConfiguration onDisk = new YamlConfiguration();
        onDisk.loadFromString("version: v0.1.0\ndebugMode: false\n");
        onDisk.setDefaults(bundledDefaults());
        return onDisk;
    }

    @Test
    void bundledConfigCarriesTheUsageReportingBlock() throws Exception {
        YamlConfiguration bundled = bundledDefaults();
        assertTrue(bundled.getBoolean("usage-reporting.enabled"));
        assertEquals(TRACE_ENDPOINT, bundled.getString("usage-reporting.endpoint"));
        String key = bundled.getString("usage-reporting.key");
        assertNotNull(key);
        assertFalse(key.isEmpty(), "an empty key would turn reporting off out of the box");
        // src/main/resources is Maven-filtered, so an unresolved placeholder would ship verbatim.
        assertFalse(key.contains("${"), "the key must not be a Maven placeholder");
    }

    @Test
    void oneArgumentGettersFallThroughToTheBundledDefaults() throws Exception {
        YamlConfiguration onDisk = onDiskConfigPredatingTheBlock();
        YamlConfiguration bundled = bundledDefaults();
        assertFalse(onDisk.isSet("usage-reporting.key"), "the block should be absent from the on-disk file");
        assertTrue(onDisk.getBoolean("usage-reporting.enabled"));
        assertEquals(TRACE_ENDPOINT, onDisk.getString("usage-reporting.endpoint"));
        assertEquals(bundled.getString("usage-reporting.key"), onDisk.getString("usage-reporting.key"));
    }

    @Test
    void twoArgumentGettersReturnTheirFallbackInsteadOfTheBundledDefaults() throws Exception {
        // Measured, not assumed: this is the trap that would silently turn reporting off on every
        // upgraded server if ConfigService ever switched to the two-argument getters.
        YamlConfiguration onDisk = onDiskConfigPredatingTheBlock();
        assertEquals("", onDisk.getString("usage-reporting.key", ""));
        assertFalse(onDisk.getBoolean("usage-reporting.enabled", false));
    }

    @Test
    void copyDefaultsWritesTheBlockIntoARewrittenConfig() throws Exception {
        // ConfigService#saveMissingConfigDefaultsIfNotPresent sets copyDefaults(true) before saving,
        // so a config rewritten on a version change gains the block on disk as well.
        YamlConfiguration onDisk = onDiskConfigPredatingTheBlock();
        onDisk.options().copyDefaults(true);
        YamlConfiguration rewritten = new YamlConfiguration();
        rewritten.loadFromString(onDisk.saveToString());
        assertEquals(bundledDefaults().getString("usage-reporting.key"), rewritten.getString("usage-reporting.key"));
        assertEquals("v0.1.0", rewritten.getString("version"));
    }
}
