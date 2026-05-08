package me.kristal.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.kristal.Kristal;
import me.kristal.utils.KristalFormatter;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KristalExpansion extends PlaceholderExpansion {

    private final Kristal plugin;
    private long lastUpdate = 0;
    private List<Map.Entry<String, Long>> cachedTop = new ArrayList<>();

    public KristalExpansion(Kristal plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "kristal";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Apeiron";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equals("balance")) {
            if (player == null) return "0";
            return String.valueOf(plugin.getDatabaseManager().getBalance(player.getUniqueId()));
        }

        if (params.equals("balance_formatted")) {
            if (player == null) return "0";
            return KristalFormatter.formatShort(plugin.getDatabaseManager().getBalance(player.getUniqueId()));
        }

        // Top 20 Sıralaması
        updateCache();
        
        if (params.startsWith("vault_")) {
            String[] parts = params.split("_");
            if (parts.length < 3) return "";
            
            try {
                int index = Integer.parseInt(parts[1]) - 1;
                if (index < 0 || index >= 20) return "";
                
                if (index >= cachedTop.size()) {
                    return "---";
                }
                
                Map.Entry<String, Long> entry = cachedTop.get(index);
                if (parts[2].equals("name")) {
                    return entry.getKey() != null ? entry.getKey() : "---";
                } else if (parts[2].equals("balance")) {
                    return KristalFormatter.formatShort(entry.getValue());
                }
            } catch (NumberFormatException ignored) {}
        }

        return null;
    }

    private void updateCache() {
        // Her 1 saniyede bir sıralamayı güncelle (Daha dinamik)
        if (System.currentTimeMillis() - lastUpdate > 1000) {
            Map<String, Long> top = plugin.getDatabaseManager().getTopBalances(20);
            cachedTop = new ArrayList<>(top.entrySet());
            lastUpdate = System.currentTimeMillis();
        }
    }
}
