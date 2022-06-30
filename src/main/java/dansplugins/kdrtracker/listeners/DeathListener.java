package dansplugins.kdrtracker.listeners;

import dansplugins.kdrtracker.data.PersistentData;
import dansplugins.kdrtracker.exceptions.PlayerRecordNotFoundException;
import dansplugins.kdrtracker.objects.PlayerRecord;
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

    public DeathListener(PersistentData persistentData) {
        this.persistentData = persistentData;
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
            // error
            return;
        }

        victimsPlayerRecord.incrementDeaths();
        victim.sendMessage("You now have " + victimsPlayerRecord.getDeaths() + " deaths.");
    }

    private void handleKill(Player killer) {
        if (killer == null) {
            return;
        }

        final PlayerRecord killersPlayerRecord;
        try {
            killersPlayerRecord = persistentData.getPlayerRecord(killer.getUniqueId());
        } catch(PlayerRecordNotFoundException e) {
            // error
            return;
        }

        killersPlayerRecord.incrementKills();
        killer.sendMessage("You now have " + killersPlayerRecord.getKills() + " kills.");
    }
}