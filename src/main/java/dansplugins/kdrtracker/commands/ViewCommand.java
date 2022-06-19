package dansplugins.kdrtracker.commands;

import dansplugins.kdrtracker.KDRTracker;
import dansplugins.kdrtracker.data.PersistentData;
import dansplugins.kdrtracker.exceptions.PlayerRecordNotFoundException;
import dansplugins.kdrtracker.objects.PlayerRecord;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Daniel McCoy Stephenson
 * @since June 19th, 2022
 *
 * This command is intended to allow players to view their ratio.
 */
public class ViewCommand extends AbstractPluginCommand {
    private final PersistentData persistentData;

    public ViewCommand(PersistentData persistentData) {
        super(new ArrayList<>(Arrays.asList("default")), new ArrayList<>(Arrays.asList("kdrt.default")));
        this.persistentData = persistentData;
    }

    @Override
    public boolean execute(CommandSender commandSender) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("This command is intended for use by players.");
            return false;
        }
        final Player player = (Player) commandSender;
        final PlayerRecord playerRecord;
        try {
            playerRecord = persistentData.getPlayerRecord(player.getUniqueId());
        } catch (PlayerRecordNotFoundException e) {
            // error
            player.sendMessage("Your player record wasn't found.");
            return false;
        }

        int kills = playerRecord.getKills();
        int deaths = playerRecord.getDeaths();

        player.sendMessage("Kills: " + kills);
        player.sendMessage("Deaths: " + deaths);
        player.sendMessage("K/D Ratio: " + getRatio(kills, deaths));
        return true;
    }

    @Override
    public boolean execute(CommandSender commandSender, String[] strings) {
        return execute(commandSender);
    }

    private double getRatio(double kills, double deaths) {
        return kills/deaths;
    }
}
