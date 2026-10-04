package dansplugins.kdrtracker.listeners;

import dansplugins.kdrtracker.data.PersistentData;
import dansplugins.kdrtracker.exceptions.PlayerRecordNotFoundException;
import dansplugins.kdrtracker.objects.PlayerRecord;
import dansplugins.kdrtracker.utils.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Daniel McCoy Stephenson
 *
 * Characterises how a PlayerDeathEvent is turned into kill and death counts:
 * every death is counted against the victim, a kill is counted only when the
 * victim has a player killer, and a party with no record is skipped.
 */
class DeathListenerTest {
    private PersistentData persistentData;
    private DeathListener deathListener;

    @BeforeEach
    void setUp() {
        persistentData = new PersistentData();
        deathListener = new DeathListener(persistentData, new Logger(java.util.logging.Logger.getAnonymousLogger(), () -> false));
    }

    private Player mockPlayer() {
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("player");
        return player;
    }

    private PlayerRecord giveRecord(Player player) throws PlayerRecordNotFoundException {
        persistentData.addPlayerRecord(new PlayerRecord(player));
        return persistentData.getPlayerRecord(player.getUniqueId());
    }

    private void die(Player victim) {
        deathListener.onPlayerDeath(new PlayerDeathEvent(victim, new ArrayList<>(), 0, "died"));
    }

    @Test
    void onPlayerDeath_withoutAKiller_countsOnlyTheVictimsDeath() throws PlayerRecordNotFoundException {
        Player victim = mockPlayer();
        PlayerRecord victimsRecord = giveRecord(victim);

        die(victim);

        assertEquals(1, victimsRecord.getDeaths());
        assertEquals(0, victimsRecord.getKills());
        verify(victim).sendMessage("You now have 1 death.");
    }

    @Test
    void onPlayerDeath_withAPlayerKiller_countsTheDeathAndTheKill() throws PlayerRecordNotFoundException {
        Player victim = mockPlayer();
        Player killer = mockPlayer();
        when(victim.getKiller()).thenReturn(killer);
        PlayerRecord victimsRecord = giveRecord(victim);
        PlayerRecord killersRecord = giveRecord(killer);

        die(victim);

        assertEquals(1, victimsRecord.getDeaths());
        assertEquals(0, victimsRecord.getKills());
        assertEquals(1, killersRecord.getKills());
        assertEquals(0, killersRecord.getDeaths());
        verify(victim).sendMessage("You now have 1 death.");
        verify(killer).sendMessage("You now have 1 kill.");
    }

    @Test
    void onPlayerDeath_messagesCarryTheNewTotals() throws PlayerRecordNotFoundException {
        Player victim = mockPlayer();
        Player killer = mockPlayer();
        when(victim.getKiller()).thenReturn(killer);
        giveRecord(victim);
        giveRecord(killer);

        die(victim);
        die(victim);

        verify(victim).sendMessage("You now have 2 deaths.");
        verify(killer).sendMessage("You now have 2 kills.");
    }

    @Test
    void onPlayerDeath_whenTheVictimHasNoRecord_skipsTheDeathButStillCountsTheKill() throws PlayerRecordNotFoundException {
        Player victim = mockPlayer();
        Player killer = mockPlayer();
        when(victim.getKiller()).thenReturn(killer);
        PlayerRecord killersRecord = giveRecord(killer);

        assertDoesNotThrow(() -> die(victim));

        assertFalse(persistentData.playerHasRecord(victim));
        assertEquals(1, killersRecord.getKills());
        verify(victim, never()).sendMessage(anyString());
    }

    @Test
    void onPlayerDeath_whenTheKillerHasNoRecord_skipsTheKillButStillCountsTheDeath() throws PlayerRecordNotFoundException {
        Player victim = mockPlayer();
        Player killer = mockPlayer();
        when(victim.getKiller()).thenReturn(killer);
        PlayerRecord victimsRecord = giveRecord(victim);

        assertDoesNotThrow(() -> die(victim));

        assertFalse(persistentData.playerHasRecord(killer));
        assertEquals(1, victimsRecord.getDeaths());
        verify(killer, never()).sendMessage(anyString());
    }

    @Test
    void countOf_usesTheSingularOnlyForOne() {
        assertEquals("0 kills", DeathListener.countOf(0, "kill", "kills"));
        assertEquals("1 kill", DeathListener.countOf(1, "kill", "kills"));
        assertEquals("2 kills", DeathListener.countOf(2, "kill", "kills"));
        assertEquals("1 death", DeathListener.countOf(1, "death", "deaths"));
    }
}
