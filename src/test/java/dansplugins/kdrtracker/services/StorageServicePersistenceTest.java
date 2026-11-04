package dansplugins.kdrtracker.services;

import dansplugins.kdrtracker.data.PersistentData;
import dansplugins.kdrtracker.exceptions.PlayerRecordNotFoundException;
import dansplugins.kdrtracker.factories.PlayerRecordFactory;
import dansplugins.kdrtracker.objects.PlayerRecord;
import dansplugins.kdrtracker.utils.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * @author Daniel McCoy Stephenson
 *
 * Runs StorageService against ponder's real JsonWriterReader and real files.
 * StorageServiceTest mocks the reader, which is how a load that could never
 * find what save wrote went unnoticed: the reader's writer resolves a name
 * against the folder given to initialize(), and its loader does not.
 */
class StorageServicePersistenceTest {
    @TempDir
    File dataFolder;

    private StorageService storageServiceFor(PersistentData persistentData) {
        Logger logger = new Logger(mock(java.util.logging.Logger.class), () -> false);
        return new StorageService(persistentData, new PlayerRecordFactory(persistentData), logger, dataFolder.getPath() + File.separator);
    }

    private Map<String, String> buildPlayerRecordData(UUID playerUUID, int kills, int deaths) {
        Map<String, String> data = new HashMap<>();
        data.put("playerUUID", playerUUID.toString());
        data.put("kills", "" + kills);
        data.put("deaths", "" + deaths);
        return data;
    }

    @Test
    void save_writesThePlayerRecordsFileInTheDataFolder() {
        PersistentData persistentData = new PersistentData();
        new PlayerRecordFactory(persistentData).createPlayerRecord(buildPlayerRecordData(UUID.randomUUID(), 1, 1));

        storageServiceFor(persistentData).save();

        assertTrue(new File(dataFolder, "playerRecords.json").isFile());
    }

    @Test
    void recordsSavedBeforeARestart_areLoadedAfterIt() throws PlayerRecordNotFoundException {
        UUID playerUUID = UUID.randomUUID();
        PersistentData beforeRestart = new PersistentData();
        new PlayerRecordFactory(beforeRestart).createPlayerRecord(buildPlayerRecordData(playerUUID, 6, 3));
        storageServiceFor(beforeRestart).save();

        PersistentData afterRestart = new PersistentData();
        storageServiceFor(afterRestart).load();

        PlayerRecord record = afterRestart.getPlayerRecord(playerUUID);
        assertEquals(6, record.getKills());
        assertEquals(3, record.getDeaths());
    }

    @Test
    void load_onAFirstStart_withNoFileYet_leavesNoRecords() {
        PersistentData persistentData = new PersistentData();

        storageServiceFor(persistentData).load();

        assertTrue(persistentData.getPlayerRecordData().isEmpty());
    }
}
