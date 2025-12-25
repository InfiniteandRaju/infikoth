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
        config.addDefault("messages.prefix", "&#FFD700&lKotH &8»&r");
        config.addDefault("messages.hill-start", "&e%hill% &fhas started! &7(&a%time%s&7)");
        config.addDefault("messages.hill-captured", "&6%player% &fhas captured &e%hill%&f! &8⛏");
        config.addDefault("messages.hill-stopped", "&c%hill% &fhas been stopped!");
        config.addDefault("messages.hill-contested", "&#FF4444&lCONTESTED! &fMultiply players on the hill!");
        config.addDefault("messages.start-capturing", "&e%player% &fis capturing &6%hill%&f!");
        config.addDefault("messages.capture-progress", "&6%hill% &8» &e%player% &7: &f%time%s");
        config.addDefault("messages.capture-interrupted", "&cCapture interrupted! &f%player% left.");
        config.addDefault("messages.reward-message", "&a&lREWARD! &fYou won &6%hill%&f!");
        config.addDefault("messages.reward-broadcast", "&6%player% &fwon &e%hill% &fand received rewards!");
        config.addDefault("messages.actionbar-capturing", "&#FFD700Capturing &f%hill%&8: &a%time%s");
        config.addDefault("messages.actionbar-waiting", "&fStand on &e%hill% &fto capture!");
        config.addDefault("messages.bossbar-capturing", "&#FFD700KoTH &8» &f%hill% &8[&a%player%&8] &f- &e%time%s");
        config.addDefault("messages.bossbar-waiting", "&#FFD700KoTH &8» &f%hill% &f- &7Waiting...");
        config.addDefault("messages.bossbar-inactive", "&#FFD700KoTH &8» &f%hill% &7(Inactive)");

        // Titles section
        config.addDefault("titles.enabled", true);
        config.addDefault("titles.start-title", "&#FFD700&lKoTH EVENT");
        config.addDefault("titles.start-subtitle", "&fThe &e%hill% &fhas started!");
        config.addDefault("titles.capture-start-title", "&#44FF44&lCAPTURING");
        config.addDefault("titles.capture-start-subtitle", "&7Hold the point!");
        config.addDefault("titles.win-title", "&#FFD700&lVICTORY!");
        config.addDefault("titles.win-subtitle", "&6%player% &fcaptured &e%hill%&f!");
        config.addDefault("titles.contest-title", "&#FF4444&lCONTESTED");
        config.addDefault("titles.contest-subtitle", "&fClear the hill!");

        // Bossbar section
        config.addDefault("bossbar.enabled", true);
        config.addDefault("bossbar.update-interval", 1);
        config.addDefault("bossbar.color", "YELLOW");
        config.addDefault("bossbar.style", "SOLID");

        // Rewards section
        config.addDefault("rewards.enabled", true);
        config.addDefault("rewards.broadcast-rewards", true);
        config.addDefault("rewards.commands", new String[] {
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
        return com.groupez.koth.utils.ColorUtils.color(text);
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
        if (!areRewardsEnabled())
            return;

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