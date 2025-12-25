package com.groupez.koth.commands;

import com.groupez.koth.KotH;
import com.groupez.koth.hill.Hill;
import com.groupez.koth.utils.ColorUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class KothCommand implements CommandExecutor, TabCompleter {

    private final KotH plugin;
    private final List<String> subCommands = Arrays.asList("start", "stop", "create", "delete", "list", "reload",
            "info", "help", "rewards", "editrewards", "wand", "pos1", "pos2");

    public KothCommand(KotH plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                return handleStart(sender, args);
            case "stop":
                return handleStop(sender, args);
            case "create":
                return handleCreate(sender, args);
            case "delete":
            case "remove":
                return handleDelete(sender, args);
            case "list":
                return handleList(sender);
            case "info":
                return handleInfo(sender, args);
            case "reload":
                return handleReload(sender);
            case "rewards":
            case "editrewards":
                return handleEditRewards(sender, args);
            case "wand":
                return handleWand(sender);
            case "pos1":
                return handlePos1(sender);
            case "pos2":
                return handlePos2(sender);
            case "help":
            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtils.color("&8&m----------------------------------------"));
        sender.sendMessage(ColorUtils.color("&6&lInfikoth Help"));
        sender.sendMessage(ColorUtils.color("&8&m----------------------------------------"));
        sender.sendMessage(ColorUtils.color("&e/infikoth start <name> &7- Start a KOTH"));
        sender.sendMessage(ColorUtils.color("&e/infikoth stop <name> &7- Stop a KOTH"));
        sender.sendMessage(ColorUtils.color("&e/infikoth create <name> [radius] [time] &7- Create a KOTH"));
        sender.sendMessage(ColorUtils.color("&e/infikoth delete <name> &7- Delete a KOTH"));
        sender.sendMessage(ColorUtils.color("&e/infikoth list &7- List all KOTHs"));
        sender.sendMessage(ColorUtils.color("&e/infikoth info <name> &7- View KOTH info"));
        sender.sendMessage(ColorUtils.color("&e/infikoth rewards <name> &7- Edit rewards"));
        sender.sendMessage(ColorUtils.color("&e/infikoth wand &7- Get selection wand"));
        sender.sendMessage(ColorUtils.color("&e/infikoth reload &7- Reload config"));
        sender.sendMessage(ColorUtils.color("&8&m----------------------------------------"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            for (String sub : subCommands) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
            return completions;
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (Arrays.asList("start", "stop", "delete", "remove", "info", "rewards", "editrewards").contains(sub)) {
                return plugin.getHillManager().getHills().stream()
                        .map(Hill::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

    private boolean handleStart(CommandSender sender, String[] args) {
        if (!sender.hasPermission("koth.start")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /infikoth start <hillname>");
            return true;
        }

        String hillName = args[1];
        if (!plugin.getHillManager().hillExists(hillName)) {
            sender.sendMessage(ChatColor.RED + "Hill '" + hillName + "' does not exist!");
            return true;
        }

        plugin.getHillManager().startHill(hillName);
        sender.sendMessage(ChatColor.GREEN + "Started KoTH: " + hillName);
        return true;
    }

    private boolean handleStop(CommandSender sender, String[] args) {
        if (!sender.hasPermission("koth.stop")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /infikoth stop <hillname>");
            return true;
        }

        String hillName = args[1];
        if (!plugin.getHillManager().hillExists(hillName)) {
            sender.sendMessage(ChatColor.RED + "Hill '" + hillName + "' does not exist!");
            return true;
        }

        plugin.getHillManager().stopHill(hillName);
        sender.sendMessage(ChatColor.RED + "Stopped KoTH: " + hillName);
        return true;
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can create hills!");
            return true;
        }

        if (!sender.hasPermission("koth.create")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /infikoth create <name> [radius] [time]");
            return true;
        }

        Player player = (Player) sender;
        String name = args[1];

        if (plugin.getHillManager().hillExists(name)) {
            sender.sendMessage(ChatColor.RED + "Hill '" + name + "' already exists!");
            return true;
        }

        // Check for cuboid selection first
        if (plugin.getSelectionManager().hasSelection(player)) {
            int captureTime = 60; // Default
            if (args.length >= 3) {
                try {
                    captureTime = Integer.parseInt(args[2]);
                } catch (NumberFormatException ignored) {
                }
            }

            Location pos1 = plugin.getSelectionManager().getPos1(player);
            Location pos2 = plugin.getSelectionManager().getPos2(player);

            plugin.getHillManager().addCuboidHill(name, pos1, pos2, captureTime, true);
            sender.sendMessage(
                    ColorUtils.color("&aCreated Cuboid Hill: &e" + name + " &7(Time: " + captureTime + "s)"));
            return true;
        }

        // Fallback to radius
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /infikoth create <name> <radius> [time]");
            sender.sendMessage(ChatColor.GRAY + "Or select a region with /infikoth wand first!");
            return true;
        }

        try {
            int radius = Integer.parseInt(args[2]);
            int captureTime = 60;
            if (args.length >= 4) {
                captureTime = Integer.parseInt(args[3]);
            }

            if (radius < 1 || radius > 100) {
                sender.sendMessage(ChatColor.RED + "Radius must be between 1 and 100!");
                return true;
            }

            plugin.getHillManager().addRadiusHill(name, player.getLocation(), radius, captureTime, true);
            sender.sendMessage(ColorUtils.color(
                    "&aCreated Radius Hill: &e" + name + " &7(Radius: " + radius + ", Time: " + captureTime + "s)"));

        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Radius and capture time must be numbers!");
        }

        return true;
    }

    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("koth.delete")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /infikoth delete <hillname>");
            return true;
        }

        String hillName = args[1];
        if (!plugin.getHillManager().hillExists(hillName)) {
            sender.sendMessage(ChatColor.RED + "Hill '" + hillName + "' does not exist!");
            return true;
        }

        plugin.getHillManager().removeHill(hillName);
        sender.sendMessage(ChatColor.GREEN + "Deleted hill: " + hillName);
        return true;
    }

    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("koth.list")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "=== KoTH Hills ===");

        for (Hill hill : plugin.getHillManager().getHills()) {
            String status = hill.isActive() ? ChatColor.GREEN + "Active" : ChatColor.RED + "Inactive";

            String type = hill.isCuboid() ? "Cuboid" : "Radius: " + hill.getRadius();

            sender.sendMessage(ChatColor.GRAY + "• " +
                    ChatColor.YELLOW + hill.getName() +
                    ChatColor.GRAY + " - " +
                    status +
                    ChatColor.DARK_GRAY + " (" + type + ", Time: " + hill.getCaptureTime() + "s)");
        }

        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("koth.info")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /infikoth info <hillname>");
            return true;
        }

        String hillName = args[1];
        Hill hill = plugin.getHillManager().getHill(hillName);

        if (hill == null) {
            sender.sendMessage(ChatColor.RED + "Hill '" + hillName + "' does not exist!");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "=== " + hillName + " Info ===");
        sender.sendMessage(ChatColor.GRAY + "Status: " +
                (hill.isActive() ? ChatColor.GREEN + "Active" : ChatColor.RED + "Inactive"));

        if (hill.isActive()) {
            sender.sendMessage(ChatColor.GRAY + "Time Left: " + ChatColor.YELLOW + hill.getTimeLeft() + "s");

            if (hill.getCurrentCapturer() != null) {
                sender.sendMessage(
                        ChatColor.GRAY + "Capturer: " + ChatColor.AQUA + hill.getCurrentCapturer().getName());
            }
        }

        Location loc = hill.getCenter();
        sender.sendMessage(ChatColor.GRAY + "Location: " + ChatColor.WHITE +
                String.format("World: %s, X: %.1f, Y: %.1f, Z: %.1f",
                        loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ()));

        if (hill.isCuboid()) {
            sender.sendMessage(ChatColor.GRAY + "Type: " + ChatColor.WHITE + "Cuboid");
        } else {
            sender.sendMessage(ChatColor.GRAY + "Type: " + ChatColor.WHITE + "Radius (" + hill.getRadius() + ")");
        }

        sender.sendMessage(ChatColor.GRAY + "Capture Time: " + ChatColor.WHITE + hill.getCaptureTime() + "s");

        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("koth.reload")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        plugin.getConfigManager().reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "Configuration reloaded!");
        return true;
    }

    private boolean handleEditRewards(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        if (!sender.hasPermission("koth.rewards")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /infikoth editrewards <hillname>");
            return true;
        }

        String hillName = args[1];
        if (!plugin.getHillManager().hillExists(hillName)) {
            sender.sendMessage(ChatColor.RED + "Hill '" + hillName + "' does not exist!");
            return true;
        }

        plugin.getRewardManager().openEditor((Player) sender, hillName);
        return true;
    }

    private boolean handleWand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        if (!sender.hasPermission("koth.create")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        Player player = (Player) sender;
        player.getInventory().addItem(plugin.getSelectionManager().getWand());
        sender.sendMessage(ColorUtils.color("&aGave you a Selection Wand!"));
        return true;
    }

    private boolean handlePos1(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }
        if (!sender.hasPermission("koth.create")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        Player player = (Player) sender;
        plugin.getSelectionManager().setPos1(player, player.getLocation().getBlock().getLocation());
        return true;
    }

    private boolean handlePos2(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }
        if (!sender.hasPermission("koth.create")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        Player player = (Player) sender;
        plugin.getSelectionManager().setPos2(player, player.getLocation().getBlock().getLocation());
        return true;
    }
}
