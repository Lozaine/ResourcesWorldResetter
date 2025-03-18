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
    private int resetWarningTime;

    @Override
    public void onEnable() {
        // Save default config if not present
        saveDefaultConfig();

        // Check for Multiverse-Core
        core = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        if (core == null) {
            getLogger().severe("Multiverse-Core not found! Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Retrieve configuration values
        loadConfig();

        // Check if world exists and create it if needed
        ensureResourceWorldExists();

        // Schedule daily resets
        scheduleDailyReset();

        getLogger().info("ResourceWorldResetter v" + getDescription().getVersion() + " enabled successfully!");
    }

    private void loadConfig() {
        reloadConfig();

        worldName = getConfig().getString("worldName", "Resources");
        resetInterval = getConfig().getLong("resetInterval", 86400); // Default to 24 hours
        restartTime = getConfig().getInt("restartTime", 3); // Default restart time (3AM)
        resetWarningTime = getConfig().getInt("resetWarningTime", 5); // Default warning time (5 minutes)

        // Always ensure worldName has a value
        if (worldName == null || worldName.isEmpty()) {
            worldName = "Resources";
            getConfig().set("worldName", worldName);
            saveConfig();
            getLogger().info("No world name specified, defaulting to 'Resources'");
        }
    }

    private void ensureResourceWorldExists() {
        World world = Bukkit.getWorld(worldName);
        MVWorldManager worldManager = core.getMVWorldManager();

        if (world == null && !worldManager.isMVWorld(worldName)) {
            getLogger().info("Resource world '" + worldName + "' does not exist! Creating...");

            // Create world with default settings
            boolean createSuccess = worldManager.addWorld(
                    worldName,                  // World name
                    World.Environment.NORMAL,   // Environment
                    null,                       // Seed
                    WorldType.NORMAL,           // World type
                    true,                       // Generate structures
                    "DEFAULT"                   // Generator
            );

            if (!createSuccess || Bukkit.getWorld(worldName) == null) {
                getLogger().severe("Failed to create world '" + worldName + "'!");
            } else {
                getLogger().info("Resource world '" + worldName + "' created successfully!");

                // Set some basic world properties via Multiverse
                worldManager.getMVWorld(worldName).setGameMode(org.bukkit.GameMode.SURVIVAL);
                worldManager.getMVWorld(worldName).setDifficulty(org.bukkit.Difficulty.NORMAL);
                worldManager.getMVWorld(worldName).setAllowAnimalSpawn(true);
                worldManager.getMVWorld(worldName).setAllowMonsterSpawn(true);

                getLogger().info("Basic world properties set for '" + worldName + "'");
            }
        } else {
            getLogger().info("Resource world '" + worldName + "' already exists and is loaded.");
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
        long warningDelay = delay - (resetWarningTime * 60); // Convert minutes to seconds

        // Schedule the announcement
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("§e[ResourceWorldResetter] Warning: The resource world will reset in " +
                        resetWarningTime + " minutes!");
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

        // Log when the next reset is scheduled
        getLogger().info("Next resource world reset scheduled for: " + nextReset);
    }

    private void resetResourceWorld() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            getLogger().warning("Resource world not found! Attempting to create...");
            ensureResourceWorldExists();
            return;
        }

        getLogger().info("Beginning reset of resource world: " + worldName);
        Bukkit.broadcastMessage("§e[ResourceWorldResetter] Beginning reset of resource world...");

        // Teleport all players out of the world
        for (Player player : world.getPlayers()) {
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            player.sendMessage("§e[ResourceWorldResetter] You have been teleported out of the resource world due to a reset.");
        }

        MVWorldManager worldManager = core.getMVWorldManager();

        // Unload the world
        boolean unloadSuccess = worldManager.unloadWorld(worldName);
        if (!unloadSuccess) {
            getLogger().severe("Failed to unload resource world!");
            Bukkit.broadcastMessage("§c[ResourceWorldResetter] Failed to reset the resource world. Please contact an administrator.");
            return;
        }

        // Delete the world
        boolean deleteSuccess = worldManager.deleteWorld(worldName, true, true);
        if (!deleteSuccess) {
            getLogger().severe("Failed to delete resource world!");
            Bukkit.broadcastMessage("§c[ResourceWorldResetter] Failed to reset the resource world. Please contact an administrator.");
            return;
        }

        // Recreate the world
        boolean createSuccess = worldManager.addWorld(
                worldName,                  // World name
                World.Environment.NORMAL,   // Environment
                null,                       // Seed
                WorldType.NORMAL,           // World type
                true,                       // Generate structures
                "DEFAULT"                   // Generator
        );

        if (!createSuccess) {
            getLogger().severe("Failed to create new resource world!");
            Bukkit.broadcastMessage("§c[ResourceWorldResetter] Failed to reset the resource world. Please contact an administrator.");
            return;
        }

        // Set world properties
        worldManager.getMVWorld(worldName).setGameMode(org.bukkit.GameMode.SURVIVAL);
        worldManager.getMVWorld(worldName).setDifficulty(org.bukkit.Difficulty.NORMAL);
        worldManager.getMVWorld(worldName).setAllowAnimalSpawn(true);
        worldManager.getMVWorld(worldName).setAllowMonsterSpawn(true);

        Bukkit.broadcastMessage("§a[ResourceWorldResetter] The resource world has been successfully reset!");
        getLogger().info("Resource world reset completed successfully.");
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
                    sender.sendMessage("§a[ResourceWorldResetter] Resource world set to: " + worldName);
                    ensureResourceWorldExists();
                } else {
                    sender.sendMessage("§c[ResourceWorldResetter] Usage: /setworld <worldname>");
                }
                return true;
            } else if (command.getName().equalsIgnoreCase("setresetinterval")) {
                if (args.length > 0) {
                    try {
                        int hours = Integer.parseInt(args[0]);
                        resetInterval = hours * 60 * 60; // Convert hours to seconds
                        getConfig().set("resetInterval", resetInterval);
                        saveConfig();
                        sender.sendMessage("§a[ResourceWorldResetter] Reset interval set to " + hours + " hours.");
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§c[ResourceWorldResetter] Please provide a valid number.");
                    }
                } else {
                    sender.sendMessage("§c[ResourceWorldResetter] Usage: /setresetinterval <hours>");
                }
                return true;
            } else if (command.getName().equalsIgnoreCase("setrestarttime")) {
                if (args.length > 0) {
                    try {
                        int hour = Integer.parseInt(args[0]);
                        if (hour < 0 || hour > 23) {
                            sender.sendMessage("§c[ResourceWorldResetter] Please provide a valid hour (0-23).");
                            return true;
                        }
                        restartTime = hour;
                        getConfig().set("restartTime", restartTime);
                        saveConfig();
                        sender.sendMessage("§a[ResourceWorldResetter] Server restart time set to: " + restartTime + ":00");
                        scheduleDailyReset(); // Reschedule if the time changes
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§c[ResourceWorldResetter] Please provide a valid hour (0-23).");
                    }
                } else {
                    sender.sendMessage("§c[ResourceWorldResetter] Usage: /setrestarttime <hour>");
                }
                return true;
            } else if (command.getName().equalsIgnoreCase("resetworld")) {
                sender.sendMessage("§a[ResourceWorldResetter] Forcing resource world reset...");
                resetResourceWorld();
                return true;
            } else if (command.getName().equalsIgnoreCase("reloadworldresetter")) {
                reloadPlugin(sender);
                return true;
            }
        } else {
            sender.sendMessage("§c[ResourceWorldResetter] You do not have permission to use this command.");
        }
        return false;
    }

    private void reloadPlugin(CommandSender sender) {
        this.reloadConfig();
        loadConfig();
        ensureResourceWorldExists();
        scheduleDailyReset();
        sender.sendMessage("§a[ResourceWorldResetter] Plugin has been reloaded successfully.");
    }
}