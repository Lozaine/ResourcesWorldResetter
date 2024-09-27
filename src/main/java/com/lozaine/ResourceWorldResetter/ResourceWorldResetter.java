package com.lozaine.ResourceWorldResetter;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class ResourceWorldResetter extends JavaPlugin {
    private String worldName;
    private MultiverseCore core;
    private long resetInterval;
    private int restartTime;

    @Override
    public void onEnable() {
        // Save default config if not present
        this.saveDefaultConfig();

        core = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        if (core == null) {
            getLogger().severe("Multiverse-Core not found! Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Retrieve configuration values
        loadConfig();

        // Check if world exists and try to load
        loadWorld();
        scheduleDailyReset(); // Schedule daily resets
    }

    private void loadConfig() {
        worldName = getConfig().getString("worldName");
        resetInterval = getConfig().getLong("resetInterval", 86400); // Default to 24 hours
        restartTime = getConfig().getInt("restartTime", 3); // Default restart time (3AM)

        if (worldName == null || worldName.isEmpty()) {
            getLogger().severe("No world name specified in the config file! Please set 'worldName' in the config.yml.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void loadWorld() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            getLogger().info("World '" + worldName + "' does not exist! Attempting to create...");
            MVWorldManager worldManager = core.getMVWorldManager();
            boolean createSuccess = worldManager.addWorld(worldName, World.Environment.NORMAL, null, WorldType.NORMAL, true, null);

            if (!createSuccess || Bukkit.getWorld(worldName) == null) {
                getLogger().severe("Failed to create world '" + worldName + "'. Disabling plugin.");
                Bukkit.getPluginManager().disablePlugin(this);
            } else {
                getLogger().info("World '" + worldName + "' created successfully.");
            }
        } else {
            getLogger().info("World '" + worldName + "' loaded successfully.");
        }
    }

    private void scheduleDailyReset() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime resetTime = LocalTime.of(restartTime, 0);

        LocalDateTime nextReset = now.with(resetTime);
        if (now.isAfter(nextReset)) {
            nextReset = nextReset.plusDays(1);
        }

        long delay = java.time.Duration.between(now, nextReset).toSeconds();
        long warningDelay = delay - 300; // 5 minutes before reset

        // Schedule the announcement
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("§eWarning: The resource world will reset in 5 minutes!");
            }
        }.runTaskLater(this, warningDelay * 20L); // Convert seconds to ticks

        // Schedule the actual reset
        new BukkitRunnable() {
            @Override
            public void run() {
                resetResourceWorld();
                scheduleDailyReset(); // Reschedule for the next day
            }
        }.runTaskLater(this, delay * 20L); // Convert seconds to ticks
    }

    private void resetResourceWorld() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            getLogger().warning("Resource world not found!");
            return;
        }

        for (Player player : world.getPlayers()) {
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            player.sendMessage("§eYou have been teleported out of the resource world due to a reset.");
        }

        MVWorldManager worldManager = core.getMVWorldManager();
        boolean unloadSuccess = worldManager.unloadWorld(worldName);
        if (!unloadSuccess) {
            getLogger().severe("Failed to unload resource world!");
            return;
        }

        boolean deleteSuccess = worldManager.deleteWorld(worldName, true, true);
        if (!deleteSuccess) {
            getLogger().severe("Failed to delete resource world!");
            return;
        }

        boolean createSuccess = worldManager.addWorld(worldName, World.Environment.NORMAL, null, WorldType.NORMAL, true, null);
        if (!createSuccess) {
            getLogger().severe("Failed to create new resource world!");
            return;
        }

        Bukkit.broadcastMessage("§aThe resource world has been successfully reset!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ResourceWorldResetter has been disabled.");
    }

    // Handle commands for setting world, reset interval, etc.
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) || sender.hasPermission("resourceworldresetter.admin")) {
            if (command.getName().equalsIgnoreCase("setworld")) {
                if (args.length > 0) {
                    worldName = args[0];
                    getConfig().set("worldName", worldName);
                    saveConfig();
                    sender.sendMessage("§aResource world set to: " + worldName);
                    loadWorld();
                } else {
                    sender.sendMessage("§cUsage: /setworld <worldname>");
                }
                return true;
            } else if (command.getName().equalsIgnoreCase("setresetinterval")) {
                if (args.length > 0) {
                    try {
                        resetInterval = Long.parseLong(args[0]) * 60 * 60; // Convert hours to seconds
                        getConfig().set("resetInterval", resetInterval);
                        saveConfig();
                        sender.sendMessage("§aReset interval set to " + args[0] + " hours.");
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cPlease provide a valid number.");
                    }
                } else {
                    sender.sendMessage("§cUsage: /setresetinterval <hours>");
                }
                return true;
            } else if (command.getName().equalsIgnoreCase("setrestarttime")) {
                if (args.length > 0) {
                    try {
                        restartTime = Integer.parseInt(args[0]);
                        getConfig().set("restartTime", restartTime);
                        saveConfig();
                        sender.sendMessage("§aServer restart time set to: " + restartTime + ":00");
                        scheduleDailyReset(); // Reschedule if the time changes
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cPlease provide a valid hour (0-23).");
                    }
                } else {
                    sender.sendMessage("§cUsage: /setrestarttime <hour>");
                }
                return true;
            } else if (command.getName().equalsIgnoreCase("resetworld")) {
                resetResourceWorld();
                sender.sendMessage("§aResource world has been reset!");
                return true;
            } else if (command.getName().equalsIgnoreCase("reloadworldresetter")) {
                if (sender.hasPermission("resourceworldresetter.reload")) {
                    reloadPlugin(sender);
                } else {
                    sender.sendMessage("§cYou do not have permission to use this command.");
                }
                return true;
            }
        } else {
            sender.sendMessage("§cYou do not have permission to use this command.");
        }
        return false;
    }

    private void reloadPlugin(CommandSender sender) {
        this.reloadConfig();
        loadConfig(); // Reload configuration values

        // Check if world exists and load it
        loadWorld();

        // Reschedule the daily reset task
        scheduleDailyReset();

        sender.sendMessage("§aResourceWorldResetter plugin has been reloaded successfully.");
    }
}
