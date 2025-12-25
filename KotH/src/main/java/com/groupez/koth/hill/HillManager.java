package com.groupez.koth.hill;

import com.groupez.koth.KotH;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class HillManager {

    private final KotH plugin;
    private final Map<String, Hill> hills = new HashMap<>();
    private final Map<String, Set<UUID>> shownCaptureTitles = new HashMap<>(); // Track shown titles

    public HillManager(KotH plugin) {
        this.plugin = plugin;
        loadHillsFromConfig();
    }

    private void loadHillsFromConfig() {
        if (plugin.getConfig().contains("hills")) {
            for (String key : plugin.getConfig().getConfigurationSection("hills").getKeys(false)) {
                String path = "hills." + key;

                // Check if it's a cuboid hill
                if (plugin.getConfig().contains(path + ".minPoint")) {
                    Location minPoint = plugin.getConfig().getLocation(path + ".minPoint");
                    Location maxPoint = plugin.getConfig().getLocation(path + ".maxPoint");
                    int captureTime = plugin.getConfig().getInt(path + ".captureTime", 60);

                    if (minPoint != null && maxPoint != null) {
                        addCuboidHill(key, minPoint, maxPoint, captureTime, false); // false = don't save again
                    }
                } else {
                    Location center = plugin.getConfig().getLocation(path + ".center");
                    int radius = plugin.getConfig().getInt(path + ".radius", 10);
                    int captureTime = plugin.getConfig().getInt(path + ".captureTime", 60);

                    if (center != null) {
                        addRadiusHill(key, center, radius, captureTime, false);
                    }
                }
            }
        }
    }

    public void updateHills() {
        for (Hill hill : hills.values()) {
            if (hill.isActive()) {
                hill.updatePlayersInHill();
                Set<UUID> playersInHill = hill.getPlayersInHill();

                if (playersInHill.isEmpty()) {
                    if (hill.getCurrentCapturer() != null) {
                        hill.setCapturer(null);
                        broadcastToWorld(hill, "hill-contested");
                        // Clear shown titles when capture is interrupted
                        shownCaptureTitles.remove(hill.getName());
                    }
                } else if (playersInHill.size() == 1) {
                    Player player = plugin.getServer().getPlayer(playersInHill.iterator().next());
                    if (player != null && player.isOnline()) {

                        if (hill.getCurrentCapturer() == null) {
                            hill.setCapturer(player);

                            // BROADCAST start capturing to all players
                            broadcastToWorld(hill, "start-capturing",
                                    "%player%", player.getName(),
                                    "%hill%", hill.getName());

                            // Send title to all players ONLY ONCE when capture starts
                            if (plugin.getConfigManager().areTitlesEnabled()) {
                                String title = plugin.getConfigManager().getTitle("capture-start-title");
                                String subtitle = plugin.getConfigManager().getTitle("capture-start-subtitle");

                                // Track that this player has seen the title for this hill
                                String hillName = hill.getName();
                                shownCaptureTitles.computeIfAbsent(hillName, k -> new HashSet<>());

                                // Only show title if player hasn't seen it yet for this hill
                                for (Player p : hill.getCenter().getWorld().getPlayers()) {
                                    Set<UUID> shownTo = shownCaptureTitles.get(hillName);
                                    if (!shownTo.contains(p.getUniqueId())) {
                                        p.sendTitle(title, subtitle, 10, 40, 10);
                                        shownTo.add(p.getUniqueId());
                                    }
                                }
                            }
                        } else if (hill.getCurrentCapturer().getUniqueId().equals(player.getUniqueId())) {
                            hill.updateCapture();

                            // Show progress to all players every 15 seconds
                            int timeLeft = hill.getTimeLeft();
                            if (timeLeft % 15 == 0 && timeLeft > 0 && timeLeft < hill.getCaptureTime()) {
                                broadcastToWorld(hill, "capture-progress",
                                        "%hill%", hill.getName(),
                                        "%player%", player.getName(),
                                        "%time%", String.valueOf(timeLeft));
                            }

                            // Send action bar to capturer
                            String actionMsg = plugin.getConfigManager().getMessage("actionbar-capturing",
                                    "%hill%", hill.getName(),
                                    "%time%", String.valueOf(timeLeft));
                            player.sendActionBar(actionMsg);
                        } else {
                            // Different player - contest
                            broadcastToWorld(hill, "hill-contested");
                            hill.setCapturer(player);

                            // Send contest title to all players (show again since new player)
                            if (plugin.getConfigManager().areTitlesEnabled()) {
                                String title = plugin.getConfigManager().getTitle("contest-title");
                                String subtitle = plugin.getConfigManager().getTitle("contest-subtitle");

                                // Clear shown titles since it's a new capture
                                shownCaptureTitles.remove(hill.getName());

                                for (Player p : hill.getCenter().getWorld().getPlayers()) {
                                    p.sendTitle(title, subtitle, 10, 40, 10);
                                }
                            }
                        }
                    }
                } else {
                    // Multiple players
                    if (hill.getCurrentCapturer() != null) {
                        broadcastToWorld(hill, "hill-contested");
                        hill.setCapturer(null);
                        // Clear shown titles when contest happens
                        shownCaptureTitles.remove(hill.getName());
                    }
                }
            } else {
                // Hill is not active, clear shown titles
                shownCaptureTitles.remove(hill.getName());
            }
        }
    }

    private void broadcastToWorld(Hill hill, String messageKey, String... replacements) {
        String message = plugin.getConfigManager().getMessage(messageKey, replacements);
        String prefix = plugin.getConfigManager().getPrefix();

        for (Player player : hill.getCenter().getWorld().getPlayers()) {
            player.sendMessage(prefix + " " + message);
        }
    }

    public void startHill(String hillName) {
        Hill hill = hills.get(hillName);
        if (hill != null) {
            hill.start();

            // Clear shown titles when hill starts
            shownCaptureTitles.remove(hillName);

            plugin.getBossBarManager().createHillBossBar(hill);

            // BROADCAST hill start to all players
            broadcastToWorld(hill, "hill-start",
                    "%hill%", hillName,
                    "%time%", String.valueOf(hill.getCaptureTime()));

            // Send title to all players (only on start)
            if (plugin.getConfigManager().areTitlesEnabled()) {
                String title = plugin.getConfigManager().getTitle("start-title",
                        "%hill%", hillName);
                String subtitle = plugin.getConfigManager().getTitle("start-subtitle",
                        "%hill%", hillName);

                for (Player player : hill.getCenter().getWorld().getPlayers()) {
                    player.sendTitle(title, subtitle, 10, 70, 20);
                }
            }
        }
    }

    public void stopHill(String hillName) {
        Hill hill = hills.get(hillName);
        if (hill != null) {
            // BROADCAST hill stop to all players
            broadcastToWorld(hill, "hill-stopped", "%hill%", hillName);

            hill.stop();

            // Clear shown titles when hill stops
            shownCaptureTitles.remove(hillName);

            plugin.getBossBarManager().removeHillBossBar(hillName);
        }
    }

    public void addRadiusHill(String name, Location center, int radius, int captureTime, boolean save) {
        Hill hill = new Hill(plugin, name, center, radius, captureTime);
        hills.put(name, hill);

        plugin.getBossBarManager().createHillBossBar(hill);

        if (save) {
            String path = "hills." + name;
            plugin.getConfig().set(path + ".center", center);
            plugin.getConfig().set(path + ".radius", radius);
            plugin.getConfig().set(path + ".captureTime", captureTime);
            plugin.saveConfig();
        }
    }

    public void addCuboidHill(String name, Location min, Location max, int captureTime, boolean save) {
        Hill hill = new Hill(plugin, name, min, max, captureTime);
        hills.put(name, hill);

        plugin.getBossBarManager().createHillBossBar(hill);

        if (save) {
            String path = "hills." + name;
            plugin.getConfig().set(path + ".minPoint", min);
            plugin.getConfig().set(path + ".maxPoint", max);
            plugin.getConfig().set(path + ".captureTime", captureTime);
            plugin.getConfig().set(path + ".center", null); // Clear old center/radius if reusing name
            plugin.getConfig().set(path + ".radius", null);
            plugin.saveConfig();
        }
    }

    // Kept for compatibility if called from elsewhere, defaults to Radius
    public void addHill(String name, Location center, int radius, int captureTime) {
        addRadiusHill(name, center, radius, captureTime, true);
    }

    public void removeHill(String name) {
        plugin.getBossBarManager().removeHillBossBar(name);
        plugin.getConfig().set("hills." + name, null);
        plugin.saveConfig();

        // Clear shown titles
        shownCaptureTitles.remove(name);
        hills.remove(name);
    }

    public Hill getHill(String name) {
        return hills.get(name);
    }

    public Collection<Hill> getHills() {
        return hills.values();
    }

    public boolean hillExists(String name) {
        return hills.containsKey(name);
    }
}