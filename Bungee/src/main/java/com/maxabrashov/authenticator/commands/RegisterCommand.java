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

public class RegisterCommand extends Command {
    private final DataBaseHandler db;
    private final yamlConfig config;
    private List<ProxiedPlayer> playersRegister;
    public RegisterCommand(DataBaseHandler db, yamlConfig config, List<ProxiedPlayer> playersRegister) {
        super("register", (String) null, "reg");
        this.db = db;
        this.config = config;
        this.playersRegister = playersRegister;
    }
    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        try {
            ProxiedPlayer p = (ProxiedPlayer) commandSender;
            // Есть ли пользователь в БД
            if(db.existInTable("Auth_Users", "name", p.getName())){
                this.config.sendMessageError(p, "messages.register.register-account-exist");
                return;
            }
            // Если неправильно ввел команду
            if (strings.length != 2) {
                this.config.sendMessageError(p, "messages.register.register-chat");
                return;
            }

            if (!strings[0].equals(strings[1])) {
                this.config.sendMessageError(p, "messages.register.register-password-mismatch");
                return;
            }

            if (strings[0].length() < 8) {
                this.config.sendMessageError(p, "messages.register.register-password-small");
                return;
            }
            String hashedPassword = BCrypt.hashpw(strings[0], BCrypt.gensalt(10));
            db.insertIntoTable("Auth_Users", new String[] {"name", "uuid", "password"}, new String[] {p.getName(), p.getUniqueId().toString(), hashedPassword});
            ResultSet rs = db.selectFromTable("Auth_Users", "id", "name", p.getName());
            if(rs.next()){
                int id = rs.getInt("id");
                if (db.existInTable("Auth_SecurityUsers", "id", String.valueOf(id))) { db.deleteFromTable("Auth_SecurityUsers", "id", String.valueOf(id)); }
                if (db.existInTable("Auth_SessionUsers", "id", String.valueOf(id))) { db.deleteFromTable("Auth_SessionUsers", "id", String.valueOf(id)); }
                db.insertIntoTable("Auth_SecurityUsers", "user_id", String.valueOf(id));
                Timestamp newTime = new Timestamp(System.currentTimeMillis());
                db.insertIntoTable("Auth_SessionUsers", new String[] {"user_id", "last_session_start", "last_ip", "last_server"}, new String[] {String.valueOf(id), newTime.toString(), p.getPendingConnection().getAddress().toString(), "lobby"});
                if (this.playersRegister.contains(p)) { playersRegister.remove(p); }
                this.config.serverConnect(p, this.config.getListString("lobby-servers"));
            }
        } catch(Exception e) { this.config.sendWMConsole(e.getMessage()); }
    }
}
