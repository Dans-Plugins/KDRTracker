package dansplugins.kdrtracker.commands;

import dansplugins.kdrtracker.KDRTracker;
import org.bukkit.command.CommandSender;
import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;

import java.util.ArrayList;
import java.util.Arrays;

import static org.bukkit.ChatColor.AQUA;

/**
 * @author Daniel McCoy Stephenson
 */
public class DefaultCommand extends AbstractPluginCommand {
    private final KDRTracker kdrTracker;

    public DefaultCommand(KDRTracker kdrTracker) {
        // kdrt.default is vestigial and is deliberately not declared in plugin.yml. This command is
        // not handed to ponder's CommandService: KDRTracker#onCommand constructs it and calls
        // execute(sender) directly when no sub-command is given, so no permission check ever runs
        // against this list. The node is kept only so the list is non-empty, because
        // PermissionChecker#checkPermission denies a sender when it is given no permissions at all.
        super(new ArrayList<>(Arrays.asList("default")), new ArrayList<>(Arrays.asList("kdrt.default")));
        this.kdrTracker = kdrTracker;
    }

    @Override
    public boolean execute(CommandSender commandSender) {
        commandSender.sendMessage(AQUA + "KDRTracker " + kdrTracker.getVersion());
        commandSender.sendMessage(AQUA + "Developed by: Daniel Stephenson");
        commandSender.sendMessage(AQUA + "Wiki: https://github.com/Dans-Plugins/KDRTracker/wiki");
        return true;
    }

    @Override
    public boolean execute(CommandSender commandSender, String[] strings) {
        return execute(commandSender);
    }
}