package com.maxabrashov.authenticator.commands;

import com.maxabrashov.authenticator.database.DataBaseHandler;
import com.maxabrashov.authenticator.yamlConfig.yamlConfig;
import jdk.jpackage.internal.Log;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;

public class LoginCommand extends Command {
    private final DataBaseHandler db;
    private final yamlConfig config;
    private List<ProxiedPlayer> playersLogin;
    public LoginCommand(DataBaseHandler db, yamlConfig config, List<ProxiedPlayer> playersLogin) {
        super("login", (String) null, "l");
        this.db = db;
        this.config = config;
        this.playersLogin = playersLogin;
    }
    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        try {
            int id = -1;
            String password = "";
            ProxiedPlayer p = (ProxiedPlayer) commandSender;
            // Есть ли пользователь в БД
            ResultSet rs = db.selectFromTable("Auth_Users", new String[]{"id", "password"}, new String[]{"name"}, new String[]{p.getName()});
            if(rs.next()){
                id = rs.getInt("id");
                password = rs.getString("password");
            }
            if (password.length() == 0) {
                this.config.sendMessageError(p, "messages.login.login-havent-account");
                return;
            }
            if (!this.playersLogin.contains(p)) {
                this.config.sendMessageError(p, "messages.login.login-session");
                return;
            }
            // Если неправильно ввел команду
            if (strings.length != 1) {
                this.config.sendMessageError(p, "messages.login.login-chat");
                return;
            }
            String hashedPassword = BCrypt.hashpw(strings[0], BCrypt.gensalt(10));
            // Правильно ли ввел пароль?
            if (BCrypt.checkpw(strings[0], password)) {
                ResultSet getRs = db.selectFromTable("Auth_SecurityUsers", "ConnectLastServer", "user_id", String.valueOf(id));
                while (getRs.next()) {
                    // Есть ли авто-вход на последний сервер?
                    if (getRs.getBoolean("ConnectLastServer")) {
                        if(db.existInTable("Auth_SessionUsers", "user_id", String.valueOf(id))) {
                            ResultSet getIp = db.selectFromTable("Auth_SessionUsers", "last_ip", "user_id", String.valueOf(id));
                            if (getIp.next()) {
                                if (getIp.getString("last_ip").equals(p.getPendingConnection().getAddress().toString())) {
                                    ServerInfo getServ = ProxyServer.getInstance().getServerInfo(getRs.getString("last_server"));
                                    if (getServ != null) {
                                        this.config.serverConnect(p, getServ);
                                        if (this.playersLogin.contains(p)) { playersLogin.remove(p); }
                                    }
                                    else {
                                        this.config.serverConnect(p, this.config.getListString("lobby-servers"));
                                        if (this.playersLogin.contains(p)) { playersLogin.remove(p); }
                                    }
                                } else {
                                    this.config.serverConnect(p, this.config.getListString("lobby-servers"));
                                    if (this.playersLogin.contains(p)) { playersLogin.remove(p); }
                                }
                            } else {
                                this.config.serverConnect(p, this.config.getListString("lobby-servers"));
                                if (this.playersLogin.contains(p)) {
                                    playersLogin.remove(p);
                                }
                            }
                        }
                        else {
                            this.config.serverConnect(p, this.config.getListString("lobby-servers"));
                            if (this.playersLogin.contains(p)) { playersLogin.remove(p); }
                        }

                    }
                    else {
                        this.config.serverConnect(p, this.config.getListString("lobby-servers"));
                        if (this.playersLogin.contains(p)) { playersLogin.remove(p); }
                    }
                    Timestamp newTime = new Timestamp(System.currentTimeMillis());
                    if(db.existInTable("Auth_SessionUsers", "user_id", String.valueOf(id))) {
                        db.updateIntoTable("Auth_SessionUsers", new String[] {"last_session_start", "last_ip"}, new String[] {newTime.toString(), p.getPendingConnection().getAddress().toString()}, "user_id", String.valueOf(id));
                    }
                    else {
                        db.insertIntoTable("Auth_SessionUsers", new String[] {"user_id", "last_session_start", "last_ip", "last_server"}, new String[] {String.valueOf(id), newTime.toString(), p.getPendingConnection().getAddress().toString(), "lobby"});
                    }
                }
            }
            else {
                this.config.sendMessageError(p, "messages.login.login-wrong-password");
            }
        } catch(Exception e) { this.config.sendWMConsole(e.getMessage()); }
    }
}
