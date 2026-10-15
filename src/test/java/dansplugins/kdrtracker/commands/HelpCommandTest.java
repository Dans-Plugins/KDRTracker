package dansplugins.kdrtracker.commands;

import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;

import static org.bukkit.ChatColor.AQUA;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Daniel McCoy Stephenson
 *
 * Characterises the help listing: the two sub-commands ponder dispatches, in
 * order, whether or not arguments follow "help".
 */
class HelpCommandTest {

    private void assertHelpListingSentTo(CommandSender sender) {
        InOrder inOrder = inOrder(sender);
        inOrder.verify(sender).sendMessage(AQUA + "/kdrt help - View a list of helpful commands.");
        inOrder.verify(sender).sendMessage(AQUA + "/kdrt info - View your kills, deaths and K/D ratio.");
        verifyNoMoreInteractions(sender);
    }

    @Test
    void execute_sendsTheHelpListing() {
        CommandSender sender = mock(CommandSender.class);

        assertTrue(new HelpCommand().execute(sender));

        assertHelpListingSentTo(sender);
    }

    @Test
    void execute_withArguments_ignoresThemAndSendsTheSameListing() {
        CommandSender sender = mock(CommandSender.class);

        assertTrue(new HelpCommand().execute(sender, new String[]{"extra", "args"}));

        assertHelpListingSentTo(sender);
    }

    @Test
    void permissions_areKdrtHelpOnly() {
        assertEquals(List.of("kdrt.help"), new HelpCommand().getPermissions());
    }
}
