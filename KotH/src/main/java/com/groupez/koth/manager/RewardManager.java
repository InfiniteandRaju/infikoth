package com.groupez.koth.manager;

import com.groupez.koth.KotH;
import com.groupez.koth.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardManager {

    private final KotH plugin;
    private File rewardsFile;
    private FileConfiguration rewardsConfig;
    private final Map<String, List<ItemStack>> hillRewards = new HashMap<>();

    public RewardManager(KotH plugin) {
        this.plugin = plugin;
        loadRewards();
    }

    private void loadRewards() {
        rewardsFile = new File(plugin.getDataFolder(), "rewards.yml");
        if (!rewardsFile.exists()) {
            try {
                rewardsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create rewards.yml!");
                e.printStackTrace();
            }
        }

        rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);
        hillRewards.clear();

        for (String key : rewardsConfig.getKeys(false)) {
            List<?> list = rewardsConfig.getList(key);
            if (list != null) {
                List<ItemStack> items = new ArrayList<>();
                for (Object obj : list) {
                    if (obj instanceof ItemStack) {
                        items.add((ItemStack) obj);
                    }
                }
                hillRewards.put(key, items);
            }
        }
    }

    public void saveRewards() {
        for (Map.Entry<String, List<ItemStack>> entry : hillRewards.entrySet()) {
            rewardsConfig.set(entry.getKey(), entry.getValue());
        }

        try {
            rewardsConfig.save(rewardsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save rewards.yml!");
            e.printStackTrace();
        }
    }

    public void openEditor(Player player, String hillName) {
        Inventory inv = Bukkit.createInventory(null, 27, ColorUtils.color("&8Reward Editor: &e" + hillName));
        
        if (hillRewards.containsKey(hillName)) {
            for (ItemStack item : hillRewards.get(hillName)) {
                if (item != null) {
                    inv.addItem(item);
                }
            }
        }

        player.openInventory(inv);
    }

    public void saveRewardsFromEditor(String hillName, Inventory inv) {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : inv.getContents()) {
            if (item != null) {
                items.add(item);
            }
        }
        hillRewards.put(hillName, items);
        saveRewards();
    }

    public void giveRewards(Player player, String hillName) {
        if (hillRewards.containsKey(hillName)) {
            List<ItemStack> items = hillRewards.get(hillName);
            boolean inventoryFull = false;

            for (ItemStack item : items) {
                if (player.getInventory().firstEmpty() != -1) {
                    player.getInventory().addItem(item.clone());
                } else {
                    inventoryFull = true;
                    player.getWorld().dropItemNaturally(player.getLocation(), item.clone());
                }
            }
            
            if (inventoryFull) {
                player.sendMessage(ColorUtils.color("&cYour inventory was full, so some rewards were dropped!"));
            }
        }
    }
}
