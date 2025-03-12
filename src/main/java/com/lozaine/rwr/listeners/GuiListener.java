package com.lozaine.rwr.listeners;

import com.lozaine.rwr.commands.GuiCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

public class GuiListener implements Listener {
    private final ResourceWorldResetter plugin;
    private final GuiCommand guiCommand;

    public GuiListener(ResourceWorldResetter plugin, GuiCommand guiCommand) {
        this.plugin = plugin;
        this.guiCommand = guiCommand;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        event.setCancelled(true);

        switch (title) {
            case "ResourceWorldResetter Config":
                handleMainMenu(player, clickedItem);
                break;
            case "Select a World to Reset":
                handleWorldSelection(player, clickedItem);
                break;
            case "Set Reset Interval":
                handleIntervalSelection(player, clickedItem);
                break;
            case "Set Reset Time":
                handleTimeSelection(player, clickedItem);
                break;
            default:
                player.sendMessage("§cInvalid menu title.");
                break;
        }
    }

    private void handleMainMenu(Player player, ItemStack clickedItem) {
        switch (clickedItem.getType()) {
            case GRASS_BLOCK:
                guiCommand.openManageWorldMenu(player);
                break;
            case CLOCK:
                guiCommand.openResetIntervalMenu(player);
                break;
            case COMPASS:
                guiCommand.openResetTimeMenu(player);
                break;
            case REDSTONE:
                player.closeInventory();
                plugin.resetResourceWorld();
                player.sendMessage("§aInitiating immediate world reset...");
                break;
            case BARRIER:
                player.closeInventory();
                break;
            default:
                player.sendMessage("§cInvalid option selected.");
                break;
        }
    }

    private void handleWorldSelection(Player player, ItemStack clickedItem) {
        if (clickedItem.getType() == Material.ARROW) {
            guiCommand.openMainMenu(player);
        } else {
            String worldName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            plugin.setWorldName(worldName);
            player.sendMessage("§aResource world set to: " + worldName);
            guiCommand.openMainMenu(player);
        }
    }

    private void handleIntervalSelection(Player player, ItemStack clickedItem) {
        if (clickedItem.getType() == Material.ARROW) {
            guiCommand.openMainMenu(player);
        } else {
            String intervalName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            long interval = 0;
            TimeUnit unit = TimeUnit.HOURS;

            switch (intervalName) {
                case "24 Hours":
                    interval = 24;
                    break;
                case "1 Week":
                    interval = 7;
                    unit = TimeUnit.DAYS;
                    break;
                case "1 Month":
                    interval = 30;
                    unit = TimeUnit.DAYS;
                    break;
                case "Custom":
                    openCustomIntervalInput(player);
                    return;
                default:
                    player.sendMessage("§cInvalid interval option.");
                    return;
            }

            plugin.setResetInterval(interval, unit);
            player.sendMessage("§aReset interval set to " + intervalName);
            guiCommand.openMainMenu(player);
        }
    }

    private void handleTimeSelection(Player player, ItemStack clickedItem) {
        if (clickedItem.getType() == Material.ARROW) {
            guiCommand.openMainMenu(player);
        } else {
            String timeName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            String[] timeParts = timeName.split(":");

            if (timeParts.length == 2) {
                try {
                    int hour = Integer.parseInt(timeParts[0]);
                    int minute = Integer.parseInt(timeParts[1]);
                    plugin.setRestartTime(hour, minute);
                    player.sendMessage("§aReset time set to " + timeName);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cInvalid time format.");
                }
            } else {
                player.sendMessage("§cInvalid time format.");
            }
            guiCommand.openMainMenu(player);
        }
    }

    private void openCustomIntervalInput(Player player) {
        player.closeInventory();
        player.sendMessage("§eUse /setresetinterval <hours> to set a custom reset interval.");
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Optional: Add cleanup logic if needed when inventory closes
    }
}