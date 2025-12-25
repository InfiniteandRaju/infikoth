package com.groupez.koth.manager;

import com.groupez.koth.KotH;
import com.groupez.koth.hill.Hill;
import com.groupez.koth.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BossBarManager {

    private final KotH plugin;
    private final Map<String, BossBar> hillBars = new HashMap<>();

    public BossBarManager(KotH plugin) {
        this.plugin = plugin;
    }

    public void createHillBossBar(Hill hill) {
        if (hillBars.containsKey(hill.getName())) {
            return;
        }

        String title = plugin.getConfigManager().getMessage("bossbar-waiting", "%hill%", hill.getName());
        BossBar bar = Bukkit.createBossBar(ColorUtils.color(title), BarColor.WHITE, BarStyle.SOLID);
        bar.setVisible(false);
        hillBars.put(hill.getName(), bar);
    }

    public void removeHillBossBar(String hillName) {
        BossBar bar = hillBars.remove(hillName);
        if (bar != null) {
            bar.removeAll();
        }
    }

    public void updateAllBossBars() {
        for (Hill hill : plugin.getHillManager().getHills()) {
            BossBar bar = hillBars.get(hill.getName());
            if (bar == null) {
                createHillBossBar(hill);
                bar = hillBars.get(hill.getName());
            }

            // Only show if hill is active or configured to show always (logic depends on
            // requirements, assuming active)
            if (!hill.isActive()) {
                bar.setVisible(false);
                bar.removeAll(); // Clear players
                continue;
            }

            // Update title and color based on state
            String title;
            BarColor color;
            double progress = 1.0;

            if (hill.getCurrentCapturer() != null) {
                int timeLeft = hill.getTimeLeft();
                int totalTime = hill.getCaptureTime();

                title = plugin.getConfigManager().getMessage("bossbar-capturing",
                        "%hill%", hill.getName(),
                        "%player%", hill.getCurrentCapturer().getName(),
                        "%time%", String.valueOf(timeLeft));

                color = BarColor.GREEN;
                progress = (double) timeLeft / totalTime;
            } else {
                title = plugin.getConfigManager().getMessage("bossbar-waiting", "%hill%", hill.getName());
                color = BarColor.RED;
                progress = 1.0;
            }

            bar.setTitle(ColorUtils.color(title));
            bar.setColor(color);
            bar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
            bar.setVisible(true);

            // Update players who should see it (in world or nearby?)
            // Usually KOTH bossbars are global or world-wide.
            // HillManager broadcasts to world, so we'll show to world players.
            for (Player player : hill.getCenter().getWorld().getPlayers()) {
                if (!bar.getPlayers().contains(player)) {
                    bar.addPlayer(player);
                }
            }

            // Remove players not in world
            for (Player player : bar.getPlayers()) {
                if (!player.getWorld().equals(hill.getCenter().getWorld())) {
                    bar.removePlayer(player);
                }
            }
        }
    }

    public void cleanup() {
        for (BossBar bar : hillBars.values()) {
            bar.removeAll();
        }
        hillBars.clear();
    }
}
