package dansplugins.kdrtracker.services;

import dansplugins.kdrtracker.data.PersistentData;
import dansplugins.kdrtracker.factories.PlayerRecordFactory;
import dansplugins.kdrtracker.utils.Logger;
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
    private final PersistentData persistentData;
    private final PlayerRecordFactory playerRecordFactory;
    private final Logger logger;

    private final JsonWriterReader jsonWriterReader = new JsonWriterReader();

    private final String FILE_PATH = "./plugins/KDRTracker/";
    private final String PLAYER_RECORDS_FILE_NAME = "playerRecords.json";

    public StorageService(PersistentData persistentData, PlayerRecordFactory playerRecordFactory, Logger logger) {
        this.persistentData = persistentData;
        this.playerRecordFactory = playerRecordFactory;
        this.logger = logger;
        jsonWriterReader.initialize(FILE_PATH);
    }

    public void save() {
        savePlayerRecords();
    }

    public void load() {
        loadPlayerRecords();
    }

    public void savePlayerRecords() {
        List<Map<String, String>> playerRecords = persistentData.getPlayerRecordData();
        jsonWriterReader.writeOutFiles(playerRecords, PLAYER_RECORDS_FILE_NAME);
        logger.log("Saved " + playerRecords.size() + " player records to " + FILE_PATH + PLAYER_RECORDS_FILE_NAME + ".");
    }

    public void loadPlayerRecords() {
        persistentData.clearPlayerRecords();

        List<Map<String, String>> data = jsonWriterReader.loadDataFromFilename(PLAYER_RECORDS_FILE_NAME);

        for (Map<String, String> playerRecordData : data) {
            playerRecordFactory.createPlayerRecord(playerRecordData);
        }
        logger.log("Loaded " + persistentData.getPlayerRecordData().size() + " player records from " + FILE_PATH + PLAYER_RECORDS_FILE_NAME + ".");
    }

}