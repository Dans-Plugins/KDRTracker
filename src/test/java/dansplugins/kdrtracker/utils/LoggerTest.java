package dansplugins.kdrtracker.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Daniel McCoy Stephenson
 *
 * Covers the debugMode gate: messages reach the plugin's logger only while
 * the switch reads true, and the switch is consulted on every call.
 */
class LoggerTest {
    private final List<LogRecord> records = new ArrayList<>();
    private final Handler capturingHandler = new Handler() {
        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    };
    private java.util.logging.Logger output;

    @BeforeEach
    void setUp() {
        output = java.util.logging.Logger.getAnonymousLogger();
        output.setUseParentHandlers(false);
        output.addHandler(capturingHandler);
    }

    @AfterEach
    void tearDown() {
        output.removeHandler(capturingHandler);
    }

    @Test
    void log_whenDebugDisabled_writesNothing() {
        Logger logger = new Logger(output, () -> false);

        logger.log("should not appear");

        assertTrue(records.isEmpty());
    }

    @Test
    void log_whenDebugEnabled_writesTheMessageAtInfoWithADebugPrefix() {
        Logger logger = new Logger(output, () -> true);

        logger.log("Recorded a kill");

        assertEquals(1, records.size());
        assertEquals(Level.INFO, records.get(0).getLevel());
        assertEquals("[DEBUG] Recorded a kill", records.get(0).getMessage());
    }

    @Test
    void log_readsTheSwitchOnEveryCall() {
        AtomicBoolean debugEnabled = new AtomicBoolean(false);
        Logger logger = new Logger(output, debugEnabled::get);

        logger.log("first");
        debugEnabled.set(true);
        logger.log("second");
        debugEnabled.set(false);
        logger.log("third");

        assertEquals(1, records.size());
        assertEquals("[DEBUG] second", records.get(0).getMessage());
    }
}
