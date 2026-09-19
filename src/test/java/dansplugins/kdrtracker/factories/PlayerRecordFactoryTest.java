package dansplugins.kdrtracker.factories;

import dansplugins.kdrtracker.data.PersistentData;
import dansplugins.kdrtracker.exceptions.PlayerRecordNotFoundException;
import dansplugins.kdrtracker.objects.PlayerRecord;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Daniel McCoy Stephenson
 *
 * Characterisation coverage for the map-driven factory path, which is how
 * every record reaches PersistentData on plugin enable
 * (StorageService#loadPlayerRecords).
 */
class PlayerRecordFactoryTest {

    private Map<String, String> buildPlayerRecordData(UUID playerUUID, int kills, int deaths) {
        Map<String, String> data = new HashMap<>();
        data.put("playerUUID", playerUUID.toString());
        data.put("kills", "" + kills);
        data.put("deaths", "" + deaths);
        return data;
    }

    @Test
    void createPlayerRecord_fromMap_addsARecordCarryingTheMapsValues() throws PlayerRecordNotFoundException {
        UUID playerUUID = UUID.randomUUID();
        PersistentData persistentData = new PersistentData();
        PlayerRecordFactory factory = new PlayerRecordFactory(persistentData);

        factory.createPlayerRecord(buildPlayerRecordData(playerUUID, 4, 2));

        PlayerRecord record = persistentData.getPlayerRecord(playerUUID);
        assertEquals(playerUUID, record.getPlayerUUID());
        assertEquals(4, record.getKills());
        assertEquals(2, record.getDeaths());
    }

    @Test
    void createPlayerRecord_fromMap_doesNotReplaceARecordAlreadyHeldForThatUUID() throws PlayerRecordNotFoundException {
        // A second entry for the same UUID in playerRecords.json is dropped, not merged or
        // substituted: the first one loaded wins, exactly as PersistentData#addPlayerRecord guards.
        UUID playerUUID = UUID.randomUUID();
        PersistentData persistentData = new PersistentData();
        PlayerRecordFactory factory = new PlayerRecordFactory(persistentData);

        factory.createPlayerRecord(buildPlayerRecordData(playerUUID, 4, 2));
        factory.createPlayerRecord(buildPlayerRecordData(playerUUID, 9, 9));

        PlayerRecord record = persistentData.getPlayerRecord(playerUUID);
        assertEquals(4, record.getKills());
        assertEquals(2, record.getDeaths());
        assertEquals(1, persistentData.getPlayerRecordData().size());
    }

    @Test
    void createPlayerRecord_fromMap_createsOneRecordPerDistinctUUID() {
        PersistentData persistentData = new PersistentData();
        PlayerRecordFactory factory = new PlayerRecordFactory(persistentData);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        factory.createPlayerRecord(buildPlayerRecordData(first, 1, 0));
        factory.createPlayerRecord(buildPlayerRecordData(second, 0, 1));

        assertTrue(persistentData.playerHasRecord(first));
        assertTrue(persistentData.playerHasRecord(second));
        assertEquals(2, persistentData.getPlayerRecordData().size());
    }
}
