package dansplugins.kdrtracker.services;

import dansplugins.kdrtracker.data.PersistentData;
import dansplugins.kdrtracker.exceptions.PlayerRecordNotFoundException;
import dansplugins.kdrtracker.factories.PlayerRecordFactory;
import dansplugins.kdrtracker.objects.PlayerRecord;
import dansplugins.kdrtracker.utils.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @ParameterizedTest
    @ValueSource(strings = {
            "[{\"playerUUID\":\"6",
            "not json",
            "null",
            "",
            "[{\"playerUUID\":\"00000000-0000-0000-0000-000000000001\",\"kills\":\"six\",\"deaths\":\"3\"}]",
            "[{\"playerUUID\":\"00000000-0000-0000-0000-000000000001\"}]",
            "[{\"playerUUID\":\"not-a-uuid\",\"kills\":\"6\",\"deaths\":\"3\"}]"
    })
    void aFileThatCannotBeUsed_failsTheLoad_andLaterSavesLeaveItAlone(String contents) throws IOException {
        Path file = new File(dataFolder, "playerRecords.json").toPath();
        Files.write(file, contents.getBytes(StandardCharsets.UTF_8));
        PersistentData persistentData = new PersistentData();
        StorageService storageService = storageServiceFor(persistentData);

        assertFalse(storageService.load());
        assertTrue(persistentData.getPlayerRecordData().isEmpty());

        storageService.save();

        assertEquals(contents, new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
    }

    @Test
    void aBadEntryAfterGoodOnes_leavesNoneOfTheRecordsInMemory() throws IOException {
        Path file = new File(dataFolder, "playerRecords.json").toPath();
        String contents = "[{\"playerUUID\":\"" + UUID.randomUUID() + "\",\"kills\":\"6\",\"deaths\":\"3\"},"
                + "{\"playerUUID\":\"" + UUID.randomUUID() + "\",\"kills\":\"six\",\"deaths\":\"3\"}]";
        Files.write(file, contents.getBytes(StandardCharsets.UTF_8));
        PersistentData persistentData = new PersistentData();

        assertFalse(storageServiceFor(persistentData).load());

        assertTrue(persistentData.getPlayerRecordData().isEmpty());
    }

    @Test
    void aFileRepairedBeforeTheNextLoad_isReadAndSavedAgain() throws IOException, PlayerRecordNotFoundException {
        Path file = new File(dataFolder, "playerRecords.json").toPath();
        Files.write(file, "not json".getBytes(StandardCharsets.UTF_8));
        UUID playerUUID = UUID.randomUUID();
        PersistentData persistentData = new PersistentData();
        StorageService storageService = storageServiceFor(persistentData);
        assertFalse(storageService.load());

        Files.write(file, ("[{\"playerUUID\":\"" + playerUUID + "\",\"kills\":\"6\",\"deaths\":\"3\"}]").getBytes(StandardCharsets.UTF_8));

        assertTrue(storageService.load());
        assertEquals(6, persistentData.getPlayerRecord(playerUUID).getKills());
        persistentData.getPlayerRecord(playerUUID).incrementKills();
        storageService.save();
        assertTrue(new String(Files.readAllBytes(file), StandardCharsets.UTF_8).contains("\"7\""));
    }
}
