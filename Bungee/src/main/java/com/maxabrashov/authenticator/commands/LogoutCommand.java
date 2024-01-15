package com.maxabrashov.authenticator.commands;

import com.maxabrashov.authenticator.database.DataBaseHandler;
import com.maxabrashov.authenticator.yamlConfig.yamlConfig;
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

public class LogoutCommand extends Command {
    private final DataBaseHandler db;
    private final yamlConfig config;
    private List<ProxiedPlayer> playersLogin;
    private List<ProxiedPlayer> playersRegister;
    public LogoutCommand(DataBaseHandler db, yamlConfig config, List<ProxiedPlayer> playersLogin, List<ProxiedPlayer> playersRegister) {
        super("logout", (String) null);
        this.db = db;
        this.config = config;
        this.playersLogin = playersLogin;
        this.playersRegister = playersRegister;
    }
    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        try {
            ProxiedPlayer p = (ProxiedPlayer) commandSender;
            if (this.playersLogin.contains(p)) {
                this.config.sendMessageError(p, "messages.login.login-not-auth");
            }
            else if (this.playersRegister.contains(p)) {
                this.config.sendMessageError(p, "messages.login.login-havent-account");
            }
            else {
                this.config.sendMessageServer(p, "messages.login.login-logout");
                Timestamp newTime = new Timestamp(System.currentTimeMillis());
                newTime.setTime(newTime.getTime()-(24*60*60*1000));
                ResultSet rs = db.selectFromTable("Auth_Users", "id", "name", p.getName());
                int id = -1;
                if(rs.next()){ id = rs.getInt("id"); }
                if (id != -1) { db.updateIntoTable("Auth_SessionUsers", "last_session_start", newTime.toString(), "user_id", String.valueOf(id)); }
                this.config.serverConnect(p, this.config.getListString("auth-servers"));
                this.config.onSendPluginMessage(p, "authplugin:bcauth", this.config.getString("messages.login.login-bossbar"), 60);
                this.playersLogin.add(p);
            }
        } catch(Exception e) { this.config.sendWMConsole(e.getMessage()); }
    }
}
