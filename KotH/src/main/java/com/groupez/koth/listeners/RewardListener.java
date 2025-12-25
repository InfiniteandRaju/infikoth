package com.groupez.koth.listeners;

import com.groupez.koth.KotH;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class RewardListener implements Listener {

    private final KotH plugin;

    public RewardListener(KotH plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        // Check if the inventory is our reward editor
        // The title format is "&8Reward Editor: &e" + hillName
        // We need to strip colors to be safe or check accurately
        
        if (ChatColor.stripColor(title).startsWith("Reward Editor: ")) {
            String hillName = ChatColor.stripColor(title).replace("Reward Editor: ", "").trim();
            Player player = (Player) event.getPlayer();
            
            plugin.getRewardManager().saveRewardsFromEditor(hillName, event.getInventory());
            player.sendMessage(plugin.getConfigManager().getPrefix() + " " + com.groupez.koth.utils.ColorUtils.color("&aRewards saved for hill: &e" + hillName));
        }
    }
}
