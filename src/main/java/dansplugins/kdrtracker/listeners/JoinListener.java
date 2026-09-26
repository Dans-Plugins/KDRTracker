package dansplugins.kdrtracker.listeners;

import dansplugins.kdrtracker.data.PersistentData;
import dansplugins.kdrtracker.factories.PlayerRecordFactory;
import dansplugins.kdrtracker.utils.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @author Daniel McCoy Stephenson
 */
public class JoinListener implements Listener {
    private final PersistentData persistentData;
    private final PlayerRecordFactory playerRecordFactory;
    private final Logger logger;

    public JoinListener(PersistentData persistentData, PlayerRecordFactory playerRecordFactory, Logger logger) {
        this.persistentData = persistentData;
        this.playerRecordFactory = playerRecordFactory;
        this.logger = logger;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!persistentData.playerHasRecord(player)) {
            playerRecordFactory.createPlayerRecord(player);
            logger.log("Created a record for " + player.getName() + " (" + player.getUniqueId() + ").");
        }
    }
}