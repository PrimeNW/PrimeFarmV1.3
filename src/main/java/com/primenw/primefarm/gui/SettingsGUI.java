package com.primenw.primefarm.gui;

import com.primenw.primefarm.PrimeFarm;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class SettingsGUI {

    public static final int SIZE = 27;

    private final PrimeFarm plugin;

    public SettingsGUI(PrimeFarm plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        String title = plugin.getLanguageManager().get(player, "settings-title");
        Inventory inv = plugin.getServer().createInventory(new SettingsHolder(), SIZE, title);

        List<Material> materials = plugin.getPlayerSettingsManager().getToggleableMaterials();

        int slot = 0;
        for (Material material : materials) {
            if (slot >= SIZE) break;
            boolean enabled = plugin.getPlayerSettingsManager().isEnabled(player.getUniqueId(), material);

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName((enabled ? "\u00A7a" : "\u00A7c") + prettyName(material));

            String stateKey = enabled ? "settings-enabled" : "settings-disabled";
            List<String> lore = plugin.getLanguageManager().getList(player, stateKey);
            meta.setLore(lore);
            item.setItemMeta(meta);

            inv.setItem(slot, item);
            slot++;
        }

        player.openInventory(inv);
    }

    private String prettyName(Material mat) {
        String[] parts = mat.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
        return sb.toString().trim();
    }
}
