package dansplugins.kdrtracker.services;

import dansplugins.kdrtracker.KDRTracker;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Daniel McCoy Stephenson
 *
 * Pins what ConfigService does with the config it is handed: which options
 * saveMissingConfigDefaultsIfNotPresent writes or keeps, and what the
 * usage-reporting getters return when a key is absent and no bundled default
 * covers it. BundledConfigDefaultsTest covers the fall-through to the bundled
 * config.yml; these tests use a config with no defaults registered, so they
 * exercise ConfigService's own fallbacks.
 */
class ConfigServiceTest {
    private static final String PLUGIN_VERSION = "v1.2.3";

    private KDRTracker kdrTracker;
    private YamlConfiguration config;
    private ConfigService configService;

    @BeforeEach
    void setUp() {
        kdrTracker = mock(KDRTracker.class);
        config = new YamlConfiguration();
        when(kdrTracker.getConfig()).thenReturn(config);
        when(kdrTracker.getVersion()).thenReturn(PLUGIN_VERSION);
        configService = new ConfigService(kdrTracker);
    }

    @Test
    void saveMissingConfigDefaults_onAnEmptyConfig_writesTheVersionAndDebugModeAndSaves() {
        configService.saveMissingConfigDefaultsIfNotPresent();

        assertEquals(PLUGIN_VERSION, config.getString("version"));
        assertTrue(config.isSet("debugMode"));
        assertFalse(config.getBoolean("debugMode"));
        verify(kdrTracker).saveConfig();
    }

    @Test
    void saveMissingConfigDefaults_replacesAnOlderVersion() {
        config.set("version", "v0.1.0");

        configService.saveMissingConfigDefaultsIfNotPresent();

        assertEquals(PLUGIN_VERSION, config.getString("version"));
    }

    @Test
    void saveMissingConfigDefaults_keepsAnExistingDebugMode() {
        config.set("debugMode", true);

        configService.saveMissingConfigDefaultsIfNotPresent();

        assertTrue(config.getBoolean("debugMode"));
    }

    @Test
    void saveMissingConfigDefaults_keepsOtherOptions() {
        config.set("saveInterval", 10);
        config.set("usage-reporting.enabled", false);

        configService.saveMissingConfigDefaultsIfNotPresent();

        assertEquals(10, config.getInt("saveInterval"));
        assertFalse(config.getBoolean("usage-reporting.enabled"));
    }

    @Test
    void getSaveInterval_readsTheConfiguredMinutes() {
        config.set("saveInterval", 15);
        assertEquals(15, configService.getSaveInterval());
    }

    @Test
    void getSaveInterval_withNoValueAndNoBundledDefault_isZero() {
        // Zero turns autosaving off; in practice the bundled config.yml supplies 5.
        assertEquals(0, configService.getSaveInterval());
    }

    @Test
    void isUsageReportingEnabled_readsTheSwitch() {
        config.set("usage-reporting.enabled", true);
        assertTrue(configService.isUsageReportingEnabled());

        config.set("usage-reporting.enabled", false);
        assertFalse(configService.isUsageReportingEnabled());
    }

    @Test
    void getUsageReportingEndpoint_readsTheConfiguredEndpoint() {
        config.set("usage-reporting.endpoint", "https://example.invalid");
        assertEquals("https://example.invalid", configService.getUsageReportingEndpoint());
    }

    @Test
    void getUsageReportingEndpoint_withNoValueAndNoBundledDefault_isTheTraceServer() {
        assertEquals("https://trace.danielstephenson.dev", configService.getUsageReportingEndpoint());
    }

    @Test
    void getUsageReportingKey_readsTheConfiguredKey() {
        config.set("usage-reporting.key", "some-key");
        assertEquals("some-key", configService.getUsageReportingKey());
    }

    @Test
    void getUsageReportingKey_withNoValueAndNoBundledDefault_isEmpty() {
        // The trace client treats an empty key as "off".
        assertEquals("", configService.getUsageReportingKey());
    }
}
