package com.maxabrashov.authenticator.yamlConfig;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class yamlConfig {
    private final Plugin plugin;
    private File file;
    private Configuration configuration;

    public yamlConfig(Plugin plugin, String name, File dataFolder) {
        this.file = new File(dataFolder, name);
        this.plugin = plugin;
        if (!dataFolder.exists()) {dataFolder.mkdir();}

        try {
            if (!file.exists()) {
                file.createNewFile();

                try (InputStream in = plugin.getResourceAsStream(name);
                OutputStream out = new FileOutputStream(file)) { ByteStreams.copy(in, out); }
            }


        } catch (IOException e) { e.printStackTrace(); }
    }

    public List<String> getDBfromConfig() {
        List<String> result = new ArrayList<String>();
        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            result.add(configuration.getString("database.host"));
            result.add(configuration.getString("database.database"));
            result.add(configuration.getString("database.username"));
            result.add(configuration.getString("database.password"));
            result.add(String.valueOf(configuration.getInt("database.port")));

        } catch (IOException e) { e.printStackTrace(); }

        return result;
    }

    public void sendMessageError(ProxiedPlayer p, String path) {
        sendMessage(p, "error", path);
    }
    public void sendMessageServer(ProxiedPlayer p, String path) {
        sendMessage(p, "prefix", path);
    }

    private void sendMessage(ProxiedPlayer p, String pathPrefix, String path) {
        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            String prefix = "";
            if (configuration.getBoolean("messages.settings." + pathPrefix + ".prefix-enable")) {
                if (p.getPendingConnection().getVersion() >= 735) {
                    prefix = configuration.getString("messages.settings." + pathPrefix + ".prefix-name-116") + configuration.getString("messages.settings." + pathPrefix + ".prefix-separator");
                } else {
                    prefix = configuration.getString("messages.settings." + pathPrefix + ".prefix-name-115") + configuration.getString("messages.settings." + pathPrefix + ".prefix-separator");
                }
            }
            p.sendMessage(new TextComponent(prefix+configuration.getString(path)).toLegacyText());
        } catch (IOException e) { e.printStackTrace(); }
    }

    public List<String> getListString(String path) { return this.configuration.getStringList(path); }
    public String getString(String path) { return this.configuration.getString(path); }

    public void sendWMConsole(String message) { this.plugin.getLogger().warning(message); }

    public boolean serverConnect(ProxiedPlayer p, List<String> serversName) {
        for (String serverName : serversName){
            ServerInfo getServ = ProxyServer.getInstance().getServerInfo(serverName);
            if (getServ != null) {
                try {
                    if(!p.getServer().getInfo().equals(getServ)) { p.connect(getServ); return true; }
                } catch (Exception exc) {
                    p.connect(getServ);
                    return true;
                }
            }
            else {
                List<String> getLobbys = this.configuration.getStringList("lobby-servers");
                for (String nameServer : getLobbys) {
                    getServ = ProxyServer.getInstance().getServerInfo(nameServer);
                    if (getServ != null) {
                        p.connect(getServ);
                        break;
                    }
                }
            }
        }
        return false;
    }
    public boolean serverConnect(ProxiedPlayer p, String serverName) {
        ServerInfo getServ = ProxyServer.getInstance().getServerInfo(serverName);
        if (getServ != null) {
            if(!p.getServer().getInfo().equals(getServ)) { p.connect(getServ); return true; }
        }
        else {
            List<String> getLobbys = this.configuration.getStringList("lobby-servers");
            for (String nameServer : getLobbys) {
                getServ = ProxyServer.getInstance().getServerInfo(nameServer);
                if (getServ != null) {
                    p.connect(getServ);
                    break;
                }
            }
        }
        return false;
    }

    public boolean serverConnect(ProxiedPlayer p, ServerInfo server) {
        if (server != null) {
            if(!p.getServer().getInfo().equals(server)) { p.connect(server); return true; }
        }
        else {
            List<String> getLobbys = this.configuration.getStringList("lobby-servers");
            for (String nameServer : getLobbys) {
                ServerInfo getServ = ProxyServer.getInstance().getServerInfo(nameServer);
                if (getServ != null) {
                    p.connect(getServ);
                    break;
                }
            }
        }
        return false;
    }
    public Timestamp parseTimeStamp(String inputString)
    {
        SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
        try{
            Date parsedDate = format.parse(inputString);
            return new Timestamp(parsedDate.getTime());
        }
        catch(ParseException e)
        {
            return null;
        }
    }

    public void onSendPluginMessage(ProxiedPlayer p, String nameChannel, String nameMessage, int message) {
        System.out.print("SendMessageFromBungee");
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(nameMessage);
        out.writeInt(message);
        p.getServer().getInfo().sendData(nameChannel, out.toByteArray());
    }

    public void onSendPluginMessage(ProxiedPlayer p, String nameChannel, String nameMessage, String message) {
        System.out.print("SendMessageFromBungee");
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(nameMessage);
        out.writeUTF(message);
        p.getServer().getInfo().sendData(nameChannel, out.toByteArray());
    }
}
