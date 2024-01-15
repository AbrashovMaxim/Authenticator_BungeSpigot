package com.maxabrashov.authenticator;

import com.maxabrashov.authenticator.commands.LoginCommand;
import com.maxabrashov.authenticator.commands.LogoutCommand;
import com.maxabrashov.authenticator.commands.RegisterCommand;
import com.maxabrashov.authenticator.database.DataBaseHandler;
import com.maxabrashov.authenticator.events.Events;
import com.maxabrashov.authenticator.yamlConfig.yamlConfig;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.protocol.packet.BossBar;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class Authenticator extends Plugin {

    @Override
    public void onEnable() {
        getProxy().registerChannel("authplugin:bcauth");
        getProxy().registerChannel("authplugin:bctauth");
        yamlConfig config = new yamlConfig(this, "config.yml", this.getDataFolder());
        DataBaseHandler db;
        try {
            db = new DataBaseHandler(config, this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        List<ProxiedPlayer> playersInAuth = new ArrayList<ProxiedPlayer>();
        List<ProxiedPlayer> playersLogin = new ArrayList<ProxiedPlayer>();
        List<ProxiedPlayer> playersRegister = new ArrayList<ProxiedPlayer>();
        List<ProxiedPlayer> playersBar = new ArrayList<ProxiedPlayer>();
        getProxy().getPluginManager().registerCommand(this, new LoginCommand(db, config, playersLogin));
        getProxy().getPluginManager().registerCommand(this, new RegisterCommand(db, config, playersRegister));
        getProxy().getPluginManager().registerCommand(this, new LogoutCommand(db, config, playersLogin, playersRegister));
        getProxy().getPluginManager().registerListener(this, new Events(this, db, config, playersLogin, playersRegister, playersBar, playersInAuth));
        getProxy().getScheduler().schedule(this, () -> {
            try {
                for (ProxiedPlayer p : playersLogin) {
                    if (!playersBar.contains(p)) {
                        config.onSendPluginMessage(p, "authplugin:bcauth", config.getString("messages.login.login-bossbar"),  60);
                        config.onSendPluginMessage(p, "authplugin:bctauth", config.getString("messages.login.login-title"), config.getString("messages.login.login-subtitle"));
                    }
                    config.sendMessageServer(p, "messages.login.login-chat");
                }
                for (ProxiedPlayer p : playersRegister) {
                    if (!playersBar.contains(p)) {
                        config.onSendPluginMessage(p, "authplugin:bcauth", config.getString("messages.register.register-bossbar"), 60);
                        config.onSendPluginMessage(p, "authplugin:bctauth", config.getString("messages.register.register-title"), config.getString("messages.register.register-subtitle"));
                    }
                    config.sendMessageServer(p, "messages.register.register-chat");
                }
            } catch (Exception exc) {
                config.sendWMConsole(exc.getMessage());
                exc.printStackTrace();
            }

        }, 5, 5, TimeUnit.SECONDS);
        getProxy().getScheduler().schedule(this, () -> {
            try {
                List<ProxiedPlayer> deleteFromAuth = new ArrayList<ProxiedPlayer>();
                for (ProxiedPlayer p : playersInAuth) {
                    if (!playersLogin.contains(p) && !playersRegister.contains(p)) {
                        Server getServer = p.getServer();
                        if (getServer != null) {
                            if (config.getListString("auth-servers").contains(getServer.getInfo().getName())) {
                                config.serverConnect(p, config.getListString("lobby-servers"));
                            }
                            else {
                                deleteFromAuth.add(p);
                            }
                        }

                    }
                }
                for (ProxiedPlayer p : deleteFromAuth) {
                    if (playersInAuth.contains(p)) { playersInAuth.remove(p); }
                    if (playersBar.contains(p)) {
                        playersBar.remove(p);
                    }

                }
            } catch (Exception exc) {
                config.sendWMConsole(exc.getMessage());
                exc.printStackTrace();
            }

        }, 500, 500, TimeUnit.MILLISECONDS);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
