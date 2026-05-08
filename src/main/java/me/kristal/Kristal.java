package me.kristal;

import me.kristal.commands.KristalCommand;
import me.kristal.database.DatabaseManager;
import me.kristal.placeholder.KristalExpansion;
import me.kristal.utils.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Kristal extends JavaPlugin {

    private static Kristal instance;
    private DatabaseManager databaseManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;

        // Config yükle
        saveDefaultConfig();
        configManager = new ConfigManager(this);

        // Veritabanı başlat
        databaseManager = new DatabaseManager(this);
        databaseManager.setup();

        // Komutlar
        KristalCommand kristalCommand = new KristalCommand(this);
        getCommand("kristal").setExecutor(kristalCommand);
        getCommand("kristal").setTabCompleter(kristalCommand);

        // PlaceholderAPI
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new KristalExpansion(this).register();
        }

        getLogger().info("Kristal plugini başarıyla aktif edildi!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    public static Kristal getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
