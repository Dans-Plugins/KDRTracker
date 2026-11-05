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

    private final String FILE_PATH;
    private final String PLAYER_RECORDS_FILE_NAME = "playerRecords.json";

    private boolean loadFailed = false;

    public StorageService(PersistentData persistentData, PlayerRecordFactory playerRecordFactory, Logger logger) {
        this(persistentData, playerRecordFactory, logger, "./plugins/KDRTracker/");
    }

    /**
     * @param filePath The folder the records file is kept in, ending with a separator.
     */
    StorageService(PersistentData persistentData, PlayerRecordFactory playerRecordFactory, Logger logger, String filePath) {
        this.persistentData = persistentData;
        this.playerRecordFactory = playerRecordFactory;
        this.logger = logger;
        this.FILE_PATH = filePath;
        jsonWriterReader.initialize(FILE_PATH);
    }

    public void save() {
        savePlayerRecords();
    }

    /**
     * @return Whether the records were read. False means the file could not be used; see {@link #loadPlayerRecords()}.
     */
    public boolean load() {
        return loadPlayerRecords();
    }

    public void savePlayerRecords() {
        if (loadFailed) {
            logger.error("Not saving player records: " + FILE_PATH + PLAYER_RECORDS_FILE_NAME + " could not be read, and saving would replace it.");
            return;
        }
        List<Map<String, String>> playerRecords = persistentData.getPlayerRecordData();
        jsonWriterReader.writeOutFiles(playerRecords, PLAYER_RECORDS_FILE_NAME);
        logger.log("Saved " + playerRecords.size() + " player records to " + FILE_PATH + PLAYER_RECORDS_FILE_NAME + ".");
    }

    /**
     * Replaces the records in memory with those in the file. A missing file is a first start
     * and leaves no records. A file that cannot be used also leaves no records, and from then
     * on saves are refused, so it stays as it is for the operator to repair.
     * @return Whether the records were read.
     */
    public boolean loadPlayerRecords() {
        persistentData.clearPlayerRecords();

        try {
            // The full path, unlike writeOutFiles: ponder's loader ignores the folder given to
            // initialize(), so a bare name would be looked up in the server's working directory.
            List<Map<String, String>> data = jsonWriterReader.loadDataFromFilename(FILE_PATH + PLAYER_RECORDS_FILE_NAME);

            for (Map<String, String> playerRecordData : data) {
                playerRecordFactory.createPlayerRecord(playerRecordData);
            }
        } catch (RuntimeException e) {
            // Syntax errors arrive from gson, bad counts and UUIDs from PlayerRecord#load, and an
            // empty file ends up here too. A file read in part counts as not read at all.
            persistentData.clearPlayerRecords();
            loadFailed = true;
            logger.error("Could not read " + FILE_PATH + PLAYER_RECORDS_FILE_NAME + ": " + e + ". The file has been left as it is. Repair it, or restore it from a backup, and restart the server.");
            return false;
        }
        loadFailed = false;
        logger.log("Loaded " + persistentData.getPlayerRecordData().size() + " player records from " + FILE_PATH + PLAYER_RECORDS_FILE_NAME + ".");
        return true;
    }

}