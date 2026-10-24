package dansplugins.kdrtracker.commands;

import dansplugins.kdrtracker.data.PersistentData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;
import preponderous.ponder.minecraft.bukkit.services.CommandService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.RED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Daniel McCoy Stephenson
 *
 * Characterises how a sub-command typed after /kdrt or /kdrtracker reaches its
 * command class: ponder's real CommandService, given the commands and the
 * not-found message KDRTracker#initializeCommandService hands it and the base
 * commands plugin.yml declares. Covers routing by name and alias, the reply to
 * an unknown sub-command, and the reply when the sender lacks every node a
 * command accepts. KDRTracker#onCommand handles a bare /kdrt itself, so that
 * case is DefaultCommandTest's.
 */
class CommandDispatchTest {
    private static final String NOT_FOUND_MESSAGE = "That command wasn't found.";

    private PersistentData persistentData;
    private CommandService commandService;

    @SuppressWarnings("unchecked")
    private Set<String> baseCommandsDeclaredInPluginYml() throws Exception {
        try (InputStream pluginYml = getClass().getClassLoader().getResourceAsStream("plugin.yml")) {
            assertNotNull(pluginYml, "plugin.yml should be on the test classpath");
            Map<String, Object> descriptor = new Yaml().load(pluginYml);
            return new HashSet<>(((Map<String, Object>) descriptor.get("commands")).keySet());
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        persistentData = new PersistentData();
        commandService = new CommandService(baseCommandsDeclaredInPluginYml());
        // The same commands and message as KDRTracker#initializeCommandService.
        commandService.initialize(new ArrayList<AbstractPluginCommand>(Arrays.asList(
                new HelpCommand(),
                new InfoCommand(persistentData)
        )), NOT_FOUND_MESSAGE);
    }

    private CommandSender senderWithPermissions(boolean granted) {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission(anyString())).thenReturn(granted);
        return sender;
    }

    @Test
    void help_underEitherBaseCommand_sendsTheHelpListing() {
        for (String label : new String[]{"kdrt", "kdrtracker"}) {
            CommandSender sender = senderWithPermissions(true);

            assertTrue(commandService.interpretAndExecuteCommand(sender, label, new String[]{"help"}));

            verify(sender).sendMessage(AQUA + "/kdrt help - View a list of helpful commands.");
            verify(sender).sendMessage(AQUA + "/kdrt info - View your kills, deaths and K/D ratio.");
        }
    }

    @Test
    void infoAndItsViewAlias_reachInfoCommand() {
        for (String subCommand : new String[]{"info", "view"}) {
            Player player = mock(Player.class);
            when(player.hasPermission(anyString())).thenReturn(true);
            when(player.getUniqueId()).thenReturn(UUID.randomUUID());

            assertFalse(commandService.interpretAndExecuteCommand(player, "kdrt", new String[]{subCommand}));

            // A player with no record gets InfoCommand's own reply, which shows the
            // sub-command was routed to it.
            verify(player).sendMessage("Your player record wasn't found.");
        }
    }

    @Test
    void unknownSubCommand_repliesThatTheCommandWasNotFound() {
        CommandSender sender = senderWithPermissions(true);

        assertFalse(commandService.interpretAndExecuteCommand(sender, "kdrt", new String[]{"leaderboard"}));

        verify(sender).sendMessage(RED + NOT_FOUND_MESSAGE);
        verify(sender, never()).hasPermission(anyString());
    }

    @Test
    void subCommandNames_areCaseSensitive() {
        // "HELP" is not matched to "help"; it gets the same reply as an unknown sub-command.
        CommandSender sender = senderWithPermissions(true);

        assertFalse(commandService.interpretAndExecuteCommand(sender, "kdrt", new String[]{"HELP"}));

        verify(sender).sendMessage(RED + NOT_FOUND_MESSAGE);
        verify(sender, never()).sendMessage(AQUA + "/kdrt help - View a list of helpful commands.");
    }

    @Test
    void help_withoutKdrtHelp_namesTheMissingNodeAndDoesNotRun() {
        CommandSender sender = senderWithPermissions(false);

        assertFalse(commandService.interpretAndExecuteCommand(sender, "kdrt", new String[]{"help"}));

        verify(sender).sendMessage(RED + "In order to use this command, you need one of the following permission: '[kdrt.help]'");
        verify(sender, never()).sendMessage(AQUA + "/kdrt help - View a list of helpful commands.");
    }

    @Test
    void info_withoutEitherNode_namesBothNodesAndDoesNotRun() {
        Player player = mock(Player.class);
        when(player.hasPermission(anyString())).thenReturn(false);

        assertFalse(commandService.interpretAndExecuteCommand(player, "kdrt", new String[]{"info"}));

        verify(player).hasPermission("kdrt.info");
        verify(player).hasPermission("kdrt.view");
        verify(player).sendMessage(RED + "In order to use this command, you need one of the following permission: '[kdrt.info, kdrt.view]'");
        verify(player, never()).getUniqueId();
    }

    @Test
    void info_withOnlyKdrtView_runs() {
        Player player = mock(Player.class);
        when(player.hasPermission("kdrt.info")).thenReturn(false);
        when(player.hasPermission("kdrt.view")).thenReturn(true);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());

        commandService.interpretAndExecuteCommand(player, "kdrt", new String[]{"info"});

        verify(player).sendMessage("Your player record wasn't found.");
    }

    @Test
    void aLabelThatIsNotABaseCommand_isIgnoredWithoutAReply() {
        CommandSender sender = senderWithPermissions(true);

        assertFalse(commandService.interpretAndExecuteCommand(sender, "other", new String[]{"help"}));

        verifyNoInteractions(sender);
    }
}
