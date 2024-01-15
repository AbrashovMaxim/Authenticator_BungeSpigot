package com.maxabrashov.authenticator.database;

import com.maxabrashov.authenticator.yamlConfig.yamlConfig;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBaseHandler {
    private final yamlConfig config;
    private final Plugin plugin;
    private Connection dbConnection;
    public DataBaseHandler(yamlConfig config, Plugin plugin) throws SQLException, ClassNotFoundException {
        this.config = config;
        this.dbConnection = getDbConnection(config);
        this.plugin = plugin;
        createAllTables();
    }

    private Connection getDbConnection(yamlConfig config) throws SQLException, ClassNotFoundException {
        List<String> getDb = config.getDBfromConfig();
        String connectionString = "jdbc:mysql://" + getDb.get(0) + ":" + getDb.get(4) + "/" + getDb.get(1);
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(connectionString, getDb.get(2), getDb.get(3));
    }

    private void createAllTables() throws SQLException {
        Statement statement = this.dbConnection.createStatement();
        String createUser = "CREATE TABLE IF NOT EXISTS Auth_Users (" +
                "id INTEGER NOT NULL AUTO_INCREMENT," +
                "uuid VARCHAR(255)," +
                "name VARCHAR(255)," +
                "password VARCHAR(255)," +
                "PRIMARY KEY (id))";
        statement.execute(createUser);
        String securityUser = "CREATE TABLE IF NOT EXISTS Auth_SecurityUsers (" +
                "id INTEGER NOT NULL AUTO_INCREMENT," +
                "user_id INT NOT NULL," +
                "DiscordToken VARCHAR(255) DEFAULT NULL," +
                "TelegramToken VARCHAR(255) DEFAULT NULL," +
                "VkToken VARCHAR(255) DEFAULT NULL," +
                "GoogleAuthToken VARCHAR(255) DEFAULT NULL," +
                "ConnectLastServer BOOLEAN DEFAULT 0," +
                "PRIMARY KEY (id)," +
                "Foreign KEY (user_id) REFERENCES Auth_Users(id))";
        statement.execute(securityUser);
        String sessionUser = "CREATE TABLE IF NOT EXISTS Auth_SessionUsers (" +
                "id INTEGER NOT NULL AUTO_INCREMENT," +
                "user_id INT NOT NULL," +
                "lass_quit TIMESTAMP," +
                "last_session_start TIMESTAMP," +
                "last_server VARCHAR(255) DEFAULT NULL," +
                "last_ip VARCHAR(255)," +
                "PRIMARY KEY (id)," +
                "Foreign KEY (user_id) REFERENCES Auth_Users(id))";
        statement.execute(sessionUser);
        statement.close();
    }

    public ResultSet selectFromTable(String Table, String select, String where, String value) {
        try {
            String query = "SELECT " + select + " FROM " + Table + " WHERE " + where + "=?";
            PreparedStatement statement = dbConnection.prepareStatement(query);
            try { statement.setInt(1, Integer.parseInt(value)); }
            catch (Exception e) {
                Timestamp getTime = this.config.parseTimeStamp(value);
                if (getTime != null) { statement.setTimestamp(1, getTime); }
                else { statement.setString(1, value); }
            }
            return statement.executeQuery();

        } catch (Exception e) { this.plugin.getLogger().warning(e.getMessage()); e.printStackTrace(); }
        return null;
    }

    public ResultSet selectFromTable(String Table, String[] select, String[] where, String[] value) {
        if (!(where.length >= 1 && where.length == value.length)) { return null; }
        try {
            StringBuilder sb = new StringBuilder();
            for(String where_ : where) {
                sb.append(where_).append("=?").append(",");
            }
            String query = "SELECT " + String.join(", ", select) + " FROM " + Table +" WHERE " + sb.toString().substring(0, sb.length()-1);
            PreparedStatement statement = dbConnection.prepareStatement(query);
            for (int i = 1; i <= value.length; i++) {
                try { statement.setInt(i, Integer.parseInt(value[i-1])); }
                catch (Exception e) {
                    Timestamp getTime = this.config.parseTimeStamp(value[i-1]);
                    if (getTime != null) { statement.setTimestamp(i, getTime); }
                    else { statement.setString(i, value[i-1]); }
                }
            }
            return statement.executeQuery();

        } catch (Exception e) { this.plugin.getLogger().warning(e.getMessage()); e.printStackTrace(); }
        return null;
    }

    public ResultSet selectFromTable(String Table, String[] select, String where, String value) {
        try {
            String query = "SELECT " + String.join(", ", select) + " FROM " + Table + " WHERE " + where + "=?";
            PreparedStatement statement = dbConnection.prepareStatement(query);
            try { statement.setInt(1, Integer.parseInt(value)); }
            catch (Exception e) {
                Timestamp getTime = this.config.parseTimeStamp(value);
                if (getTime != null) { statement.setTimestamp(1, getTime); }
                else { statement.setString(1, value); }
            }
            return statement.executeQuery();

        } catch (Exception e) { this.plugin.getLogger().warning(e.getMessage()); e.printStackTrace(); }
        return null;
    }

    public boolean existInTable(String Table, String where, String value) {
        try {
            String query = "SELECT * FROM " + Table + " WHERE " + where + "=?";
            PreparedStatement statement = dbConnection.prepareStatement(query);
            try { statement.setInt(1, Integer.parseInt(value)); }
            catch (Exception e) {
                Timestamp getTime = this.config.parseTimeStamp(value);
                if (getTime != null) { statement.setTimestamp(1, getTime); }
                else { statement.setString(1, value); }
            }
            ResultSet get =  statement.executeQuery();
            while (get.next()) { return true; }
            return false;

        } catch (Exception e) { this.plugin.getLogger().warning(e.getMessage()); e.printStackTrace(); }
        return false;
    }

    public void deleteFromTable(String Table, String where, String value) {
        try {
            String query = "DELETE FROM " + Table + " WHERE " + where + "=?";
            PreparedStatement statement = dbConnection.prepareStatement(query);
            try { statement.setInt(1, Integer.parseInt(value)); }
            catch (Exception e) {
                Timestamp getTime = this.config.parseTimeStamp(value);
                if (getTime != null) { statement.setTimestamp(1, getTime); }
                else { statement.setString(1, value); }
            }
            statement.execute();

        } catch (Exception e) { this.plugin.getLogger().warning(e.getMessage()); e.printStackTrace(); }
    }
    public void deleteFromTable(String Table, String[] where, String[] value) {
        if (!(where.length >= 1 && where.length == value.length)) { return; }
        try {
            StringBuilder sb = new StringBuilder();
            for(String where_ : where) {
                sb.append(where_).append("=?").append(" AND ");
            }
            String query = "DELETE FROM " + Table +" WHERE " + sb.toString().substring(0, sb.length()-5);
            PreparedStatement statement = dbConnection.prepareStatement(query);
            for (int i = 1; i <= value.length; i++) {
                try { statement.setInt(i, Integer.parseInt(value[i-1])); }
                catch (Exception e) {
                    Timestamp getTime = this.config.parseTimeStamp(value[i-1]);
                    if (getTime != null) { statement.setTimestamp(i, getTime); }
                    else { statement.setString(i, value[i-1]); }
                }
            }

        } catch (Exception e) { this.plugin.getLogger().warning(e.getMessage()); e.printStackTrace(); }
    }

    public void insertIntoTable(String Table, String where, String value) {
        try {
            String query = "INSERT INTO " + Table + " (" + where + ") VALUES (?)";
            PreparedStatement statement = dbConnection.prepareStatement(query);
            try { statement.setInt(1, Integer.parseInt(value)); }
            catch (Exception e) {
                Timestamp getTime = this.config.parseTimeStamp(value);
                if (getTime != null) { statement.setTimestamp(1, getTime); }
                else { statement.setString(1, value); }
            }
            statement.execute();

        } catch (Exception e) { this.plugin.getLogger().warning(e.getMessage()); e.printStackTrace(); }
    }
    public void insertIntoTable(String Table, String[] where, String[] value) {
        if (!(where.length >= 1 && where.length == value.length)) { return; }
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= value.length; i++) { sb.append("?").append(","); }
            String query = "INSERT INTO " + Table + " (" + String.join(",", where) + ") VALUES (" + sb.toString().substring(0, sb.length()-1) + ")";
            PreparedStatement statement = dbConnection.prepareStatement(query);
            for (int i = 1; i <= value.length; i++) {
                try { statement.setInt(i, Integer.parseInt(value[i-1])); }
                catch (Exception e) {
                    Timestamp getTime = this.config.parseTimeStamp(value[i-1]);
                    if (getTime != null) { statement.setTimestamp(i, getTime); }
                    else { statement.setString(i, value[i-1]); }
                }
            }
            statement.execute();

        } catch (Exception e) { this.plugin.getLogger().warning(e.getMessage()); e.printStackTrace(); }
    }

    public void updateIntoTable(String Table, String set, String setValue, String where, String value) {
        try {
            StringBuilder sb1 = new StringBuilder();
            String query = "UPDATE " + Table + " SET " + set + " = ? WHERE " + where + "=?";
            PreparedStatement statement = dbConnection.prepareStatement(query);
            try { statement.setInt(1, Integer.parseInt(setValue)); }
            catch (Exception e) {
                Timestamp getTime = this.config.parseTimeStamp(setValue);
                if (getTime != null) { statement.setTimestamp(1, getTime); }
                else { statement.setString(1, setValue); }
            }
            try { statement.setInt(2, Integer.parseInt(value)); }
            catch (Exception e) {
                Timestamp getTime = this.config.parseTimeStamp(value);
                if (getTime != null) { statement.setTimestamp(2, getTime); }
                else { statement.setString(2, value); }
            }
            statement.execute();

        } catch (Exception e) { this.plugin.getLogger().warning(e.getMessage()); e.printStackTrace(); }
    }
    public void updateIntoTable(String Table, String[] set, String[] setValue, String where, String value) {
        if (!(set.length >= 1 && set.length == setValue.length)) { return; }
        try {
            StringBuilder sb1 = new StringBuilder();
            for (int i = 0; i < set.length; i++) { sb1.append(set[i]).append("=?").append(","); }
            String query = "UPDATE " + Table + " SET " + sb1.toString().substring(0, sb1.length()-1) + " WHERE " + where + "=?";
            PreparedStatement statement = dbConnection.prepareStatement(query);
            int i = 1;
            for (i = 1; i <= setValue.length; i++) {
                try { statement.setInt(i, Integer.parseInt(setValue[i-1])); }
                catch (Exception e) {
                    Timestamp getTime = this.config.parseTimeStamp(setValue[i - 1]);
                    if (getTime != null) { statement.setTimestamp(i, getTime); }
                    else { statement.setString(i, setValue[i - 1]); }
                }
            }
            try { statement.setInt(i, Integer.parseInt(value)); }
            catch (Exception e) {
                Timestamp getTime = this.config.parseTimeStamp(value);
                if (getTime != null) { statement.setTimestamp(i, getTime); }
                else { statement.setString(i, value); }
            }
            statement.execute();

        } catch (Exception e) { this.plugin.getLogger().warning(e.getMessage()); e.printStackTrace(); }
    }
    public void updateIntoTable(String Table, String[] set, String[] setValue, String[] where, String[] value) {
        if (!(where.length >= 1 && where.length == value.length)) { return; }
        if (!(set.length >= 1 && set.length == setValue.length)) { return; }
        try {
            StringBuilder sb1 = new StringBuilder();
            StringBuilder sb2 = new StringBuilder();
            for (int i = 0; i < set.length; i++) { sb1.append(set[i]).append("=?").append(","); }
            for (int i = 0; i < where.length; i++) { sb2.append(where[i]).append("=?").append(","); }
            String query = "UPDATE " + Table + " SET " + sb1.toString().substring(0, sb1.length()-1) + " WHERE " + sb2.toString().substring(0, sb2.length()-1);
            PreparedStatement statement = dbConnection.prepareStatement(query);
            int i = 1;
            for (i = 1; i <= setValue.length; i++) {
                try { statement.setInt(i, Integer.parseInt(setValue[i-1])); }
                catch (Exception e) {
                    Timestamp getTime = this.config.parseTimeStamp(setValue[i-1]);
                    if (getTime != null) { statement.setTimestamp(i, getTime); }
                    else { statement.setString(i, setValue[i-1]); }
                }
            }
            for (int j = 0; j <= value.length; j++) {
                i += 1;
                try { statement.setInt(i, Integer.parseInt(value[j])); }
                catch (Exception e) {
                    Timestamp getTime = this.config.parseTimeStamp(value[j]);
                    if (getTime != null) { statement.setTimestamp(i, getTime); }
                    else { statement.setString(i, value[j]); }
                }
            }
            statement.execute();

        } catch (Exception e) { this.plugin.getLogger().warning(e.getMessage()); e.printStackTrace(); }
    }
}
