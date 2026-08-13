package dansplugins.kdrtracker.services;

/*
    To add a new config option, saveMissingConfigDefaultsIfNotPresent must be
    altered and the option documented in CONFIG.md.
 */

import dansplugins.kdrtracker.KDRTracker;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author Daniel McCoy Stephenson
 */
public class ConfigService {
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
}