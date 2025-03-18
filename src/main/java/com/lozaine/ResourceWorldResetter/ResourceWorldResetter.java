package com.lozaine.ResourceWorldResetter;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;

public class ResourceWorldResetter extends JavaPlugin {
    private String worldName;
    private MultiverseCore core;
    private long resetInterval;
    private int restartTime;
    private int resetWarningTime;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        core = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");

        if (core == null) {
            getLogger().severe("Multiverse-Core not found! Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        loadConfig();
        ensureResourceWorldExists();
        scheduleDailyReset();

        getLogger().info("ResourceWorldResetter enabled successfully!");
    }

    private void loadConfig() {
        reloadConfig();
        worldName = getConfig().getString("worldName", "Resources");
        resetInterval = getConfig().getLong("resetInterval", 86400);
        restartTime = getConfig().getInt("restartTime", 3);
        resetWarningTime = getConfig().getInt("resetWarningTime", 5);

        if (worldName == null || worldName.isEmpty()) {
            worldName = "Resources";
            getConfig().set("worldName", worldName);
            saveConfig();
        }
    }

    private void ensureResourceWorldExists() {
        MVWorldManager worldManager = core.getMVWorldManager();
        if (!worldManager.isMVWorld(worldName)) {
            getLogger().info("Creating resource world: " + worldName);
            worldManager.addWorld(worldName, World.Environment.NORMAL, null, WorldType.NORMAL, true, "DEFAULT");
        }
    }

    private void scheduleDailyReset() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime resetTime = LocalTime.of(restartTime, 0);
        LocalDateTime nextReset = now.with(resetTime);
        if (now.isAfter(nextReset)) nextReset = nextReset.plusDays(1);

        long delay = java.time.Duration.between(now, nextReset).toSeconds();
        long warningDelay = delay - (resetWarningTime * 60);

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("§e[ResourceWorldResetter] Warning: The resource world will reset in " + resetWarningTime + " minutes!");
            }
        }.runTaskLater(this, warningDelay * 20L);

        new BukkitRunnable() {
            @Override
            public void run() {
                resetResourceWorld();
                scheduleDailyReset();
            }
        }.runTaskLater(this, delay * 20L);

        getLogger().info("Next resource world reset scheduled for: " + nextReset);
    }

    private void resetResourceWorld() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            ensureResourceWorldExists();
            return;
        }

        Bukkit.broadcastMessage("§e[ResourceWorldResetter] Resetting resource world...");
        for (Player player : world.getPlayers()) {
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            player.sendMessage("§e[ResourceWorldResetter] You have been teleported out of the resource world.");
        }

        MVWorldManager worldManager = core.getMVWorldManager();
        boolean unloaded = worldManager.unloadWorld(worldName);
        if (!unloaded) {
            getLogger().severe("Failed to unload world: " + worldName);
            return;
        }

        CompletableFuture.runAsync(() -> {
            File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
            if (deleteFolder(worldFolder)) {
                Bukkit.getScheduler().runTask(this, () -> recreateWorld(worldManager));
            } else {
                getLogger().severe("Failed to delete world folder: " + worldName);
            }
        });
    }

    private boolean deleteFolder(File folder) {
        if (!folder.exists()) return true;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        return folder.delete();
    }

    private void recreateWorld(MVWorldManager worldManager) {
        boolean success = worldManager.addWorld(worldName, World.Environment.NORMAL, null, WorldType.NORMAL, true, "DEFAULT");
        if (success) {
            Bukkit.broadcastMessage("§a[ResourceWorldResetter] The resource world has been reset!");
        } else {
            Bukkit.broadcastMessage("§c[ResourceWorldResetter] Failed to recreate the resource world!");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("ResourceWorldResetter disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) || sender.hasPermission("resourceworldresetter.admin")) {
            if (command.getName().equalsIgnoreCase("setworld")) {
                if (args.length > 0) {
                    worldName = args[0];
                    getConfig().set("worldName", worldName);
                    saveConfig();
                    sender.sendMessage("§aResource world set to: " + worldName);
                    ensureResourceWorldExists();
                } else {
                    sender.sendMessage("§cUsage: /setworld <worldname>");
                }
                return true;
            } else if (command.getName().equalsIgnoreCase("setrestarttime")) {
                if (args.length > 0) {
                    try {
                        int hour = Integer.parseInt(args[0]);
                        if (hour < 0 || hour > 23) {
                            sender.sendMessage("§cInvalid hour (0-23).");
                            return true;
                        }
                        restartTime = hour;
                        getConfig().set("restartTime", restartTime);
                        saveConfig();
                        sender.sendMessage("§aServer restart time set to: " + restartTime + ":00");
                        scheduleDailyReset();
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cPlease enter a valid hour (0-23).");
                    }
                } else {
                    sender.sendMessage("§cUsage: /setrestarttime <hour>");
                }
                return true;
            } else if (command.getName().equalsIgnoreCase("resetworld")) {
                sender.sendMessage("§aForcing resource world reset...");
                resetResourceWorld();
                return true;
            }
        } else {
            sender.sendMessage("§cYou do not have permission to use this command.");
        }
        return false;
    }
}
