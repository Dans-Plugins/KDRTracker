package dansplugins.kdrtracker.objects;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Daniel McCoy Stephenson
 *
 * Regression coverage for PlayerRecord's kill/death accounting and ratio math.
 */
class PlayerRecordTest {

    private PlayerRecord buildPlayerRecord(UUID playerUUID, int kills, int deaths) {
        Map<String, String> data = new HashMap<>();
        data.put("playerUUID", playerUUID.toString());
        data.put("kills", "" + kills);
        data.put("deaths", "" + deaths);
        return new PlayerRecord(data);
    }

    @Test
    void getRatio_returnsKillsAsWholeNumber_whenDeathsIsZero() {
        PlayerRecord playerRecord = buildPlayerRecord(UUID.randomUUID(), 5, 0);
        assertEquals(5.0, playerRecord.getRatio());
    }

    @Test
    void getRatio_returnsZero_whenKillsAndDeathsAreBothZero() {
        PlayerRecord playerRecord = buildPlayerRecord(UUID.randomUUID(), 0, 0);
        assertEquals(0.0, playerRecord.getRatio());
    }

    @Test
    void getRatio_returnsQuotient_whenDeathsIsNonZero() {
        PlayerRecord playerRecord = buildPlayerRecord(UUID.randomUUID(), 3, 2);
        assertEquals(1.5, playerRecord.getRatio());
    }

    @Test
    void incrementKills_increasesKillsByOne() {
        PlayerRecord playerRecord = buildPlayerRecord(UUID.randomUUID(), 0, 0);
        playerRecord.incrementKills();
        assertEquals(1, playerRecord.getKills());
    }

    @Test
    void incrementDeaths_increasesDeathsByOne() {
        PlayerRecord playerRecord = buildPlayerRecord(UUID.randomUUID(), 0, 0);
        playerRecord.incrementDeaths();
        assertEquals(1, playerRecord.getDeaths());
    }

    @Test
    void save_roundTripsThroughLoad() {
        UUID playerUUID = UUID.randomUUID();
        PlayerRecord playerRecord = buildPlayerRecord(playerUUID, 4, 2);

        PlayerRecord reloaded = new PlayerRecord(playerRecord.save());

        assertEquals(playerUUID, reloaded.getPlayerUUID());
        assertEquals(4, reloaded.getKills());
        assertEquals(2, reloaded.getDeaths());
    }

    @Test
    void getRatio_returnsZero_whenKillsIsZeroAndDeathsIsNonZero() {
        PlayerRecord playerRecord = buildPlayerRecord(UUID.randomUUID(), 0, 4);
        assertEquals(0.0, playerRecord.getRatio());
    }

    @Test
    void getRatio_isNotRounded() {
        // Rounding to two places is InfoCommand#formatRatio's job, not the record's.
        PlayerRecord playerRecord = buildPlayerRecord(UUID.randomUUID(), 1, 3);
        assertEquals(1.0 / 3.0, playerRecord.getRatio());
    }

    @Test
    void incrementKills_andIncrementDeaths_buildOnTheLoadedCounts() {
        PlayerRecord playerRecord = buildPlayerRecord(UUID.randomUUID(), 7, 3);

        playerRecord.incrementKills();
        playerRecord.incrementDeaths();

        assertEquals(8, playerRecord.getKills());
        assertEquals(4, playerRecord.getDeaths());
        assertEquals(2.0, playerRecord.getRatio());
    }

    @Test
    void save_writesExactlyTheUUIDAndBothCountsAsDecimalStrings() {
        // These keys and formats are what playerRecords.json holds, so a change here changes the file.
        UUID playerUUID = UUID.fromString("0f8fad5b-d9cb-469f-a165-70867728950e");
        PlayerRecord playerRecord = buildPlayerRecord(playerUUID, 12, 5);

        Map<String, String> expected = new HashMap<>();
        expected.put("playerUUID", "0f8fad5b-d9cb-469f-a165-70867728950e");
        expected.put("kills", "12");
        expected.put("deaths", "5");
        assertEquals(expected, playerRecord.save());
    }

    @Test
    void load_ignoresKeysItDoesNotKnow() {
        UUID playerUUID = UUID.randomUUID();
        Map<String, String> data = buildPlayerRecord(playerUUID, 2, 1).save();
        data.put("somethingElse", "ignored");

        PlayerRecord playerRecord = new PlayerRecord(data);

        assertEquals(playerUUID, playerRecord.getPlayerUUID());
        assertEquals(2, playerRecord.getKills());
        assertEquals(1, playerRecord.getDeaths());
        assertFalse(playerRecord.save().containsKey("somethingElse"));
    }

    // The failures below are RuntimeExceptions on purpose: StorageService#loadPlayerRecords
    // catches RuntimeException to refuse a playerRecords.json it cannot use, so an entry that
    // threw a checked exception, or did not throw at all, would get past that guard.

    @Test
    void load_throwsNumberFormatException_whenACountIsNotANumber() {
        Map<String, String> data = buildPlayerRecord(UUID.randomUUID(), 0, 0).save();
        data.put("kills", "three");
        assertThrows(NumberFormatException.class, () -> new PlayerRecord(data));
    }

    @Test
    void load_throwsNumberFormatException_whenACountDoesNotFitAnInt() {
        Map<String, String> data = buildPlayerRecord(UUID.randomUUID(), 0, 0).save();
        data.put("deaths", "2147483648");
        assertThrows(NumberFormatException.class, () -> new PlayerRecord(data));
    }

    @Test
    void load_throwsNumberFormatException_whenACountIsMissing() {
        Map<String, String> data = buildPlayerRecord(UUID.randomUUID(), 0, 0).save();
        data.remove("deaths");
        assertThrows(NumberFormatException.class, () -> new PlayerRecord(data));
    }

    @Test
    void load_throwsIllegalArgumentException_whenThePlayerUUIDIsMalformed() {
        Map<String, String> data = buildPlayerRecord(UUID.randomUUID(), 0, 0).save();
        data.put("playerUUID", "not-a-uuid");
        assertThrows(IllegalArgumentException.class, () -> new PlayerRecord(data));
    }

    @Test
    void load_throwsNullPointerException_whenThePlayerUUIDIsMissing() {
        Map<String, String> data = buildPlayerRecord(UUID.randomUUID(), 0, 0).save();
        data.remove("playerUUID");
        assertThrows(NullPointerException.class, () -> new PlayerRecord(data));
    }

    @Test
    void load_acceptsANegativeCount() {
        // Current behaviour, recorded rather than endorsed: nothing rejects a negative count, so
        // a hand-edited "-2" loads as is and yields a negative ratio.
        Map<String, String> data = buildPlayerRecord(UUID.randomUUID(), 0, 1).save();
        data.put("kills", "-2");

        PlayerRecord playerRecord = new PlayerRecord(data);

        assertEquals(-2, playerRecord.getKills());
        assertEquals(-2.0, playerRecord.getRatio());
    }
}
