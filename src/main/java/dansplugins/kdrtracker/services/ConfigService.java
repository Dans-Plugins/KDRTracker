package dansplugins.kdrtracker.services;

/*
    To add a new config option, either give it a default in the bundled
    src/main/resources/config.yml (copyDefaults(true) below copies those into
    the on-disk file, and the one-argument getters fall through to them even
    when the file predates the option) or alter
    saveMissingConfigDefaultsIfNotPresent. Either way, document the option in
    CONFIG.md.
 */

import dansplugins.kdrtracker.KDRTracker;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author Daniel McCoy Stephenson
 */
public class ConfigService {
    private static final String USAGE_REPORTING_ENABLED_KEY = "usage-reporting.enabled";
    private static final String USAGE_REPORTING_ENDPOINT_KEY = "usage-reporting.endpoint";
    private static final String USAGE_REPORTING_KEY_KEY = "usage-reporting.key";
    private static final String DEFAULT_USAGE_REPORTING_ENDPOINT = "https://trace.danielstephenson.dev";

    private final KDRTracker kdrTracker;

    public ConfigService(KDRTracker kdrTracker) {
        this.kdrTracker = kdrTracker;
    }

    public void saveMissingConfigDefaultsIfNotPresent() {
        // set version
        if (!getConfig().isString("version")) {
            getConfig().addDefault("version", kdrTracker.getVersion());
        } else {
            getConfig().set("version", kdrTracker.getVersion());
        }

        // save config options
        if (!isSet("debugMode")) { getConfig().set("debugMode", false); }

        getConfig().options().copyDefaults(true);
        kdrTracker.saveConfig();
    }

    public FileConfiguration getConfig() {
        return kdrTracker.getConfig();
    }

    public boolean isSet(String option) {
        return getConfig().isSet(option);
    }

    public boolean getBoolean(String option) {
        return getConfig().getBoolean(option);
    }

    public String getString(String option) {
        return getConfig().getString(option);
    }

    // The one-argument getters, deliberately. An existing config.yml is only
    // rewritten when the plugin version changes, so a server upgraded from a
    // version before usage reporting can have no usage-reporting block on disk.
    // Bukkit registers the jar's config.yml as the defaults for that file, and
    // the one-argument getters fall through to them -- but the two-argument
    // getters return their explicit fallback instead, which for the key would
    // be "" and would turn reporting off on every existing installation.
    // Verified against YamlConfiguration, not assumed.

    public boolean isUsageReportingEnabled() {
        return getBoolean(USAGE_REPORTING_ENABLED_KEY);
    }

    public String getUsageReportingEndpoint() {
        String endpoint = getString(USAGE_REPORTING_ENDPOINT_KEY);
        return endpoint != null ? endpoint : DEFAULT_USAGE_REPORTING_ENDPOINT;
    }

    /** Empty when no key is configured or bundled, which the client treats as "off". */
    public String getUsageReportingKey() {
        String key = getString(USAGE_REPORTING_KEY_KEY);
        return key != null ? key : "";
    }
}
