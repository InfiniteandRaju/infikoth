package com.groupez.koth.commands;

import com.groupez.koth.KotH;
import com.groupez.koth.hill.Hill;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KothCommand implements CommandExecutor, TabCompleter {

    private final KotH plugin;
    private final List<String> subCommands = Arrays.asList("start", "stop", "create", "delete", "list", "reload", "info", "help");

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
            case "help":
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleStart(CommandSender sender, String[] args) {
        if (!sender.hasPermission("koth.start")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /koth start <hillname>");
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
            sender.sendMessage(ChatColor.RED + "Usage: /koth stop <hillname>");
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

        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /koth create <name> <radius> <capturetime>");
            return true;
        }

        Player player = (Player) sender;
        try {
            String name = args[1];
            int radius = Integer.parseInt(args[2]);
            int captureTime = Integer.parseInt(args[3]);

            if (radius < 1 || radius > 100) {
                sender.sendMessage(ChatColor.RED + "Radius must be between 1 and 100!");
                return true;
            }

            if (captureTime < 5 || captureTime > 600) {
                sender.sendMessage(ChatColor.RED + "Capture time must be between 5 and 600 seconds!");
                return true;
            }

            if (plugin.getHillManager().hillExists(name)) {
                sender.sendMessage(ChatColor.RED + "Hill '" + name + "' already exists!");
                return true;
            }

            plugin.getHillManager().addHill(name, player.getLocation(), radius, captureTime);
            sender.sendMessage(ChatColor.GREEN + "Created hill: " + name +
                    ChatColor.GRAY + " (Radius: " + radius + ", Time: " + captureTime + "s)");
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
            sender.sendMessage(ChatColor.RED + "Usage: /koth delete <hillname>");
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
            String status = hill.isActive() ?
                    ChatColor.GREEN + "Active" :
                    ChatColor.RED + "Inactive";

            sender.sendMessage(ChatColor.GRAY + "• " +
                    ChatColor.YELLOW + hill.getName() +
                    ChatColor.GRAY + " - " +
                    status +
                    ChatColor.DARK_GRAY + " (Radius: " + hill.getRadius() + ", Time: " + hill.getCaptureTime() + "s)");
        }

        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("koth.info")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /koth info <hillname>");
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
                sender.sendMessage(ChatColor.GRAY + "Capturer: " + ChatColor.AQUA + hill.getCurrentCapturer().getName());
            }
        }

        Location loc = hill.getCenter();
        sender.sendMessage(ChatColor.GRAY + "Location: " + ChatColor.WHITE +
                String.format("World: %s, X: %.1f, Y: %.1f, Z: %.1f",
                        loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ()));
        sender.sendMessage(ChatColor.GRAY + "Radius: " + ChatColor.WHITE + hill.getRadius() + " blocks");
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

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "=== KoTH Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/koth start <name> " + ChatColor.GRAY + "- Start a KoTH");
        sender.sendMessage(ChatColor.YELLOW + "/koth stop <name> " + ChatColor.GRAY + "- Stop a KoTH");
        sender.sendMessage(ChatColor.YELLOW + "/koth create <name> <radius> <time> " + ChatColor.GRAY + "- Create a new hill");
        sender.sendMessage(ChatColor.YELLOW + "/koth delete <name> " + ChatColor.GRAY + "- Delete a hill");
        sender.sendMessage(ChatColor.YELLOW + "/koth list " + ChatColor.GRAY + "- List all hills");
        sender.sendMessage(ChatColor.YELLOW + "/koth info <name> " + ChatColor.GRAY + "- Get hill info");
        sender.sendMessage(ChatColor.YELLOW + "/koth reload " + ChatColor.GRAY + "- Reload configuration");
        sender.sendMessage(ChatColor.YELLOW + "/koth help " + ChatColor.GRAY + "- Show this help");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (Arrays.asList("start", "stop", "delete", "remove", "info").contains(subCommand)) {
                for (Hill hill : plugin.getHillManager().getHills()) {
                    String name = hill.getName();
                    if (name.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(name);
                    }
                }
            }
        }

        return completions;
    }
}