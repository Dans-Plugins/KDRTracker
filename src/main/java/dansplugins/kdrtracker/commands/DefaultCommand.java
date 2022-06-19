package dansplugins.kdrtracker.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import dansplugins.kdrtracker.KDRTracker;
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