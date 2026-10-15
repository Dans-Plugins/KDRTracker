package dansplugins.kdrtracker.commands;

import dansplugins.kdrtracker.KDRTracker;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;

import static org.bukkit.ChatColor.AQUA;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Daniel McCoy Stephenson
 *
 * Characterises what a bare /kdrt shows: the plugin version as reported by
 * KDRTracker#getVersion, the developer and the wiki link, in that order.
 */
class DefaultCommandTest {
    private KDRTracker kdrTracker;

    @BeforeEach
    void setUp() {
        kdrTracker = mock(KDRTracker.class);
        when(kdrTracker.getVersion()).thenReturn("v9.8.7-test");
    }

    private void assertPluginInfoSentTo(CommandSender sender) {
        InOrder inOrder = inOrder(sender);
        inOrder.verify(sender).sendMessage(AQUA + "KDRTracker v9.8.7-test");
        inOrder.verify(sender).sendMessage(AQUA + "Developed by: Daniel Stephenson");
        inOrder.verify(sender).sendMessage(AQUA + "Wiki: https://github.com/Dans-Plugins/KDRTracker/wiki");
        verifyNoMoreInteractions(sender);
    }

    @Test
    void execute_sendsVersionDeveloperAndWikiLink() {
        CommandSender sender = mock(CommandSender.class);

        assertTrue(new DefaultCommand(kdrTracker).execute(sender));

        assertPluginInfoSentTo(sender);
    }

    @Test
    void execute_withArguments_ignoresThemAndSendsTheSameInfo() {
        CommandSender sender = mock(CommandSender.class);

        assertTrue(new DefaultCommand(kdrTracker).execute(sender, new String[]{"extra"}));

        assertPluginInfoSentTo(sender);
    }

    @Test
    void permissions_areNonEmptySoPonderWouldNotDenyEveryone() {
        // kdrt.default is vestigial (see DefaultCommand's constructor), but PermissionChecker
        // denies a sender outright when a command carries no permissions at all.
        assertEquals(List.of("kdrt.default"), new DefaultCommand(kdrTracker).getPermissions());
    }
}
