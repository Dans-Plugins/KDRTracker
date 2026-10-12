package dansplugins.kdrtracker.services;

import dansplugins.kdrtracker.data.PersistentData;
import dansplugins.kdrtracker.exceptions.PlayerRecordNotFoundException;
import dansplugins.kdrtracker.factories.PlayerRecordFactory;
import dansplugins.kdrtracker.objects.PlayerRecord;
import dansplugins.kdrtracker.utils.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import preponderous.ponder.misc.JsonWriterReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Daniel McCoy Stephenson
 *
 * Pins how player records travel between PersistentData and
 * playerRecords.json. ponder's JsonWriterReader is replaced at construction,
 * so nothing is read from or written to ./plugins/KDRTracker/.
 */
class StorageServiceTest {
    private static final String PLAYER_RECORDS_FILE_NAME = "playerRecords.json";

    private MockedConstruction<JsonWriterReader> jsonWriterReaders;
    private PersistentData persistentData;
    private java.util.logging.Logger output;
    private StorageService storageService;

    @BeforeEach
    void setUp() {
        jsonWriterReaders = mockConstruction(JsonWriterReader.class);
        persistentData = new PersistentData();
        output = mock(java.util.logging.Logger.class);
        storageService = new StorageService(persistentData, new PlayerRecordFactory(persistentData), new Logger(output, () -> true));
    }

    @AfterEach
    void tearDown() {
        jsonWriterReaders.close();
    }

    private JsonWriterReader jsonWriterReader() {
        assertEquals(1, jsonWriterReaders.constructed().size());
        return jsonWriterReaders.constructed().get(0);
    }

    private Map<String, String> buildPlayerRecordData(UUID playerUUID, int kills, int deaths) {
        Map<String, String> data = new HashMap<>();
        data.put("playerUUID", playerUUID.toString());
        data.put("kills", "" + kills);
        data.put("deaths", "" + deaths);
        return data;
    }

    private void givenTheFileHolds(List<Map<String, String>> data) {
        doReturn(data).when(jsonWriterReader()).loadDataFromFilename(PLAYER_RECORDS_FILE_NAME);
    }

    @Test
    void constructor_pointsTheReaderAtThePluginsDataFolder() {
        verify(jsonWriterReader()).initialize("./plugins/KDRTracker/");
    }

    @Test
    void load_addsARecordForEveryEntryInTheFile() throws PlayerRecordNotFoundException {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        List<Map<String, String>> data = new ArrayList<>();
        data.add(buildPlayerRecordData(first, 4, 2));
        data.add(buildPlayerRecordData(second, 0, 7));
        givenTheFileHolds(data);

        storageService.load();

        PlayerRecord firstRecord = persistentData.getPlayerRecord(first);
        assertEquals(4, firstRecord.getKills());
        assertEquals(2, firstRecord.getDeaths());
        PlayerRecord secondRecord = persistentData.getPlayerRecord(second);
        assertEquals(0, secondRecord.getKills());
        assertEquals(7, secondRecord.getDeaths());
        assertEquals(2, persistentData.getPlayerRecordData().size());
        verify(output).info("[DEBUG] Loaded 2 player records from ./plugins/KDRTracker/playerRecords.json.");
    }

    @Test
    void load_discardsRecordsHeldBeforeTheLoad() {
        // Loading replaces what is in memory rather than merging into it: a record held only in
        // memory is gone afterwards, and a record also in the file takes the file's counts.
        UUID onlyInMemory = UUID.randomUUID();
        UUID inBoth = UUID.randomUUID();
        PlayerRecordFactory factory = new PlayerRecordFactory(persistentData);
        factory.createPlayerRecord(buildPlayerRecordData(onlyInMemory, 1, 1));
        factory.createPlayerRecord(buildPlayerRecordData(inBoth, 9, 9));
        List<Map<String, String>> data = new ArrayList<>();
        data.add(buildPlayerRecordData(inBoth, 3, 5));
        givenTheFileHolds(data);

        storageService.load();

        assertFalse(persistentData.playerHasRecord(onlyInMemory));
        assertEquals(1, persistentData.getPlayerRecordData().size());
        assertEquals(buildPlayerRecordData(inBoth, 3, 5), persistentData.getPlayerRecordData().get(0));
    }

    @Test
    void load_keepsTheFirstOfTwoEntriesForTheSameUUID() throws PlayerRecordNotFoundException {
        UUID playerUUID = UUID.randomUUID();
        List<Map<String, String>> data = new ArrayList<>();
        data.add(buildPlayerRecordData(playerUUID, 4, 2));
        data.add(buildPlayerRecordData(playerUUID, 8, 8));
        givenTheFileHolds(data);

        storageService.load();

        assertEquals(4, persistentData.getPlayerRecord(playerUUID).getKills());
        assertEquals(1, persistentData.getPlayerRecordData().size());
        verify(output).info("[DEBUG] Loaded 1 player records from ./plugins/KDRTracker/playerRecords.json.");
    }

    @Test
    void load_withAnEmptyFile_leavesNoRecords() {
        new PlayerRecordFactory(persistentData).createPlayerRecord(buildPlayerRecordData(UUID.randomUUID(), 1, 1));
        givenTheFileHolds(new ArrayList<>());

        storageService.load();

        assertTrue(persistentData.getPlayerRecordData().isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void save_writesEveryRecordInMemoryToThePlayerRecordsFile() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        PlayerRecordFactory factory = new PlayerRecordFactory(persistentData);
        factory.createPlayerRecord(buildPlayerRecordData(first, 4, 2));
        factory.createPlayerRecord(buildPlayerRecordData(second, 0, 7));

        storageService.save();

        ArgumentCaptor<List<Map<String, String>>> written = ArgumentCaptor.forClass(List.class);
        verify(jsonWriterReader()).writeOutFiles(written.capture(), eq(PLAYER_RECORDS_FILE_NAME));
        assertEquals(2, written.getValue().size());
        assertTrue(written.getValue().contains(buildPlayerRecordData(first, 4, 2)));
        assertTrue(written.getValue().contains(buildPlayerRecordData(second, 0, 7)));
        verify(output).info("[DEBUG] Saved 2 player records to ./plugins/KDRTracker/playerRecords.json.");
    }

    @Test
    @SuppressWarnings("unchecked")
    void save_withNoRecords_writesAnEmptyList() {
        storageService.save();

        ArgumentCaptor<List<Map<String, String>>> written = ArgumentCaptor.forClass(List.class);
        verify(jsonWriterReader()).writeOutFiles(written.capture(), eq(PLAYER_RECORDS_FILE_NAME));
        assertTrue(written.getValue().isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void save_thenLoad_restoresTheSameCounts() throws PlayerRecordNotFoundException {
        UUID playerUUID = UUID.randomUUID();
        new PlayerRecordFactory(persistentData).createPlayerRecord(buildPlayerRecordData(playerUUID, 6, 3));
        storageService.save();
        ArgumentCaptor<List<Map<String, String>>> written = ArgumentCaptor.forClass(List.class);
        verify(jsonWriterReader()).writeOutFiles(written.capture(), eq(PLAYER_RECORDS_FILE_NAME));
        persistentData.clearPlayerRecords();
        givenTheFileHolds(written.getValue());

        storageService.load();

        PlayerRecord record = persistentData.getPlayerRecord(playerUUID);
        assertEquals(6, record.getKills());
        assertEquals(3, record.getDeaths());
    }
}
