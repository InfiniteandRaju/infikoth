package com.groupez.koth;

import com.groupez.koth.hill.Hill;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class HillListener implements Listener {

    private final KotH plugin;

    public HillListener(KotH plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Check if player changed chunks for performance
        if (event.getFrom().getChunk().equals(event.getTo().getChunk())) {
            return;
        }

        for (Hill hill : plugin.getHillManager().getHills()) {
            if (hill.isActive()) {
                boolean wasInHill = hill.isPlayerInHill(player);
                boolean isNowInHill = hill.getCenter().getWorld().equals(player.getWorld()) &&
                        hill.getCenter().distance(player.getLocation()) <= hill.getRadius();

                // Handle player entering/leaving hill
                if (!wasInHill && isNowInHill) {
                    hill.addPlayer(player);
                } else if (wasInHill && !isNowInHill) {
                    hill.removePlayer(player);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // If player was capturing a hill, stop their capture
        for (Hill hill : plugin.getHillManager().getHills()) {
            if (hill.isActive() && hill.getCurrentCapturer() != null &&
                    hill.getCurrentCapturer().getUniqueId().equals(player.getUniqueId())) {
                hill.setCapturer(null);
            }
            hill.removePlayer(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Update boss bars for the joining player
        plugin.getBossBarManager().updateAllBossBars();
    }
}