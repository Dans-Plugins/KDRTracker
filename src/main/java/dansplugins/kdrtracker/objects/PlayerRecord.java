package dansplugins.kdrtracker.objects;

import org.bukkit.entity.Player;
import preponderous.ponder.misc.abs.Savable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Daniel McCoy Stephenson
 * @since June 19th, 2022
 *
 * This class is intended to keep track of a player's kills and deaths.
 */
public class PlayerRecord implements Savable {
    private UUID playerUUID;
    private int kills = 0;
    private int deaths = 0;

    public PlayerRecord(Player player) {
        this.playerUUID = player.getUniqueId();
    }

    public PlayerRecord(Map<String, String> data) {
        this.load(data);
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public int getKills() {
        return kills;
    }

    public void incrementKills() {
        setKills((getKills() + 1));
    }

    public int getDeaths() {
        return deaths;
    }

    public void incrementDeaths() {
        setDeaths(getDeaths() + 1);
    }

    public double getRatio() {
        if (deaths == 0) {
            return kills;
        }
        double dKills = kills;
        double dDeaths = deaths;

        return dKills/dDeaths;
    }

    private void setKills(int kills) {
        this.kills = kills;
    }

    private void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    @Override
    public Map<String, String> save() {
        Map<String, String> data = new HashMap<>();
        data.put("playerUUID", playerUUID.toString());
        data.put("kills", "" + kills);
        data.put("deaths", "" + deaths);
        return data;
    }

    @Override
    public void load(Map<String, String> map) {
        playerUUID = UUID.fromString(map.get("playerUUID"));
        kills = Integer.parseInt(map.get("kills"));
        deaths = Integer.parseInt(map.get("deaths"));
    }
}