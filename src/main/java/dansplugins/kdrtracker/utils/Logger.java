package dansplugins.kdrtracker.utils;

import dansplugins.kdrtracker.KDRTracker;

/**
 * @author Daniel McCoy Stephenson
 */
public class Logger {
    private final KDRTracker kdrTracker;

    public Logger(KDRTracker kdrTracker) {
        this.kdrTracker = kdrTracker;
    }

    public void log(String message) {
        if (kdrTracker.isDebugEnabled()) {
            System.out.println("[ExamplePonderPlugin] " + message);
        }
    }

}
