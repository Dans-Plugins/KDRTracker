package dansplugins.kdrtracker.data;

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
 * Regression coverage for player records loaded from storage (e.g. on plugin
 * enable) being found again by value-equal UUIDs, rather than by the same
 * UUID object instance.
 */
class PersistentDataTest {

    private PlayerRecord buildLoadedPlayerRecord(UUID playerUUID) {
        Map<String, String> data = new HashMap<>();
        data.put("playerUUID", playerUUID.toString());
        data.put("kills", "3");
        data.put("deaths", "1");
        return new PlayerRecord(data);
    }

    @Test
    void getPlayerRecord_findsRecordByEqualButDistinctUUIDInstance() throws PlayerRecordNotFoundException {
        UUID playerUUID = UUID.randomUUID();
        PersistentData persistentData = new PersistentData();
        persistentData.addPlayerRecord(buildLoadedPlayerRecord(playerUUID));

        // Simulate a freshly-parsed UUID instance, as would come from a new
        // Player object on rejoin after the record was loaded from disk.
        UUID lookupUUID = UUID.fromString(playerUUID.toString());
        assertNotSame(playerUUID, lookupUUID, "test setup should use a distinct UUID instance");

        PlayerRecord found = persistentData.getPlayerRecord(lookupUUID);
        assertEquals(playerUUID, found.getPlayerUUID());
        assertEquals(3, found.getKills());
        assertEquals(1, found.getDeaths());
    }

    @Test
    void playerHasRecord_isTrueForEqualButDistinctUUIDInstance() {
        UUID playerUUID = UUID.randomUUID();
        PersistentData persistentData = new PersistentData();
        persistentData.addPlayerRecord(buildLoadedPlayerRecord(playerUUID));

        UUID lookupUUID = UUID.fromString(playerUUID.toString());
        assertTrue(persistentData.playerHasRecord(lookupUUID));
    }
}
