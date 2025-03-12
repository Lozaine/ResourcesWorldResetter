package com.lozaine.rwr;

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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ResourceWorldResetter extends JavaPlugin {
    private String worldName;
    private MultiverseCore core;
    private long resetInterval;
    private int restartTime;
    private int resetWarningTime;

    @Override
    public void onEnable() {
        try {
            // Save default config if not present
            saveDefaultConfig();

            // Get MultiverseCore instance
            core = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
            if (core == null) {
                getLogger().severe("Multiverse-Core not found! Disabling plugin.");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            // Retrieve configuration values
            loadConfig();

            // Only proceed if worldName is valid
            if (worldName != null && !worldName.isEmpty()) {
                // Check if world exists and try to load
                loadWorld();
                scheduleDailyReset(); // Schedule daily resets
                getLogger().info("ResourceWorldResetter has been enabled successfully!");
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error enabling ResourceWorldResetter plugin", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void loadConfig() {
        try {
            worldName = getConfig().getString("worldName");
            resetInterval = getConfig().getLong("resetInterval", 86400); // Default to 24 hours
            restartTime = getConfig().getInt("restartTime", 3); // Default restart time (3AM)
            resetWarningTime = getConfig().getInt("resetWarningTime", 5); // Default warning time (5 minutes)

            if (worldName == null || worldName.isEmpty()) {
                getLogger().severe("No world name specified in the config file! Please set 'worldName' in the config.yml.");
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error loading configuration", e);
        }
    }

    public void loadWorld() {
        try {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                getLogger().info("World '" + worldName + "' does not exist! Attempting to create...");
                MVWorldManager worldManager = core.getMVWorldManager();

                // Create world with appropriate parameters for 1.21.4
                boolean createSuccess = worldManager.addWorld(
                        worldName,
                        World.Environment.NORMAL,
                        null,
                        WorldType.NORMAL,
                        true,
                        null
                );

                if (!createSuccess || Bukkit.getWorld(worldName) == null) {
                    getLogger().severe("Failed to create world '" + worldName + "'.");
                } else {
                    getLogger().info("World '" + worldName + "' created successfully.");
                }
            } else {
                getLogger().info("World '" + worldName + "' loaded successfully.");
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error loading world", e);
        }
    }

    public void scheduleDailyReset() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalTime resetTime = LocalTime.of(restartTime, 0);

            LocalDateTime nextReset = now.with(resetTime);
            if (now.isAfter(nextReset)) {
                nextReset = nextReset.plusDays(1);
            }

            long delay = Duration.between(now, nextReset).toSeconds();
            long warningDelay = delay - (resetWarningTime * 60); // Convert warning time to seconds

            // Schedule the announcement
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        Bukkit.broadcastMessage("§eWarning: The resource world will reset in " + resetWarningTime + " minutes!");
                    } catch (Exception e) {
                        getLogger().log(Level.SEVERE, "Error broadcasting warning message", e);
                    }
                }
            }.runTaskLater(this, Math.max(0, warningDelay) * 20L); // Convert seconds to ticks, ensure non-negative

            // Schedule the actual reset
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        resetResourceWorld();
                        scheduleDailyReset(); // Reschedule for the next day
                    } catch (Exception e) {
                        getLogger().log(Level.SEVERE, "Error during scheduled reset", e);
                    }
                }
            }.runTaskLater(this, Math.max(20L, delay * 20L)); // Convert seconds to ticks, ensure at least 1 tick
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error scheduling daily reset", e);
        }
    }

    public void resetResourceWorld() {
        try {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                getLogger().warning("Resource world not found!");
                return;
            }

            // Teleport all players out of the resource world
            for (Player player : world.getPlayers()) {
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                player.sendMessage("§eYou have been teleported out of the resource world due to a reset.");
            }

            MVWorldManager worldManager = core.getMVWorldManager();

            // Unload the world
            boolean unloadSuccess = worldManager.unloadWorld(worldName);
            if (!unloadSuccess) {
                getLogger().severe("Failed to unload resource world!");
                return;
            }

            // Delete the world
            boolean deleteSuccess = worldManager.deleteWorld(worldName, true, true);
            if (!deleteSuccess) {
                getLogger().severe("Failed to delete resource world!");
                return;
            }

            // Create a new world
            boolean createSuccess = worldManager.addWorld(
                    worldName,
                    World.Environment.NORMAL,
                    null,
                    WorldType.NORMAL,
                    true,
                    null
            );

            if (!createSuccess) {
                getLogger().severe("Failed to create new resource world!");
                return;
            }

            Bukkit.broadcastMessage("§aThe resource world has been successfully reset!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error resetting resource world", e);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("ResourceWorldResetter has been disabled.");
    }

    // Handle commands for setting world, reset interval, etc.
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
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
                            if (restartTime < 0 || restartTime > 23) {
                                sender.sendMessage("§cPlease provide a valid hour (0-23).");
                                return true;
                            }
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
                return true;
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error processing command", e);
            sender.sendMessage("§cAn error occurred while processing your command.");
            return true;
        }
        return false;
    }

    private void reloadPlugin(CommandSender sender) {
        try {
            this.reloadConfig();
            loadConfig(); // Reload configuration values

            // Check if world exists and load it
            if (worldName != null && !worldName.isEmpty()) {
                loadWorld();
                // Reschedule the daily reset task
                scheduleDailyReset();
                sender.sendMessage("§aResourceWorldResetter plugin has been reloaded successfully.");
            } else {
                sender.sendMessage("§cNo world name specified in the config file! Please set 'worldName' in the config.yml.");
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error reloading plugin", e);
            sender.sendMessage("§cAn error occurred while reloading the plugin.");
        }
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setResetInterval(long resetInterval) {
        this.resetInterval = resetInterval;
    }

    public long getResetInterval() {
        return resetInterval;
    }

    public void setResetInterval(long interval, TimeUnit unit) {
    }

    public void setRestartTime(int restartTime) {
        this.restartTime = restartTime;
    }

    public int getRestartTime() {
        return restartTime;
    }

    public void reloadPlugin() {
    }

    public void setRestartTime(int hour, int minute) {
    }
}