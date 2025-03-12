package com.lozaine.rwr.commands;

import com.lozaine.rwr.ResourceWorldResetter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class GuiCommand implements CommandExecutor {

    private final ResourceWorldResetter plugin;

    public GuiCommand(ResourceWorldResetter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        openMainMenu(player);
        return true;
    }

    public void openMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 36, "ResourceWorldResetter Config");

        gui.setItem(10, createGuiItem(Material.GRASS_BLOCK, "Manage World", "Click to manage the resource world"));
        gui.setItem(12, createGuiItem(Material.CLOCK, "Set Reset Interval", "Click to set the reset interval"));
        gui.setItem(14, createGuiItem(Material.COMPASS, "Set Reset Time", "Click to set the reset time"));
        gui.setItem(16, createGuiItem(Material.REDSTONE, "Reset Now", "Click to reset the world immediately"));
        gui.setItem(31, createGuiItem(Material.BARRIER, "Close", "Close the menu"));

        player.openInventory(gui);
    }

    public void openManageWorldMenu(Player player) {
        List<World> worlds = Bukkit.getWorlds();
        int size = Math.min(54, ((worlds.size() - 1) / 9 + 1) * 9);
        Inventory worldMenu = Bukkit.createInventory(null, size, "Select a World to Reset");

        for (int i = 0; i < worlds.size() && i < size - 1; i++) {
            World world = worlds.get(i);
            worldMenu.setItem(i, createGuiItem(Material.GRASS_BLOCK, world.getName(), "Click to select this world for reset"));
        }

        worldMenu.setItem(size - 1, createGuiItem(Material.ARROW, "Back", "Return to main menu"));

        player.openInventory(worldMenu);
    }

    public void openResetIntervalMenu(Player player) {
        Inventory intervalMenu = Bukkit.createInventory(null, 27, "Set Reset Interval");

        intervalMenu.setItem(10, createGuiItem(Material.PAPER, "24 Hours", "Click to set reset interval to 24 hours"));
        intervalMenu.setItem(12, createGuiItem(Material.PAPER, "1 Week", "Click to set reset interval to 1 week"));
        intervalMenu.setItem(14, createGuiItem(Material.PAPER, "1 Month", "Click to set reset interval to 1 month"));
        intervalMenu.setItem(16, createGuiItem(Material.PAPER, "Custom", "Click to set a custom reset interval"));
        intervalMenu.setItem(26, createGuiItem(Material.ARROW, "Back", "Return to main menu"));

        player.openInventory(intervalMenu);
    }

    public void openResetTimeMenu(Player player) {
        Inventory timeMenu = Bukkit.createInventory(null, 54, "Set Reset Time");

        for (int i = 0; i < 24; i++) {
            timeMenu.setItem(i, createGuiItem(Material.CLOCK, String.format("%02d:00", i), "Click to set reset time to " + String.format("%02d:00", i)));
        }

        timeMenu.setItem(53, createGuiItem(Material.ARROW, "Back", "Return to main menu"));

        player.openInventory(timeMenu);
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6" + name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}
