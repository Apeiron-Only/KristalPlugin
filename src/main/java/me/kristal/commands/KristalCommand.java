package me.kristal.commands;

import me.kristal.Kristal;
import me.kristal.utils.KristalFormatter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class KristalCommand implements CommandExecutor, TabCompleter {

    private final Kristal plugin;
    private final List<String> subs = Arrays.asList("give", "take", "set", "reset", "reload", "view");

    public KristalCommand(Kristal plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getConfigManager().getComponentMessage("only-players"));
                return true;
            }
            showBalance(player, player);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("reload")) {
            if (!sender.hasPermission("kristal.reload")) {
                sender.sendMessage(plugin.getConfigManager().getComponentMessage("no-permission"));
                return true;
            }
            plugin.getConfigManager().loadMessages();
            sender.sendMessage(plugin.getConfigManager().getComponentMessage("reload-success"));
            return true;
        }

        if (sub.equals("view")) {
            if (!sender.hasPermission("kristal.view")) {
                sender.sendMessage(plugin.getConfigManager().getComponentMessage("no-permission"));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(plugin.getConfigManager().getComponentMessage("usage"));
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            showBalance(sender, target);
            return true;
        }

        if (!sender.hasPermission("kristal." + sub)) {
            sender.sendMessage(plugin.getConfigManager().getComponentMessage("no-permission"));
            return true;
        }

        switch (sub) {
            case "give" -> handleGive(sender, args);
            case "take" -> handleTake(sender, args);
            case "set" -> handleSet(sender, args);
            case "reset" -> handleReset(sender, args);
            default -> sender.sendMessage(plugin.getConfigManager().getComponentMessage("usage"));
        }

        return true;
    }

    private void showBalance(CommandSender viewer, OfflinePlayer target) {
        long bal = plugin.getDatabaseManager().getBalance(target.getUniqueId());
        String key = (viewer instanceof Player p && p.getUniqueId().equals(target.getUniqueId())) ? "balance-self" : "balance-other";
        
        String msg = plugin.getConfigManager().getMessage(key)
                .replace("%kristal_balance_formatted%", KristalFormatter.formatShort(bal))
                .replace("%target%", target.getName() != null ? target.getName() : "Unknown");
        
        viewer.sendMessage(plugin.getConfigManager().getComponentMessage("prefix").append(
            net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(msg)
        ));
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.getConfigManager().getComponentMessage("usage"));
            return;
        }

        long amount = parseAmount(args[2]);
        if (amount <= 0) {
            sender.sendMessage(plugin.getConfigManager().getComponentMessage("invalid-amount"));
            return;
        }

        String targetName = args[1];
        if (targetName.equals("*")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                addBalance(p.getUniqueId(), p.getName(), amount);
                p.sendMessage(formatMessage("give-receive", amount));
            }
            sender.sendMessage(formatMessage("broadcast-give", amount));
        } else if (targetName.equals("**")) {
            plugin.getDatabaseManager().updateAllBalances(amount, false);
            sender.sendMessage(formatMessage("broadcast-give", amount));
        } else {
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
            addBalance(target.getUniqueId(), target.getName(), amount);
            sender.sendMessage(formatMessage("give-success", amount, target.getName()));
            if (target.isOnline()) {
                target.getPlayer().sendMessage(formatMessage("give-receive", amount));
            }
        }
    }

    private void handleTake(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.getConfigManager().getComponentMessage("usage"));
            return;
        }

        long amount = parseAmount(args[2]);
        if (amount <= 0) {
            sender.sendMessage(plugin.getConfigManager().getComponentMessage("invalid-amount"));
            return;
        }

        String targetName = args[1];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        long current = plugin.getDatabaseManager().getBalance(target.getUniqueId());

        if (current < amount) {
            sender.sendMessage(plugin.getConfigManager().getComponentMessage("insufficient-funds"));
            return;
        }

        setBalance(target.getUniqueId(), target.getName(), current - amount);
        sender.sendMessage(formatMessage("take-success", amount, target.getName()));
        if (target.isOnline()) {
            target.getPlayer().sendMessage(formatMessage("take-receive", amount));
        }
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.getConfigManager().getComponentMessage("usage"));
            return;
        }

        long amount = parseAmount(args[2]);
        if (amount < 0) {
            sender.sendMessage(plugin.getConfigManager().getComponentMessage("invalid-amount"));
            return;
        }

        String targetName = args[1];
        if (targetName.equals("*")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                setBalance(p.getUniqueId(), p.getName(), amount);
                p.sendMessage(formatMessage("set-receive", amount));
            }
            sender.sendMessage(formatMessage("broadcast-set", amount));
        } else if (targetName.equals("**")) {
            plugin.getDatabaseManager().updateAllBalances(amount, true);
            sender.sendMessage(formatMessage("broadcast-set", amount));
        } else {
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
            setBalance(target.getUniqueId(), target.getName(), amount);
            sender.sendMessage(formatMessage("set-success", amount, target.getName()));
            if (target.isOnline()) {
                target.getPlayer().sendMessage(formatMessage("set-receive", amount));
            }
        }
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getConfigManager().getComponentMessage("usage"));
            return;
        }

        String targetName = args[1];
        if (targetName.equals("*")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                setBalance(p.getUniqueId(), p.getName(), 0);
                p.sendMessage(formatMessage("reset-receive", 0));
            }
            sender.sendMessage(formatMessage("broadcast-reset", 0));
        } else if (targetName.equals("**")) {
            plugin.getDatabaseManager().updateAllBalances(0, true);
            sender.sendMessage(formatMessage("broadcast-reset", 0));
        } else {
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
            setBalance(target.getUniqueId(), target.getName(), 0);
            sender.sendMessage(formatMessage("reset-success", 0, target.getName()));
            if (target.isOnline()) {
                target.getPlayer().sendMessage(formatMessage("reset-receive", 0));
            }
        }
    }

    private long parseAmount(String s) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void addBalance(UUID uuid, String name, long amount) {
        long current = plugin.getDatabaseManager().getBalance(uuid);
        setBalance(uuid, name, current + amount);
    }

    private void setBalance(UUID uuid, String name, long amount) {
        long max = plugin.getConfigManager().getMaxKristal();
        long min = plugin.getConfigManager().getMinKristal();
        long newBalance = Math.max(min, Math.min(max, amount));
        plugin.getDatabaseManager().setBalance(uuid, name, newBalance);
    }

    private net.kyori.adventure.text.Component formatMessage(String key, long amount) {
        return formatMessage(key, amount, null);
    }

    private net.kyori.adventure.text.Component formatMessage(String key, long amount, String target) {
        String msg = plugin.getConfigManager().getMessage(key)
                .replace("%amount%", KristalFormatter.formatShort(amount));
        if (target != null) msg = msg.replace("%target%", target);
        
        return plugin.getConfigManager().getComponentMessage("prefix").append(
            net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(msg)
        );
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return subs.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .filter(s -> sender.hasPermission("kristal." + s))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (Arrays.asList("give", "set", "reset", "take", "view").contains(sub)) {
                List<String> players = new ArrayList<>();
                if (!sub.equals("view") && !sub.equals("take")) {
                    players.add("*");
                    players.add("**");
                }
                players.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
                return players.stream()
                        .filter(p -> p.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
}
