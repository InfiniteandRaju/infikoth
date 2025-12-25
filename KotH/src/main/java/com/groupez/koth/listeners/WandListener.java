package com.groupez.koth.listeners;

import com.groupez.koth.KotH;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class WandListener implements Listener {

    private final KotH plugin;

    public WandListener(KotH plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND)
            return; // Ignore offhand to prevent double firing

        Player player = event.getPlayer();
        if (!plugin.getSelectionManager().isWand(player.getInventory().getItemInMainHand()))
            return;

        event.setCancelled(true); // Prevent breaking/placing blocks

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            plugin.getSelectionManager().setPos1(player, event.getClickedBlock().getLocation());
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            plugin.getSelectionManager().setPos2(player, event.getClickedBlock().getLocation());
        }
    }
}
