package dansplugins.kdrtracker.commands;

import dansplugins.kdrtracker.data.PersistentData;
import dansplugins.kdrtracker.exceptions.PlayerRecordNotFoundException;
import dansplugins.kdrtracker.objects.PlayerRecord;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;

import java.util.ArrayList;
import java.util.Arrays;

import static org.bukkit.ChatColor.AQUA;

/**
 * @author Daniel McCoy Stephenson
 * @since June 19th, 2022
 *
 * This command is intended to allow players to view their ratio.
 */
public class InfoCommand extends AbstractPluginCommand {
    private final PersistentData persistentData;

    public InfoCommand(PersistentData persistentData) {
        super(new ArrayList<>(Arrays.asList("info", "view")), new ArrayList<>(Arrays.asList("kdrt.info", "kdrt.view")));
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

        player.sendMessage(AQUA + "Kills: " + kills);
        player.sendMessage(AQUA + "Deaths: " + deaths);
        player.sendMessage(AQUA + "K/D Ratio: " + playerRecord.getRatio());
        return true;
    }

    @Override
    public boolean execute(CommandSender commandSender, String[] strings) {
        return execute(commandSender);
    }
}
