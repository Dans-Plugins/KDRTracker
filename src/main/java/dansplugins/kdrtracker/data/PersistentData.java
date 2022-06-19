package dansplugins.kdrtracker.data;

import dansplugins.kdrtracker.exceptions.PlayerRecordNotFoundException;
import org.bukkit.entity.Player;
import dansplugins.kdrtracker.objects.PlayerRecord;

import java.util.HashSet;
import java.util.UUID;

/**
 * @author Daniel McCoy Stephenson
 * @since June 19th, 2022
 *
 * This class is intended to house data that we want to be persistent.
 */
public class PersistentData {
    private final HashSet<PlayerRecord> playerRecords = new HashSet<>();

    public boolean playerHasRecord(Player player) {
        try {
            getPlayerRecord(player.getUniqueId());
            return true;
        } catch (PlayerRecordNotFoundException e) {
            return false;
        }
    }

    public PlayerRecord getPlayerRecord(UUID playerUUID) throws PlayerRecordNotFoundException {
        for (PlayerRecord playerRecord : playerRecords) {
            if (playerRecord.getPlayerUUID() == playerUUID) {
                return playerRecord;
            }
        }
        throw new PlayerRecordNotFoundException();
    }

    public boolean addPlayerRecord(PlayerRecord playerRecord) {
        return playerRecords.add(playerRecord);
    }

    public boolean removePlayerRecord(UUID playerUUID) {
        try {
            removePlayerRecord(getPlayerRecord(playerUUID));
            return true;
        } catch (PlayerRecordNotFoundException e) {
            return false;
        }
    }

    private void removePlayerRecord(PlayerRecord playerRecord) {
        playerRecords.remove(playerRecord);
    }
}
