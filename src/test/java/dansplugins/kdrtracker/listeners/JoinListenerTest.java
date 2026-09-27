package dansplugins.kdrtracker.listeners;

import dansplugins.kdrtracker.data.PersistentData;
import dansplugins.kdrtracker.exceptions.PlayerRecordNotFoundException;
import dansplugins.kdrtracker.factories.PlayerRecordFactory;
import dansplugins.kdrtracker.objects.PlayerRecord;
import dansplugins.kdrtracker.utils.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Daniel McCoy Stephenson
 *
 * Characterises record creation on join: a first join creates a record, and
 * a rejoin leaves the existing record, and its counts, in place.
 */
class JoinListenerTest {
    private PersistentData persistentData;
    private JoinListener joinListener;

    @BeforeEach
    void setUp() {
        persistentData = new PersistentData();
        joinListener = new JoinListener(persistentData, new PlayerRecordFactory(persistentData),
                new Logger(java.util.logging.Logger.getAnonymousLogger(), () -> false));
    }

    private Player mockPlayer(UUID playerUUID) {
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerUUID);
        when(player.getName()).thenReturn("player");
        return player;
    }

    private void join(Player player) {
        joinListener.onPlayerJoin(new PlayerJoinEvent(player, "joined"));
    }

    @Test
    void onPlayerJoin_firstJoin_createsAnEmptyRecord() throws PlayerRecordNotFoundException {
        UUID playerUUID = UUID.randomUUID();

        join(mockPlayer(playerUUID));

        PlayerRecord record = persistentData.getPlayerRecord(playerUUID);
        assertEquals(0, record.getKills());
        assertEquals(0, record.getDeaths());
    }

    @Test
    void onPlayerJoin_rejoin_keepsTheExistingRecord() throws PlayerRecordNotFoundException {
        UUID playerUUID = UUID.randomUUID();
        join(mockPlayer(playerUUID));
        PlayerRecord first = persistentData.getPlayerRecord(playerUUID);
        first.incrementKills();

        // A rejoin arrives as a new Player object carrying an equal but distinct UUID.
        join(mockPlayer(UUID.fromString(playerUUID.toString())));

        assertSame(first, persistentData.getPlayerRecord(playerUUID));
        assertEquals(1, persistentData.getPlayerRecord(playerUUID).getKills());
        assertEquals(1, persistentData.getPlayerRecordData().size());
    }
}
