package com.groupez.koth.hill;

import com.groupez.koth.KotH;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Hill {

    private final String name;
    private final Location center;
    private final int radius;
    private final int captureTime;
    private final KotH plugin;

    private boolean active = false;
    private Player currentCapturer = null;
    private int timeLeft = 0;
    private final Set<UUID> playersInHill = new HashSet<>();
    private UUID capturingPlayerId = null;

    public Hill(KotH plugin, String name, Location center, int radius, int captureTime) {
        this.plugin = plugin;
        this.name = name;
        this.center = center;
        this.radius = radius;
        this.captureTime = captureTime;
    }

    public void start() {
        this.active = true;
        this.timeLeft = captureTime;
        this.currentCapturer = null;
        this.capturingPlayerId = null;
        this.playersInHill.clear();
    }

    public void stop() {
        this.active = false;
        this.currentCapturer = null;
        this.timeLeft = 0;
        this.capturingPlayerId = null;
        this.playersInHill.clear();
    }

    public void setCapturer(Player player) {
        this.currentCapturer = player;
        this.capturingPlayerId = player.getUniqueId();
        this.timeLeft = captureTime;
    }

    public void updateCapture() {
        if (active && currentCapturer != null && timeLeft > 0) {
            timeLeft--;

            // Check if capturer is still in hill
            if (!isPlayerInHill(currentCapturer)) {
                currentCapturer = null;
                capturingPlayerId = null;
                return;
            }

            if (timeLeft <= 0) {
                completeCapture();
            }
        }
    }

    private void completeCapture() {
        if (currentCapturer != null) {
            // BROADCAST WIN MESSAGE TO ALL PLAYERS IN WORLD
            String winMessage = plugin.getConfigManager().getMessage("hill-captured",
                    "%player%", currentCapturer.getName(),
                    "%hill%", name);

            // Send to all players in the world
            for (Player player : center.getWorld().getPlayers()) {
                player.sendMessage(plugin.getConfigManager().getPrefix() + " " + winMessage);
            }

            // Send titles to all players if enabled
            if (plugin.getConfigManager().areTitlesEnabled()) {
                String title = plugin.getConfigManager().getTitle("win-title",
                        "%player%", currentCapturer.getName(),
                        "%hill%", name);

                String subtitle = plugin.getConfigManager().getTitle("win-subtitle",
                        "%player%", currentCapturer.getName(),
                        "%hill%", name);

                for (Player player : center.getWorld().getPlayers()) {
                    player.sendTitle(title, subtitle, 10, 70, 20);
                }
            }

            // Give rewards to the winner
            plugin.getConfigManager().giveRewards(currentCapturer, name);

            // Broadcast reward message if enabled
            if (plugin.getConfigManager().shouldBroadcastRewards()) {
                String rewardMsg = plugin.getConfigManager().getMessage("reward-broadcast",
                        "%player%", currentCapturer.getName(),
                        "%hill%", name);

                for (Player player : center.getWorld().getPlayers()) {
                    player.sendMessage(plugin.getConfigManager().getPrefix() + " " + rewardMsg);
                }
            }
        }

        stop();
    }

    public void addPlayer(Player player) {
        playersInHill.add(player.getUniqueId());
    }

    public void removePlayer(Player player) {
        playersInHill.remove(player.getUniqueId());
        if (currentCapturer != null && currentCapturer.getUniqueId().equals(player.getUniqueId())) {
            currentCapturer = null;
            capturingPlayerId = null;

            // Broadcast interruption message
            String interruptMsg = plugin.getConfigManager().getMessage("capture-interrupted",
                    "%player%", player.getName());

            for (Player p : center.getWorld().getPlayers()) {
                p.sendMessage(plugin.getConfigManager().getPrefix() + " " + interruptMsg);
            }
        }
    }

    public Set<UUID> getPlayersInHill() {
        return new HashSet<>(playersInHill);
    }

    public boolean isPlayerInHill(Player player) {
        if (!active) return false;
        if (!player.isOnline()) return false;

        Location playerLoc = player.getLocation();
        if (!playerLoc.getWorld().equals(center.getWorld())) return false;

        double distance = playerLoc.distance(center);
        return distance <= radius;
    }

    public void updatePlayersInHill() {
        playersInHill.clear();
        for (Player player : center.getWorld().getPlayers()) {
            if (isPlayerInHill(player)) {
                playersInHill.add(player.getUniqueId());
            }
        }
    }

    // Getters
    public String getName() { return name; }
    public Location getCenter() { return center; }
    public int getRadius() { return radius; }
    public int getCaptureTime() { return captureTime; }
    public boolean isActive() { return active; }
    public Player getCurrentCapturer() { return currentCapturer; }
    public int getTimeLeft() { return timeLeft; }
    public UUID getCapturingPlayerId() { return capturingPlayerId; }
}
