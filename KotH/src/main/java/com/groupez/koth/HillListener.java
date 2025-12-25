package com.groupez.koth;

import com.groupez.koth.hill.Hill;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

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
            // Even if same chunk, they might have crossed the boundary of a small hill
            // But for optimization we can skip, or maybe checking distance every move is fine?
            // Original code skipped. Let's keep it safe for now but maybe remove optimization if hills are small.
            // Actually, for precise cuboids, check every move is better, or at least full block change.
             if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                 event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                 event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
                 return;
             }
        }

        for (Hill hill : plugin.getHillManager().getHills()) {
            if (hill.isActive()) {
                boolean wasRecordedInHill = hill.getPlayersInHill().contains(player.getUniqueId());
                boolean isPhysicallyInHill = hill.isPlayerInHill(player);

                // Handle player entering/leaving hill
                if (!wasRecordedInHill && isPhysicallyInHill) {
                    hill.addPlayer(player);
                } else if (wasRecordedInHill && !isPhysicallyInHill) {
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