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
}
