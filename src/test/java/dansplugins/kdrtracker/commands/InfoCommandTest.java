package dansplugins.kdrtracker.commands;

import dansplugins.kdrtracker.data.PersistentData;
import dansplugins.kdrtracker.objects.PlayerRecord;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.ChatColor.AQUA;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Daniel McCoy Stephenson
 *
 * Regression coverage for the K/D ratio shown to players being rounded for
 * display, rather than printed at full floating-point precision, and for the
 * replies /kdrt info sends: the stats to a player with a record, and a
 * refusal to the console and to a player with no record.
 */
class InfoCommandTest {
    private PersistentData persistentData;
    private InfoCommand infoCommand;

    @BeforeEach
    void setUp() {
        persistentData = new PersistentData();
        infoCommand = new InfoCommand(persistentData);
    }

    private Player mockPlayer() {
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        return player;
    }

    private void giveRecord(Player player, int kills, int deaths) {
        Map<String, String> data = new HashMap<>();
        data.put("playerUUID", player.getUniqueId().toString());
        data.put("kills", "" + kills);
        data.put("deaths", "" + deaths);
        persistentData.addPlayerRecord(new PlayerRecord(data));
    }

    @Test
    void execute_forAPlayerWithARecord_sendsKillsDeathsAndTheRoundedRatio() {
        Player player = mockPlayer();
        giveRecord(player, 1, 3);

        assertTrue(infoCommand.execute(player));

        InOrder inOrder = inOrder(player);
        inOrder.verify(player).sendMessage(AQUA + "Kills: 1");
        inOrder.verify(player).sendMessage(AQUA + "Deaths: 3");
        inOrder.verify(player).sendMessage(AQUA + "K/D Ratio: 0.33");
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void execute_forAPlayerWithNoDeaths_showsTheKillCountAsTheRatio() {
        Player player = mockPlayer();
        giveRecord(player, 3, 0);

        assertTrue(infoCommand.execute(player));

        verify(player).sendMessage(AQUA + "K/D Ratio: 3.00");
    }

    @Test
    void execute_forAPlayerWithNoRecord_saysTheRecordWasNotFound() {
        Player player = mockPlayer();

        assertFalse(infoCommand.execute(player));

        verify(player).sendMessage("Your player record wasn't found.");
        verify(player, times(1)).sendMessage(anyString());
    }

    @Test
    void execute_fromTheConsole_saysTheCommandIsForPlayers() {
        CommandSender console = mock(CommandSender.class);

        assertFalse(infoCommand.execute(console));

        verify(console).sendMessage("This command is intended for use by players.");
        verify(console, times(1)).sendMessage(anyString());
    }

    @Test
    void execute_withArguments_ignoresThemAndSendsTheStats() {
        Player player = mockPlayer();
        giveRecord(player, 2, 1);

        assertTrue(infoCommand.execute(player, new String[]{"someone-else"}));

        verify(player).sendMessage(AQUA + "Kills: 2");
        verify(player).sendMessage(AQUA + "Deaths: 1");
        verify(player).sendMessage(AQUA + "K/D Ratio: 2.00");
    }

    @Test
    void names_areInfoAndView() {
        assertEquals(List.of("info", "view"), infoCommand.getNames());
    }

    @Test
    void permissions_areKdrtInfoAndKdrtView() {
        assertEquals(List.of("kdrt.info", "kdrt.view"), infoCommand.getPermissions());
    }

    @Test
    void formatRatio_roundsRecurringDecimalDown() {
        assertEquals("0.33", InfoCommand.formatRatio(1.0 / 3.0));
    }

    @Test
    void formatRatio_roundsRecurringDecimalUp() {
        assertEquals("0.67", InfoCommand.formatRatio(2.0 / 3.0));
    }

    @Test
    void formatRatio_padsWholeNumbersToTwoPlaces() {
        assertEquals("5.00", InfoCommand.formatRatio(5.0));
    }

    @Test
    void formatRatio_formatsZero() {
        assertEquals("0.00", InfoCommand.formatRatio(0.0));
    }

    @Test
    void formatRatio_usesAPeriodAsTheDecimalSeparatorRegardlessOfTheDefaultLocale() {
        Locale defaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            assertEquals("1.50", InfoCommand.formatRatio(1.5));
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }
}
