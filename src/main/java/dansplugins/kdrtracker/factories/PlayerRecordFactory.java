package dansplugins.kdrtracker.factories;

import org.bukkit.entity.Player;
import dansplugins.kdrtracker.data.PersistentData;
import dansplugins.kdrtracker.objects.PlayerRecord;

/**
 * @author Daniel McCoy Stephenson
 * @since June 19th, 2022
 *
 * This should be the only place where player records are directly instantiated.
 */
public class PlayerRecordFactory {
    private final PersistentData persistentData;

    public PlayerRecordFactory(PersistentData persistentData) {
        this.persistentData = persistentData;
    }

    public void createPlayerRecord(Player player) {
        PlayerRecord playerRecord = new PlayerRecord(player);
        persistentData.addPlayerRecord(playerRecord);
    }

}