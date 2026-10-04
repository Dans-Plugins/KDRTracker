package dansplugins.kdrtracker.listeners;

import dansplugins.kdrtracker.data.PersistentData;
import dansplugins.kdrtracker.exceptions.PlayerRecordNotFoundException;
import dansplugins.kdrtracker.objects.PlayerRecord;
import dansplugins.kdrtracker.utils.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * @author Daniel McCoy Stephenson
 * @since June 19th, 2022
 */
public class DeathListener implements Listener {
    private final PersistentData persistentData;
    private final Logger logger;

    public DeathListener(PersistentData persistentData, Logger logger) {
        this.persistentData = persistentData;
        this.logger = logger;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player victim = event.getEntity();
        handleDeath(victim);

        final Player killer = victim.getKiller();
        handleKill(killer);
    }

    private void handleDeath(Player victim) {
        final PlayerRecord victimsPlayerRecord;

        try {
            victimsPlayerRecord = persistentData.getPlayerRecord(victim.getUniqueId());
        } catch(PlayerRecordNotFoundException e) {
            logger.log("No record for " + victim.getName() + " (" + victim.getUniqueId() + "); death not recorded.");
            return;
        }

        victimsPlayerRecord.incrementDeaths();
        logger.log("Recorded a death for " + victim.getName() + "; deaths now " + victimsPlayerRecord.getDeaths() + ".");
        victim.sendMessage("You now have " + countOf(victimsPlayerRecord.getDeaths(), "death", "deaths") + ".");
    }

    private void handleKill(Player killer) {
        if (killer == null) {
            return;
        }

        final PlayerRecord killersPlayerRecord;
        try {
            killersPlayerRecord = persistentData.getPlayerRecord(killer.getUniqueId());
        } catch(PlayerRecordNotFoundException e) {
            logger.log("No record for " + killer.getName() + " (" + killer.getUniqueId() + "); kill not recorded.");
            return;
        }

        killersPlayerRecord.incrementKills();
        logger.log("Recorded a kill for " + killer.getName() + "; kills now " + killersPlayerRecord.getKills() + ".");
        killer.sendMessage("You now have " + countOf(killersPlayerRecord.getKills(), "kill", "kills") + ".");
    }

    /**
     * Formats a count with the noun that agrees with it: "1 kill", "2 kills", "0 kills".
     */
    static String countOf(int count, String singular, String plural) {
        return count + " " + (count == 1 ? singular : plural);
    }
}
