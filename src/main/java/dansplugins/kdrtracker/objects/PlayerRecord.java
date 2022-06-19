package dansplugins.kdrtracker.objects;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author Daniel McCoy Stephenson
 * @since June 19th, 2022
 *
 * This class is intended to keep track of a player's kills and deaths.
 */
public class PlayerRecord {
    private final UUID playerUUID;
    private int kills = 0;
    private int deaths = 0;

    public PlayerRecord(Player player) {
        this.playerUUID = player.getUniqueId();
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

    private void setKills(int kills) {
        this.kills = kills;
    }

    private void setDeaths(int deaths) {
        this.deaths = deaths;
    }
}