package com.maxabrashov.authplugin.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SetSpawnCommand implements CommandExecutor {

    private final Plugin plugin;
    public SetSpawnCommand(Plugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player p = (Player) commandSender;
        Location location = p.getLocation();
        FileConfiguration config = this.plugin.getConfig();

        config.set("spawn.x", location.getBlockX());
        config.set("spawn.y", location.getBlockY());
        config.set("spawn.z", location.getBlockZ());
        config.set("spawn.yaw", location.getYaw());
        config.set("spawn.pitch", location.getPitch());

        plugin.saveConfig();
        p.sendMessage("New spawn - set!");
        return false;
    }
}
