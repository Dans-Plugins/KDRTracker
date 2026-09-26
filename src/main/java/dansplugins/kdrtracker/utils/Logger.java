package dansplugins.kdrtracker.utils;

import java.util.function.BooleanSupplier;

/**
 * @author Daniel McCoy Stephenson
 *
 * Writes debug messages to the plugin's logger when debugMode is on. The
 * switch is read on every call, so a config reload takes effect without a
 * restart.
 */
public class Logger {
    private final java.util.logging.Logger output;
    private final BooleanSupplier debugEnabled;

    public Logger(java.util.logging.Logger output, BooleanSupplier debugEnabled) {
        this.output = output;
        this.debugEnabled = debugEnabled;
    }

    public void log(String message) {
        if (debugEnabled.getAsBoolean()) {
            output.info("[DEBUG] " + message);
        }
    }

}
