package dansplugins.kdrtracker.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import dansplugins.kdrtracker.data.PersistentData;
import dansplugins.kdrtracker.factories.PlayerRecordFactory;

/**
 * @author Daniel McCoy Stephenson
 */
public class JoinListener implements Listener {
    private final PersistentData persistentData;
    private final PlayerRecordFactory playerRecordFactory;

    public JoinListener(PersistentData persistentData, PlayerRecordFactory playerRecordFactory) {
        this.persistentData = persistentData;
        this.playerRecordFactory = playerRecordFactory;
    }

    @EventHandler()
    public void handle(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!persistentData.playerHasRecord(player)) {
            playerRecordFactory.createPlayerRecord(player);
        }
    }
}