package com.lozaine.rwr.commands;

import com.lozaine.rwr.ResourceWorldResetter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles all admin commands for the ResourceWorldResetter plugin
 */
public class AdminCommands implements CommandExecutor {
    private final ResourceWorldResetter plugin;

    public AdminCommands(ResourceWorldResetter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) || sender.hasPermission("resourceworldresetter.admin")) {
            if (command.getName().equalsIgnoreCase("setworld")) {
                return handleSetWorld(sender, args);
            } else if (command.getName().equalsIgnoreCase("setresetinterval")) {
                return handleSetResetInterval(sender, args);
            } else if (command.getName().equalsIgnoreCase("setrestarttime")) {
                return handleSetRestartTime(sender, args);
            } else if (command.getName().equalsIgnoreCase("resetworld")) {
                return handleResetWorld(sender);
            } else if (command.getName().equalsIgnoreCase("reloadworldresetter")) {
                return handleReloadPlugin(sender);
            }
        } else {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }
        return false;
    }

    private boolean handleSetWorld(CommandSender sender, String[] args) {
        if (args.length > 0) {
            plugin.setWorldName(args[0]);
            sender.sendMessage("§aResource world set to: " + args[0]);
            plugin.loadWorld();
            return true;
        } else {
            sender.sendMessage("§cUsage: /setworld <worldname>");
            return true;
        }
    }

    private boolean handleSetResetInterval(CommandSender sender, String[] args) {
        if (args.length > 0) {
            try {
                long intervalHours = Long.parseLong(args[0]);
                long intervalSeconds = intervalHours * 60 * 60; // Convert hours to seconds
                plugin.setResetInterval(intervalSeconds);
                sender.sendMessage("§aReset interval set to " + args[0] + " hours.");
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage("§cPlease provide a valid number.");
                return true;
            }
        } else {
            sender.sendMessage("§cUsage: /setresetinterval <hours>");
            return true;
        }
    }

    private boolean handleSetRestartTime(CommandSender sender, String[] args) {
        if (args.length > 0) {
            try {
                int restartTime = Integer.parseInt(args[0]);
                if (restartTime < 0 || restartTime > 23) {
                    sender.sendMessage("§cPlease provide a valid hour (0-23).");
                    return true;
                }
                plugin.setRestartTime(restartTime);
                sender.sendMessage("§aServer restart time set to: " + restartTime + ":00");
                plugin.scheduleDailyReset(); // Reschedule if the time changes
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage("§cPlease provide a valid hour (0-23).");
                return true;
            }
        } else {
            sender.sendMessage("§cUsage: /setrestarttime <hour>");
            return true;
        }
    }

    private boolean handleResetWorld(CommandSender sender) {
        plugin.resetResourceWorld();
        sender.sendMessage("§aResource world has been reset!");
        return true;
    }

    private boolean handleReloadPlugin(CommandSender sender) {
        if (sender.hasPermission("resourceworldresetter.reload")) {
            plugin.reloadPlugin();
            sender.sendMessage("§aResourceWorldResetter plugin has been reloaded successfully.");
            return true;
        } else {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }
    }
}