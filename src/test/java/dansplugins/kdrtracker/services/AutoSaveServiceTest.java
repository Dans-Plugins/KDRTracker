package dansplugins.kdrtracker.services;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Daniel McCoy Stephenson
 *
 * Pins how the autosave is scheduled: a repeating main-thread task whose
 * callback saves the player records, every saveInterval minutes, and nothing
 * at all when the interval is zero or less.
 */
class AutoSaveServiceTest {
    private StorageService storageService;
    private BukkitScheduler scheduler;
    private Plugin plugin;
    private AutoSaveService autoSaveService;

    @BeforeEach
    void setUp() {
        storageService = mock(StorageService.class);
        scheduler = mock(BukkitScheduler.class);
        plugin = mock(Plugin.class);
        autoSaveService = new AutoSaveService(storageService);
    }

    @Test
    void schedule_runsASaveEveryIntervalOnTheMainThread() {
        BukkitTask task = mock(BukkitTask.class);
        when(scheduler.runTaskTimer(eq(plugin), any(Runnable.class), anyLong(), anyLong())).thenReturn(task);

        assertSame(task, autoSaveService.schedule(scheduler, plugin, 5));

        ArgumentCaptor<Runnable> callback = ArgumentCaptor.forClass(Runnable.class);
        // 5 minutes at 20 ticks a second, for the first run and every run after it
        verify(scheduler).runTaskTimer(eq(plugin), callback.capture(), eq(6000L), eq(6000L));
        verify(scheduler, never()).runTaskTimerAsynchronously(any(Plugin.class), any(Runnable.class), anyLong(), anyLong());
        verify(storageService, never()).save();

        callback.getValue().run();

        verify(storageService).save();
    }

    @Test
    void schedule_withAZeroInterval_schedulesNothing() {
        assertNull(autoSaveService.schedule(scheduler, plugin, 0));
        verifyNoInteractions(scheduler);
    }

    @Test
    void schedule_withANegativeInterval_schedulesNothing() {
        assertNull(autoSaveService.schedule(scheduler, plugin, -1));
        verifyNoInteractions(scheduler);
    }
}
