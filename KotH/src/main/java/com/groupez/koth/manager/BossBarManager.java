package com.groupez.koth;

import com.groupez.koth.manager.BossBarManager;
import com.groupez.koth.hill.HillManager;
import com.groupez.koth.commands.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;

public class KotH extends JavaPlugin {
    
    private static KotH instance;
    private BossBarManager bossBarManager;
    private HillManager hillManager;
    private CommandManager commandManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config if it doesn't exist
        this.saveDefaultConfig();
        
        // Initialize managers in the CORRECT ORDER
        // 1. First create BossBarManager
        this.bossBarManager = new BossBarManager(this);
        
        // 2. Then create HillManager (which needs BossBarManager)
        this.hillManager = new HillManager(this);
        
        // 3. Then create CommandManager
        this.commandManager = new CommandManager(this);
        
        // 4. Register command
        this.getCommand("infikoth").setExecutor(commandManager);
        this.getCommand("infikoth").setTabCompleter(commandManager);
        
        // 5. Load hills AFTER everything is initialized
        // This should be called from HillManager constructor or a separate init method
        // If loadHillsFromConfig() is called in HillManager constructor, that's fine now
        
        getLogger().info("§a========================================");
        getLogger().info("§aInfiKoth v1.0.0 has been enabled!");
        getLogger().info("§aUse §e/infikoth help §afor commands");
        getLogger().info("§a========================================");
    }
    
    @Override
    public void onDisable() {
        // Clean up boss bars when disabling
        if (bossBarManager != null) {
            bossBarManager.cleanup();
        }
        
        getLogger().info("§cInfiKoth has been disabled!");
    }
    
    // Getters for other classes to access managers
    public static KotH getInstance() {
        return instance;
    }
    
    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }
    
    public HillManager getHillManager() {
        return hillManager;
    }
    
    public CommandManager getCommandManager() {
        return commandManager;
    }
}
