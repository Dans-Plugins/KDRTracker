package dansplugins.kdrtracker.services;

import dansplugins.kdrtracker.KDRTracker;
import dansplugins.kdrtracker.data.PersistentData;
import dansplugins.kdrtracker.factories.PlayerRecordFactory;
import preponderous.ponder.misc.JsonWriterReader;

import java.util.List;
import java.util.Map;

/**
 * @author Daniel McCoy Stephenson
 * @since June 19th, 2022
 *
 * This class is intended to handle the storage of data for the plugin.
 */
public class StorageService {
    private final ConfigService configService;
    private final KDRTracker kdrTracker;
    private final PersistentData persistentData;
    private final PlayerRecordFactory playerRecordFactory;

    private final JsonWriterReader jsonWriterReader = new JsonWriterReader();

    private final String FILE_PATH = "./plugins/KDRTracker/";
    private final String PLAYER_RECORDS_FILE_NAME = "playerRecords.json";

    public StorageService(ConfigService configService, KDRTracker kdrTracker, PersistentData persistentData, PlayerRecordFactory playerRecordFactory) {
        this.configService = configService;
        this.kdrTracker = kdrTracker;
        this.persistentData = persistentData;
        this.playerRecordFactory = playerRecordFactory;
        jsonWriterReader.initialize(FILE_PATH);
    }

    public void save() {
        savePlayerRecords();
        if (configService.hasBeenAltered()) {
            kdrTracker.saveConfig();
        }
    }

    public void load() {
        loadPlayerRecords();
    }

    public void savePlayerRecords() {
        List<Map<String, String>> playerRecords = persistentData.getPlayerRecordData();
        jsonWriterReader.writeOutFiles(playerRecords, PLAYER_RECORDS_FILE_NAME);
    }

    public void loadPlayerRecords() {
        persistentData.clearPlayerRecords();

        List<Map<String, String>> data = jsonWriterReader.loadDataFromFilename(PLAYER_RECORDS_FILE_NAME);

        for (Map<String, String> playerRecordData : data) {
            playerRecordFactory.createPlayerRecord(playerRecordData);
        }
    }

}