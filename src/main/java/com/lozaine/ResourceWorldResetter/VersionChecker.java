package com.lozaine.ResourceWorldResetter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VersionChecker {
    private final ResourceWorldResetter plugin;
    private final String currentVersion;
    private String latestVersion;
    private boolean updateAvailable = false;
    private final String updateCheckUrl;

    public VersionChecker(ResourceWorldResetter plugin, String updateCheckUrl) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
        this.updateCheckUrl = updateCheckUrl;
    }

    /**
     * Start the version checker scheduler
     */
    public void startVersionChecker() {
        // First check immediately when plugin starts
        checkVersion();

        // Then schedule periodic checks (every 12 hours)
        new BukkitRunnable() {
            @Override
            public void run() {
                checkVersion();
            }
        }.runTaskTimerAsynchronously(plugin, 20L * 60 * 60 * 12, 20L * 60 * 60 * 12);
    }

    /**
     * Check for updates to the plugin
     */
    private void checkVersion() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(updateCheckUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        latestVersion = reader.readLine().trim();
                        reader.close();

                        if (!currentVersion.equals(latestVersion)) {
                            updateAvailable = true;
                            // Log to console
                            plugin.getLogger().info("Update available! Current version: " + currentVersion + ", Latest version: " + latestVersion);
                            plugin.getLogger().info("Download the latest version at: https://github.com/Lozaine/ResourceWorldResetter/releases");

                            // Notify admins in game
                            notifyAdmins();
                        } else {
                            updateAvailable = false;
                            plugin.getLogger().info("ResourceWorldResetter is up to date!");
                        }
                    } else {
                        plugin.getLogger().warning("Failed to check for updates. Response code: " + responseCode);
                    }
                } catch (IOException e) {
                    plugin.getLogger().warning("Error checking for updates: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Notify admins in game about available updates
     */
    private void notifyAdmins() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("resourceworldresetter.admin")) {
                player.sendMessage(ChatColor.YELLOW + "[ResourceWorldResetter] " + ChatColor.AQUA + "Update available! Current version: "
                        + ChatColor.RED + currentVersion + ChatColor.AQUA + ", Latest version: " + ChatColor.GREEN + latestVersion);
                player.sendMessage(ChatColor.YELLOW + "[ResourceWorldResetter] " + ChatColor.AQUA + "Download it here: "
                        + ChatColor.GREEN + "https://github.com/Lozaine/ResourceWorldResetter/releases");
            }
        }
    }

    /**
     * Notify a specific player about update if they're an admin
     */
    public void notifyPlayer(Player player) {
        if (updateAvailable && player.hasPermission("resourceworldresetter.admin")) {
            player.sendMessage(ChatColor.YELLOW + "[ResourceWorldResetter] " + ChatColor.AQUA + "Update available! Current version: "
                    + ChatColor.RED + currentVersion + ChatColor.AQUA + ", Latest version: " + ChatColor.GREEN + latestVersion);
            player.sendMessage(ChatColor.YELLOW + "[ResourceWorldResetter] " + ChatColor.AQUA + "Download it here: "
                    + ChatColor.GREEN + "https://github.com/Lozaine/ResourceWorldResetter/releases");
        }
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}