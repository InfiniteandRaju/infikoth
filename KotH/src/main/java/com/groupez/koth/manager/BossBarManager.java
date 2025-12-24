package com.groupez.koth.manager;

import com.groupez.koth.KotH;
import com.groupez.koth.hill.Hill;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BossBarManager {

    private final KotH plugin;
    private final Map<String, BossBar> bossBars = new HashMap<>();

    public BossBarManager(KotH plugin) {
        this.plugin = plugin;
    }

    public void createHillBossBar(Hill hill) {
        String hillName = hill.getName();

        if (bossBars.containsKey(hillName)) {
            removeHillBossBar(hillName);
        }

        BossBar bossBar = Bukkit.createBossBar(
                getBossBarTitle(hill, null),
                BarColor.PURPLE,
                BarStyle.SOLID
        );

        bossBar.setVisible(false);
        bossBars.put(hillName, bossBar);
    }

    public void updateHillBossBar(Hill hill) {
        if (!plugin.getConfigManager().isBossBarEnabled()) return;

        String hillName = hill.getName();
        BossBar bossBar = bossBars.get(hillName);

        if (bossBar == null) {
            createHillBossBar(hill);
            bossBar = bossBars.get(hillName);
        }

        if (hill.isActive()) {
            Player capturer = hill.getCurrentCapturer();
            int timeLeft = hill.getTimeLeft();

            if (capturer != null && timeLeft > 0) {
                // Update boss bar for active capture
                double progress = (double) timeLeft / hill.getCaptureTime();
                bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
                bossBar.setTitle(getBossBarTitle(hill, capturer));
                bossBar.setColor(getColorByProgress(progress));
                bossBar.setVisible(true);

                // Add all online players in the world to the boss bar
                for (Player player : hill.getCenter().getWorld().getPlayers()) {
                    if (!bossBar.getPlayers().contains(player)) {
                        bossBar.addPlayer(player);
                    }
                }
            } else {
                // Hill is active but no one is capturing
                bossBar.setTitle(getBossBarTitle(hill, null));
                bossBar.setColor(BarColor.PURPLE);
                bossBar.setProgress(1.0);
                bossBar.setVisible(true);

                // Add all online players in the world
                for (Player player : hill.getCenter().getWorld().getPlayers()) {
                    if (!bossBar.getPlayers().contains(player)) {
                        bossBar.addPlayer(player);
                    }
                }
            }
        } else {
            // Hill is not active, hide boss bar
            bossBar.setVisible(false);
            bossBar.removeAll();
        }
    }

    private String getBossBarTitle(Hill hill, Player capturer) {
        String hillName = hill.getName();
        int timeLeft = hill.getTimeLeft();

        if (capturer != null) {
            return plugin.getConfigManager().getMessage("bossbar-capturing",
                    "%hill%", hillName,
                    "%player%", capturer.getName(),
                    "%time%", String.valueOf(timeLeft));
        } else if (hill.isActive()) {
            return plugin.getConfigManager().getMessage("bossbar-waiting",
                    "%hill%", hillName);
        } else {
            return plugin.getConfigManager().getMessage("bossbar-inactive",
                    "%hill%", hillName);
        }
    }

    private BarColor getColorByProgress(double progress) {
        if (progress > 0.66) return BarColor.GREEN;
        if (progress > 0.33) return BarColor.YELLOW;
        return BarColor.RED;
    }

    public void removeHillBossBar(String hillName) {
        BossBar bossBar = bossBars.get(hillName);
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar.setVisible(false);
            bossBars.remove(hillName);
        }
    }

    public void updateAllBossBars() {
        for (Hill hill : plugin.getHillManager().getHills()) {
            updateHillBossBar(hill);
        }
    }

    public void cleanup() {
        for (BossBar bossBar : bossBars.values()) {
            bossBar.removeAll();
            bossBar.setVisible(false);
        }
        bossBars.clear();
    }
}