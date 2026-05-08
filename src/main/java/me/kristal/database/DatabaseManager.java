package me.kristal.database;

import me.kristal.Kristal;

import java.io.File;
import java.sql.*;
import java.util.*;

public class DatabaseManager {

    private final Kristal plugin;
    private Connection connection;

    public DatabaseManager(Kristal plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) dataFolder.mkdirs();

            File dbFile = new File(dataFolder, "database.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS kristal_data (" +
                        "uuid VARCHAR(36) PRIMARY KEY," +
                        "username VARCHAR(16)," +
                        "balance BIGINT DEFAULT 0)");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Veritabanı bağlantısı başarısız: " + e.getMessage());
        }
    }

    public long getBalance(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT balance FROM kristal_data WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong("balance");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setBalance(UUID uuid, String username, long amount) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO kristal_data (uuid, username, balance) VALUES (?, ?, ?) " +
                        "ON CONFLICT(uuid) DO UPDATE SET balance = ?, username = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, username);
            ps.setLong(3, amount);
            ps.setLong(4, amount);
            ps.setString(5, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateAllBalances(long amount, boolean set) {
        String query = set ? "UPDATE kristal_data SET balance = ?" : "UPDATE kristal_data SET balance = balance + ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, amount);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Long> getTopBalances(int limit) {
        Map<String, Long> top = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT username, balance FROM kristal_data ORDER BY balance DESC LIMIT ?")) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                top.put(rs.getString("username"), rs.getLong("balance"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
