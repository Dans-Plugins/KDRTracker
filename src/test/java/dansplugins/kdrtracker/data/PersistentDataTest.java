package dansplugins.kdrtracker.data;

import dansplugins.kdrtracker.exceptions.PlayerRecordNotFoundException;
import dansplugins.kdrtracker.objects.PlayerRecord;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Daniel McCoy Stephenson
 *
 * Regression coverage for player records loaded from storage (e.g. on plugin
 * enable) being found again by value-equal UUIDs, rather than by the same
 * UUID object instance, and characterisation of the add/remove/data/clear
 * surface that JoinListener and StorageService rely on.
 */
class PersistentDataTest {

    private PlayerRecord buildLoadedPlayerRecord(UUID playerUUID) {
        return buildLoadedPlayerRecord(playerUUID, 3, 1);
    }

    private PlayerRecord buildLoadedPlayerRecord(UUID playerUUID, int kills, int deaths) {
        Map<String, String> data = new HashMap<>();
        data.put("playerUUID", playerUUID.toString());
        data.put("kills", "" + kills);
        data.put("deaths", "" + deaths);
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

    @Test
    void playerHasRecord_isFalseWhenNoRecordIsHeld() {
        PersistentData persistentData = new PersistentData();
        assertFalse(persistentData.playerHasRecord(UUID.randomUUID()));
    }

    @Test
    void getPlayerRecord_throwsWhenNoRecordIsHeld() {
        PersistentData persistentData = new PersistentData();
        assertThrows(PlayerRecordNotFoundException.class, () -> persistentData.getPlayerRecord(UUID.randomUUID()));
    }

    @Test
    void addPlayerRecord_returnsTrueAndHoldsTheRecord_whenUUIDIsNew() throws PlayerRecordNotFoundException {
        UUID playerUUID = UUID.randomUUID();
        PersistentData persistentData = new PersistentData();

        assertTrue(persistentData.addPlayerRecord(buildLoadedPlayerRecord(playerUUID)));
        assertEquals(playerUUID, persistentData.getPlayerRecord(playerUUID).getPlayerUUID());
    }

    @Test
    void addPlayerRecord_returnsFalseAndKeepsTheFirstRecord_whenUUIDIsAlreadyHeld() throws PlayerRecordNotFoundException {
        // JoinListener and StorageService#loadPlayerRecords both go through this guard; a
        // duplicate must neither be added alongside nor replace the record already held.
        UUID playerUUID = UUID.randomUUID();
        PersistentData persistentData = new PersistentData();
        PlayerRecord first = buildLoadedPlayerRecord(playerUUID, 3, 1);
        PlayerRecord duplicate = buildLoadedPlayerRecord(UUID.fromString(playerUUID.toString()), 9, 9);
        persistentData.addPlayerRecord(first);

        assertFalse(persistentData.addPlayerRecord(duplicate));
        assertSame(first, persistentData.getPlayerRecord(playerUUID));
        assertEquals(1, persistentData.getPlayerRecordData().size());
    }

    @Test
    void removePlayerRecord_returnsTrueAndForgetsTheRecord_whenHeld() {
        UUID playerUUID = UUID.randomUUID();
        PersistentData persistentData = new PersistentData();
        persistentData.addPlayerRecord(buildLoadedPlayerRecord(playerUUID));

        assertTrue(persistentData.removePlayerRecord(UUID.fromString(playerUUID.toString())));
        assertFalse(persistentData.playerHasRecord(playerUUID));
    }

    @Test
    void removePlayerRecord_returnsFalse_whenNotHeld() {
        PersistentData persistentData = new PersistentData();
        assertFalse(persistentData.removePlayerRecord(UUID.randomUUID()));
    }

    @Test
    void getPlayerRecordData_returnsOneSaveMapPerRecord() {
        // This is what StorageService#savePlayerRecords writes to disk.
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        PersistentData persistentData = new PersistentData();
        persistentData.addPlayerRecord(buildLoadedPlayerRecord(first, 3, 1));
        persistentData.addPlayerRecord(buildLoadedPlayerRecord(second, 0, 4));

        List<Map<String, String>> data = persistentData.getPlayerRecordData();

        assertEquals(2, data.size());
        Map<String, Map<String, String>> byUUID = new HashMap<>();
        for (Map<String, String> record : data) {
            byUUID.put(record.get("playerUUID"), record);
        }
        assertEquals("3", byUUID.get(first.toString()).get("kills"));
        assertEquals("1", byUUID.get(first.toString()).get("deaths"));
        assertEquals("0", byUUID.get(second.toString()).get("kills"));
        assertEquals("4", byUUID.get(second.toString()).get("deaths"));
    }

    @Test
    void getPlayerRecordData_isEmptyWhenNothingIsHeld() {
        assertTrue(new PersistentData().getPlayerRecordData().isEmpty());
    }

    @Test
    void clearPlayerRecords_forgetsEveryRecord() {
        // StorageService#loadPlayerRecords clears before loading so a reload does not double up.
        UUID playerUUID = UUID.randomUUID();
        PersistentData persistentData = new PersistentData();
        persistentData.addPlayerRecord(buildLoadedPlayerRecord(playerUUID));
        persistentData.addPlayerRecord(buildLoadedPlayerRecord(UUID.randomUUID()));

        persistentData.clearPlayerRecords();

        assertFalse(persistentData.playerHasRecord(playerUUID));
        assertTrue(persistentData.getPlayerRecordData().isEmpty());
    }
}
