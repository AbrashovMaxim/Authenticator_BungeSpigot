package com.maxabrashov.authplugin.model;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Bar {
    private int taskId = -1;
    private final Plugin plugin;
    private final Player player;
    private BossBar bar;
    private int time;
    private final String title;
    private boolean stop = false;

    public Bar(Plugin plugin, Player player, int time, String title) {
        this.plugin = plugin;
        this.player = player;
        this.time = time;
        this.title = title;
    }

    public BossBar getBar() { return this.bar; }

    public void build() {
        this.bar = Bukkit.createBossBar(String.format(this.title, String.valueOf(this.time) + "s"), BarColor.RED, BarStyle.SOLID);
        this.bar.setVisible(true);
        this.bar.addPlayer(this.player);
        cast();
        sendMessage(this.player, "authplugin:bctauth","passwordAlong", "along");
    }

    public void cast() {
        int tempTime = time;
        new BukkitRunnable() {
            double progress = 1.0;
            double timeTimer = 1.0/tempTime;
            @Override
            public void run() {
                if (stop) {
                    bar.setVisible(false);
                    bar.removeAll();
                    this.cancel();
                }
                else if (progress <= 0.0 || time == -1) {
                    sendMessage(player, "authplugin:bcauth","passwordAlong", "along");
                    bar.setVisible(false);
                    bar.removeAll();
                    this.cancel();
                }
                else {
                    bar.setTitle(String.format(title, String.valueOf(time) + "s"));
                    bar.setProgress(progress);
                    progress = progress-timeTimer;
                    time -= 1;
                }

            }
        }.runTaskTimer(plugin, 0, 20);
    }
    public void sendMessage(Player player, String channelName, String nameMessage, String message) {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF(nameMessage);
        output.writeUTF(message);
        output.writeUTF(player.getName());
        player.sendPluginMessage(this.plugin, channelName, output.toByteArray());
    }

    public void stopBar() { this.stop = true; }

}
