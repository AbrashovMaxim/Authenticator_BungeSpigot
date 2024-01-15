package com.maxabrashov.authplugin.model;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.HashMap;

public class Bungee implements PluginMessageListener {
    private Plugin plugin;
    private HashMap<Player, Bar> hash;
    private HashMap<Player, String[]> playersTitle;
    public Bungee(Plugin plugin, HashMap<Player, Bar> hash, HashMap<Player, String[]> playersTitle) {
        this.plugin = plugin;
        this.hash = hash;
        this.playersTitle = playersTitle;
    }
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (channel.equals("authplugin:bcauth") && !this.hash.containsKey(player)) {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String nameMessage = in.readUTF();
            int Message = in.readInt();
            Bar bar = new Bar(this.plugin, player, Message, nameMessage);
            hash.put(player, bar);
            bar.build();
        }
        else if (channel.equals("authplugin:bctauth") && !this.playersTitle.containsKey(player)) {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String nameMessage = in.readUTF();
            String Message = in.readUTF();
            this.playersTitle.put(player, new String[] {nameMessage, Message});
        }
    }
}
