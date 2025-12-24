package com.groupez.koth.config;

import com.groupez.koth.KotH;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class ConfigManager {

    private final KotH plugin;
    private FileConfiguration config;

    public ConfigManager(KotH plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        setupDefaults();
    }

    private void setupDefaults() {
        // Messages section
        config.addDefault("messages.prefix", "&6[&cKoTH&6]");
        config.addDefault("messages.hill-start", "&e%hill% &ahas started! &7(&6%time%s&7)");
        config.addDefault("messages.hill-captured", "&a%player% &7has successfully captured &e%hill%&7! &8⛏️");
        config.addDefault("messages.hill-stopped", "&e%hill% &chas been stopped!");
        config.addDefault("messages.hill-contested", "&cHill contested by multiple players!");
        config.addDefault("messages.start-capturing", "&e%player% &7started capturing &a%hill%&7!");
        config.addDefault("messages.capture-progress", "&a%hill% &7- &e%player% &7capturing: &6%time%s left");
        config.addDefault("messages.capture-interrupted", "&cCapture interrupted! %player% left the hill.");
        config.addDefault("messages.reward-message", "&aYou received rewards for capturing &e%hill%&a!");
        config.addDefault("messages.reward-broadcast", "&7%player% &areceived rewards for capturing &e%hill%&a!");
        config.addDefault("messages.actionbar-capturing", "&aCapturing &e%hill%&a: &6%time%s left");
        config.addDefault("messages.actionbar-waiting", "&eStand in hill to capture %hill%");
        config.addDefault("messages.bossbar-capturing", "&6KoTH: &f%hill% &a» &b%player% &e- %time%s");
        config.addDefault("messages.bossbar-waiting", "&6KoTH: &f%hill% &e- Waiting for capturer");
        config.addDefault("messages.bossbar-inactive", "&6KoTH: &f%hill%");

        // Titles section
        config.addDefault("titles.enabled", true);
        config.addDefault("titles.start-title", "&6&lKoTH STARTED!");
        config.addDefault("titles.start-subtitle", "&e%hill% &7is now active!");
        config.addDefault("titles.capture-start-title", "&a&lSTARTED CAPTURING!");
        config.addDefault("titles.capture-start-subtitle", "&7Stay in the hill area");
        config.addDefault("titles.win-title", "&6&lHILL CAPTURED!");
        config.addDefault("titles.win-subtitle", "&a%player% &7won &e%hill%&7!");
        config.addDefault("titles.contest-title", "&c&lHILL CONTESTED!");
        config.addDefault("titles.contest-subtitle", "&7Multiple players detected");

        // Bossbar section
        config.addDefault("bossbar.enabled", true);
        config.addDefault("bossbar.update-interval", 1);

        // Rewards section
        config.addDefault("rewards.enabled", true);
        config.addDefault("rewards.broadcast-rewards", true);
        config.addDefault("rewards.commands", new String[]{
                "give %player% minecraft:diamond 5",
                "give %player% minecraft:emerald 3",
                "give %player% minecraft:gold_ingot 10",
                "xp give %player% 50 levels",
                "effect give %player% minecraft:speed 30 1"
        });

        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    // Helper method to translate color codes
    private String color(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    // Message getters
    public String getPrefix() {
        return color(config.getString("messages.prefix", "&6[&cKoTH&6]"));
    }

    public String getMessage(String key, String... replacements) {
        String message = config.getString("messages." + key, "");
        message = color(message);

        if (replacements.length > 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                if (i + 1 < replacements.length) {
                    message = message.replace(replacements[i], replacements[i + 1]);
                }
            }
        }
        return message;
    }

    public String getTitle(String key, String... replacements) {
        String title = config.getString("titles." + key, "");
        title = color(title);

        if (replacements.length > 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                if (i + 1 < replacements.length) {
                    title = title.replace(replacements[i], replacements[i + 1]);
                }
            }
        }
        return title;
    }

    public boolean areTitlesEnabled() {
        return config.getBoolean("titles.enabled", true);
    }

    public boolean isBossBarEnabled() {
        return config.getBoolean("bossbar.enabled", true);
    }

    public boolean areRewardsEnabled() {
        return config.getBoolean("rewards.enabled", true);
    }

    public boolean shouldBroadcastRewards() {
        return config.getBoolean("rewards.broadcast-rewards", true);
    }

    // Reward methods
    public void giveRewards(Player player, String hillName) {
        if (!areRewardsEnabled()) return;

        // Send personal reward message
        String rewardMsg = getMessage("reward-message",
                "%player%", player.getName(),
                "%hill%", hillName);
        player.sendMessage(getPrefix() + " " + rewardMsg);

        // Execute reward commands
        for (String cmd : getRewardCommands()) {
            String processedCmd = cmd.replace("%player%", player.getName())
                    .replace("%hill%", hillName);

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCmd);
        }
    }

    public List<String> getRewardCommands() {
        return config.getStringList("rewards.commands");
    }

    // Action bar helper
    public void sendActionBar(Player player, String message) {
        String coloredMessage = color(message);
        player.sendActionBar(coloredMessage);
    }
}