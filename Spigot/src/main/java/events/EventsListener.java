package events;

import com.maxabrashov.authplugin.model.Bar;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;

public class EventsListener implements Listener {
    private final Plugin plugin;
    private HashMap<Player, Bar> hash;
    private HashMap<Player, String[]> playersTitle;

    public EventsListener(Plugin plugin, HashMap<Player, Bar> hash, HashMap<Player, String[]> playersTitle) {
        this.plugin = plugin;
        this.hash = hash;
        this.playersTitle = playersTitle;
    }

    @EventHandler
    public void onPlayerExitServer(PlayerQuitEvent e) {
        e.setQuitMessage("");
        Player p = (Player) e.getPlayer();
        if (hash.containsKey(p)) {
            hash.get(p).stopBar();
            hash.remove(p);
        }
        if (playersTitle.containsKey(p)) {
            playersTitle.remove(p);
        }
    }

    @EventHandler
    public void onPlayerJoinServer(PlayerJoinEvent e) {
        e.setJoinMessage("");
        FileConfiguration config = this.plugin.getConfig();
        int x = config.getInt("spawn.x");
        int y = config.getInt("spawn.y");
        int z = config.getInt("spawn.z");
        float yaw = (float) config.getDouble("spawn.yaw");
        float pitch = (float) config.getDouble("spawn.pitch");
        e.getPlayer().teleport(new Location(e.getPlayer().getWorld(), x, y, z, yaw, pitch));
        e.getPlayer().setInvulnerable(true);
        e.getPlayer().setGameMode(GameMode.ADVENTURE);
    }

    @EventHandler
    public void onPlayerSpawn(PlayerRespawnEvent e) {
        FileConfiguration config = this.plugin.getConfig();
        int x = config.getInt("spawn.x");
        int y = config.getInt("spawn.y");
        int z = config.getInt("spawn.z");
        float yaw = (float) config.getDouble("spawn.yaw");
        float pitch = (float) config.getDouble("spawn.pitch");
        e.getPlayer().teleport(new Location(e.getPlayer().getWorld(), x, y, z, yaw, pitch));
        e.getPlayer().setInvulnerable(true);
        e.getPlayer().setGameMode(GameMode.ADVENTURE);
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent e) {
        e.setLeaveMessage("");
        Player p = (Player) e.getPlayer();
        if (hash.containsKey(p)) {
            hash.get(p).stopBar();
            hash.remove(p);
        }
        if (playersTitle.containsKey(p)) {
            playersTitle.remove(p);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        System.out.println("SEND MESSAGE: " + e.getMessage());
    }
}
