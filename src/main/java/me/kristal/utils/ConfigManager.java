package me.kristal.utils;

import me.kristal.Kristal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final Kristal plugin;
    private final Map<String, String> messages = new HashMap<>();

    public ConfigManager(Kristal plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        messages.clear();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        if (config.getConfigurationSection("messages") != null) {
            for (String key : config.getConfigurationSection("messages").getKeys(false)) {
                messages.put(key, config.getString("messages." + key));
            }
        }
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "Message not found: " + key);
    }

    public Component getComponentMessage(String key) {
        String msg = getMessage(key);
        String prefix = messages.getOrDefault("prefix", "");
        // Eğer key prefix ise sadece mesajı döndür (prefix ekleme)
        if (key.equals("prefix")) {
            return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
        }
        return LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + msg);
    }

    public long getMaxKristal() {
        return plugin.getConfig().getLong("settings.max-kristal", 1000000000L);
    }

    public long getMinKristal() {
        return plugin.getConfig().getLong("settings.min-kristal", 0L);
    }
}
