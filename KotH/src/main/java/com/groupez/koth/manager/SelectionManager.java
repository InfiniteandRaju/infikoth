package com.groupez.koth.manager;

import com.groupez.koth.KotH;
import com.groupez.koth.utils.ColorUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SelectionManager {

    private final KotH plugin;
    private final Map<UUID, Location> pos1 = new HashMap<>();
    private final Map<UUID, Location> pos2 = new HashMap<>();
    private final ItemStack wandItem;

    public SelectionManager(KotH plugin) {
        this.plugin = plugin;
        this.wandItem = createWand();
    }

    private ItemStack createWand() {
        ItemStack wand = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(ColorUtils.color("&6&lInfikoth Wand"));
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtils.color("&7Left-click to set &ePos1"));
        lore.add(ColorUtils.color("&7Right-click to set &ePos2"));
        meta.setLore(lore);
        wand.setItemMeta(meta);
        return wand;
    }

    public void setPos1(Player player, Location loc) {
        pos1.put(player.getUniqueId(), loc);
        player.sendMessage(
                plugin.getConfigManager().getPrefix() + " " + ColorUtils.color("&aPos1 set at " + formatLoc(loc)));
    }

    public void setPos2(Player player, Location loc) {
        pos2.put(player.getUniqueId(), loc);
        player.sendMessage(
                plugin.getConfigManager().getPrefix() + " " + ColorUtils.color("&aPos2 set at " + formatLoc(loc)));
    }

    public Location getPos1(Player player) {
        return pos1.get(player.getUniqueId());
    }

    public Location getPos2(Player player) {
        return pos2.get(player.getUniqueId());
    }

    public boolean hasSelection(Player player) {
        return pos1.containsKey(player.getUniqueId()) && pos2.containsKey(player.getUniqueId());
    }

    public ItemStack getWand() {
        return wandItem.clone();
    }

    public boolean isWand(ItemStack item) {
        if (item == null || item.getType() != Material.GOLDEN_AXE)
            return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName())
            return false;
        return item.getItemMeta().getDisplayName().equals(ColorUtils.color("&6&lInfikoth Wand"));
    }

    private String formatLoc(Location loc) {
        return String.format("&7(X: %d, Y: %d, Z: %d)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
}
