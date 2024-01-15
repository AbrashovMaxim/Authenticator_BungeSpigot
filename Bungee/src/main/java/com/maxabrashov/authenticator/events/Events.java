package com.maxabrashov.authenticator.events;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.maxabrashov.authenticator.database.DataBaseHandler;
import com.maxabrashov.authenticator.yamlConfig.yamlConfig;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;

import static org.springframework.aop.framework.ProxyFactory.getProxy;

public class Events implements Listener {
    private final Plugin plugin;
    private final DataBaseHandler db;
    private final yamlConfig config;
    private List<ProxiedPlayer> playersInAuth;
    private List<ProxiedPlayer> playersLogin;
    private List<ProxiedPlayer> playersRegister;
    private List<ProxiedPlayer> playersBar;

    public Events(Plugin plugin, DataBaseHandler db, yamlConfig config, List<ProxiedPlayer> playersLogin, List<ProxiedPlayer> playersRegister, List<ProxiedPlayer> playersBar, List<ProxiedPlayer> playersInAuth) {
        this.plugin = plugin;
        this.db = db;
        this.config = config;
        this.playersLogin = playersLogin;
        this.playersRegister = playersRegister;
        this.playersBar = playersBar;
        this.playersInAuth = playersInAuth;
    }

    @EventHandler
    public void playerLoginInServer(PostLoginEvent e) {
        ProxiedPlayer p = e.getPlayer();
        try {
            if (!this.playersInAuth.contains(p)) { this.playersInAuth.add(p); }
            // Есть ли ЮЗЕР в БД?
            if(this.db.existInTable("Auth_Users", "name", p.getName())) {
                ResultSet rs = db.selectFromTable("Auth_Users", "id", "name", p.getName());
                int id = -1;
                if(rs.next()){ id = rs.getInt("id"); }
                // Есть ЮЗЕР в БД
                if (id != -1) {
                    // Были ли Сессии ЮЗЕРА в БД?
                    if (this.db.existInTable("Auth_SessionUsers", "user_id", String.valueOf(id))) {
                        ResultSet rsSession = this.db.selectFromTable("Auth_SessionUsers", new String[] {"last_session_start", "last_server"}, "user_id", String.valueOf(id));
                        if (rsSession.next()) {
                            Timestamp times = rsSession.getTimestamp("last_session_start");
                            Timestamp newTime = new Timestamp(System.currentTimeMillis());
                            // Закончилось ли время в Сессии ЮЗЕРА в БД?
                            if (newTime.getTime()-times.getTime() <= 14400000) {
                                ResultSet getCon = db.selectFromTable("Auth_SecurityUsers", "ConnectLastServer", "user_id", String.valueOf(id));
                                if (getCon.next()) {
                                    // Авто-вход на последний сервер?
                                    if (getCon.getBoolean("ConnectLastServer")) {
                                        this.config.sendMessageServer(p, "messages.login.login-session-load");
                                        if (this.config.getListString("auth-servers").contains(rsSession.getString("last_server"))) {
                                            this.config.serverConnect(p, this.config.getListString("lobby-servers"));
                                        } else {
                                            ServerInfo getServer = ProxyServer.getInstance().getServerInfo(rsSession.getString("last_server"));
                                            this.config.serverConnect(p, getServer);
                                        }

                                    }
                                    // Нету авто-входа на последний сервер
                                    else {
                                        this.config.sendMessageServer(p, "messages.login.login-session-load");
                                        Server getServer = p.getServer();
                                        if (getServer != null) {
                                            if (this.config.getListString("auth-servers").contains(getServer.getInfo().getName())) {
                                                this.config.serverConnect(p, this.config.getListString("lobby-servers"));
                                            }
                                        }
                                        else {
                                            this.config.serverConnect(p, this.config.getListString("lobby-servers"));
                                        }
                                    }
                                    db.deleteFromTable("Auth_SessionUsers", "user_id", String.valueOf(id));
                                    newTime = new Timestamp(System.currentTimeMillis());
                                    db.insertIntoTable("Auth_SessionUsers", new String[] {"user_id", "last_session_start", "last_ip"}, new String[] {String.valueOf(id), newTime.toString(), p.getPendingConnection().getAddress().toString()});
                                    return;
                                }
                            }
                            // Закончилось время Сессии ЮЗЕРА в БД
                            else {
                                if (!this.playersLogin.contains(p)) { this.playersLogin.add(p); }
                            }
                        }
                        else {
                            if (!this.playersLogin.contains(p)) { this.playersLogin.add(p); }
                        }
                    }
                    // Нету Сессий ЮЗЕРА в БД
                    else {
                        if (!this.playersRegister.contains(p)) { this.playersRegister.add(p); }
                    }

                }
                else {
                    if (!this.playersRegister.contains(p)) { this.playersRegister.add(p); }
                }
            }
            // Нету ЮЗЕРА в БД
            else {
                if (!this.playersRegister.contains(p)) { this.playersRegister.add(p); }
            }
            this.config.serverConnect(p, this.config.getListString("auth-servers"));
        } catch(Exception exc) { this.config.sendWMConsole(exc.getMessage()); }
    }

    @EventHandler
    public void playerSwitchServer(ServerSwitchEvent e) {
        ProxiedPlayer p = e.getPlayer();
        try {
            if ((this.playersLogin.contains(p) || this.playersRegister.contains(p)) && !this.config.getListString("auth-servers").contains(p.getServer().getInfo().getName())) {
                this.config.serverConnect(p, this.config.getListString("auth-servers"));
            }
            if ((this.playersLogin.contains(p) || this.playersRegister.contains(p)) && this.config.getListString("auth-servers").contains(p.getServer().getInfo().getName())) {
                if (!this.playersBar.contains(p)) {
                    if (this.playersRegister.contains(p)) {
                        this.config.onSendPluginMessage(p, "authplugin:bcauth", this.config.getString("messages.register.register-bossbar"), 60);
                        this.config.onSendPluginMessage(p, "authplugin:bctauth", this.config.getString("messages.register.register-title"), this.config.getString("messages.register.register-subtitle"));
                    }
                    else if(this.playersLogin.contains(p)) {
                        this.config.onSendPluginMessage(p, "authplugin:bcauth", this.config.getString("messages.login.login-bossbar"),  60);
                        this.config.onSendPluginMessage(p, "authplugin:bctauth", this.config.getString("messages.login.login-title"), this.config.getString("messages.login.login-subtitle"));
                    }
                }
            }
            else {
                if(this.db.existInTable("Auth_Users", "name", p.getName())) {
                    Timestamp newTime = new Timestamp(System.currentTimeMillis());
                    ResultSet rs = db.selectFromTable("Auth_Users", "id", "name", p.getName());
                    int id = -1;
                    if(rs.next()){ id = rs.getInt("id"); }
                    if(this.db.existInTable("Auth_SessionUsers", "user_id", String.valueOf(id))) {
                        this.db.updateIntoTable("Auth_SessionUsers", new String[] {"last_server", "last_session_start"}, new String[] { p.getServer().getInfo().getName(), newTime.toString() }, "user_id", String.valueOf(id));
                    }
                    else {
                        db.insertIntoTable("Auth_SessionUsers", new String[] {"user_id", "last_session_start", "last_ip", "last_server"}, new String[] {String.valueOf(id), newTime.toString(), p.getPendingConnection().getAddress().toString(), p.getServer().getInfo().getName()});
                    }
                }
                else {
                    if (!this.playersRegister.contains(p)) { this.playersRegister.add(p); }
                    if (!this.config.getListString("auth-servers").contains(p.getServer().getInfo().getName())) {
                        this.config.serverConnect(p, this.config.getListString("auth-servers"));
                    }
                }
            }
        } catch(Exception exc) { this.config.sendWMConsole(exc.getMessage()); }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent e) {
        ProxiedPlayer p = e.getPlayer();
        if (this.playersRegister.contains(p)) { this.playersRegister.remove(p); }
        if (this.playersLogin.contains(p)) { this.playersLogin.remove(p); }
        if (this.playersBar.contains(p)) { this.playersBar.remove(p); }
        if (this.playersInAuth.contains(p)) { this.playersInAuth.remove(p); }
    }


    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        if (e.getTag().equals("authplugin:bcauth")) {
            ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());

            String nameMessage = in.readUTF();
            String message = in.readUTF();
            String player = in.readUTF();

            if (nameMessage.equals("passwordAlong")) {
                ProxiedPlayer p = ProxyServer.getInstance().getPlayer(player);
                String server = "";
                String lineUp = "";
                String lineDown = "";
                if (p.getPendingConnection().getVersion() >= 735) {
                    server = this.config.getString("messages.settings.prefix.prefix-name-116");
                    lineUp = this.config.getString("messages.settings.line-up-116");
                    lineDown = this.config.getString("messages.settings.line-down-116");
                }
                else {
                    server = this.config.getString("messages.settings.prefix.prefix-name-115");
                    lineUp = this.config.getString("messages.settings.line-up-115");
                    lineDown = this.config.getString("messages.settings.line-down-115");
                }

                if (this.playersLogin.contains(p)) {
                    String text = lineUp + "\n\n\n\n" + server + "\n\n" + this.config.getString("messages.login.login-time-along") + "\n\n\n\n" + lineDown;
                    p.disconnect(new TextComponent(text).toLegacyText());
                    return;
                }
                if (this.playersRegister.contains(p)) {

                    String text = lineUp + "\n\n\n\n" + server + "\n\n" + this.config.getString("messages.register.register-time-along") + "\n\n\n\n" + lineDown;
                    p.disconnect(new TextComponent(text).toLegacyText());
                    return;
                }
            }
        }
        else if (e.getTag().equals("authplugin:bctauth")) {
            ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
            String nameMessage = in.readUTF();
            String message = in.readUTF();
            String player = in.readUTF();
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(player);
            if (!this.playersBar.contains(p)) {
                this.playersBar.add(p);
            }
        }
    }
}
