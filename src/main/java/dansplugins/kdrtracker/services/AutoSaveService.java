package dansplugins.kdrtracker.services;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

/**
 * @author Daniel McCoy Stephenson
 *
 * Saves player records on a fixed interval, so a crash or a killed server
 * loses at most one interval of kills and deaths instead of everything since
 * the last start. The save runs on the main thread: the listeners change
 * player records there, and reading them from another thread at the same
 * time is not safe.
 */
public class AutoSaveService {
    private static final long TICKS_PER_MINUTE = 20L * 60L;

    private final StorageService storageService;

    public AutoSaveService(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Schedules the repeating save.
     * @param scheduler The scheduler to register the task with.
     * @param plugin The plugin that owns the task.
     * @param intervalMinutes Minutes between saves; zero or less turns autosaving off.
     * @return The scheduled task, or null if autosaving is off.
     */
    public BukkitTask schedule(BukkitScheduler scheduler, Plugin plugin, int intervalMinutes) {
        if (intervalMinutes <= 0) {
            return null;
        }
        long intervalTicks = intervalMinutes * TICKS_PER_MINUTE;
        return scheduler.runTaskTimer(plugin, storageService::save, intervalTicks, intervalTicks);
    }
}
