package com.maxabrashov.authplugin;

import com.maxabrashov.authplugin.commands.SetSpawnCommand;
import com.maxabrashov.authplugin.model.Bar;
import com.maxabrashov.authplugin.model.Bungee;
import events.EventsListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public final class AuthPlugin extends JavaPlugin {

    private HashMap<Player, String[]> playersTitle = new HashMap<>();

    @Override
    public void onEnable() {
        HashMap<Player, Bar> hash = new HashMap<>();
        Plugin main = this;
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "authplugin:bcauth");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "authplugin:bcauth", new Bungee(this, hash, this.playersTitle));
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "authplugin:bctauth");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "authplugin:bctauth", new Bungee(this, hash, this.playersTitle));
        Bukkit.getPluginManager().registerEvents(new EventsListener(this, hash, this.playersTitle), this);
        getCommand("setspawn").setExecutor(new SetSpawnCommand(this));

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    if (playersTitle.containsKey(p)) {
                        p.sendTitle(playersTitle.get(p)[0], playersTitle.get(p)[1], 5, 90, 5);
                    }
                }
            }
        }.runTaskTimer(this, 0, 100);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
                        if (!p.equals(pl)) {
                            p.hidePlayer(main, pl);
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0, 100);
    }

    @Override
    public void onDisable() {
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }
}
