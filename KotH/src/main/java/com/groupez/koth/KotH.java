package com.groupez.koth;

import com.groupez.koth.commands.KothCommand;
import com.groupez.koth.config.ConfigManager;
import com.groupez.koth.hill.HillManager;
import com.groupez.koth.manager.BossBarManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class KotH extends JavaPlugin {

    private static KotH instance;
    private ConfigManager configManager;
    private HillManager hillManager;
    private BossBarManager bossBarManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.hillManager = new HillManager(this);
        this.bossBarManager = new BossBarManager(this);

        // Register commands
        getCommand("koth").setExecutor(new KothCommand(this));

        // Register events
        getServer().getPluginManager().registerEvents(new HillListener(this), this);

        // Start scheduler for hill updates
        startHillScheduler();

        getLogger().info("KoTH plugin has been enabled!");
        getLogger().info("Boss bar system ready - will show player name and countdown!");
    }

    @Override
    public void onDisable() {
        if (bossBarManager != null) {
            bossBarManager.cleanup();
        }
        getLogger().info("KoTH plugin has been disabled!");
    }

    private void startHillScheduler() {
        // Update hills every second (20 ticks)
        getServer().getScheduler().runTaskTimer(this, () -> {
            hillManager.updateHills();
        }, 0L, 20L);

        // Update boss bars more frequently for smooth animation (every tick)
        getServer().getScheduler().runTaskTimer(this, () -> {
            bossBarManager.updateAllBossBars();
        }, 0L, 1L);
    }

    public static KotH getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public HillManager getHillManager() {
        return hillManager;
    }

    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }
}