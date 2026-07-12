package com.primenw.primefarm.gui;

import com.primenw.primefarm.PrimeFarm;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 54 slotluk, 5 sayfali depo/satis arayuzu.
 * Materyaller herhangi bir sayfaya sabitlenmez: tum materyaller ortak, sirali
 * bir yigin listesi olarak tutulur; sayfa 1'in 45 slotu dolunca otomatik
 * olarak sayfa 2'ye tasar, o da dolunca sayfa 3'e, vs.
 * Slot duzeni: 0-44 urun gosterimi, 45 onceki sayfa, 49 tumunu sat, 51 hepsini al, 53 sonraki sayfa.
 */
public class StorageGUI {

    public static final int SIZE = 54;
    public static final int PREV_SLOT = 45;
    public static final int SELL_ALL_SLOT = 49;
    public static final int WITHDRAW_ALL_SLOT = 51;
    public static final int NEXT_SLOT = 53;
    public static final int ITEMS_PER_PAGE = 45;

    private final PrimeFarm plugin;

    public StorageGUI(PrimeFarm plugin) {
        this.plugin = plugin;
    }

    public String titleFor(Player player, int page) {
        String base = plugin.getLanguageManager().get(player, "gui-title");
        return base + " (" + page + "/" + com.primenw.primefarm.managers.PageManager.TOTAL_PAGES + ")";
    }

    public void open(Player player, int page) {
        page = Math.max(1, Math.min(page, com.primenw.primefarm.managers.PageManager.TOTAL_PAGES));
        Inventory inv = plugin.getServer().createInventory(new PageHolder(page), SIZE, titleFor(player, page));

        if (!plugin.getPageManager().hasAccess(player, page)) {
            fillLocked(inv, player, page);
        } else {
            fillMaterials(inv, player, page);
        }

        addNavButtons(inv, player, page);
        player.openInventory(inv);
    }

    private void fillLocked(Inventory inv, Player player, int page) {
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta meta = barrier.getItemMeta();
        meta.setDisplayName(plugin.getLanguageManager().get(player, "gui-page-locked-name"));

        List<String> lore;
        if (plugin.getPageManager().isPurchasablePage(page)) {
            double price = plugin.getPageManager().getPagePrice(page);
            lore = plugin.getLanguageManager().getList(player, "gui-page-locked-lore-buy",
                    "%price%", String.valueOf(price),
                    "%currency%", plugin.getEconomyManager().getCurrencyName());
        } else {
            lore = plugin.getLanguageManager().getList(player, "gui-page-locked-lore-permission",
                    "%permission%", plugin.getPageManager().getPagePermission(page));
        }
        meta.setLore(lore);
        barrier.setItemMeta(meta);

        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            inv.setItem(i, barrier);
        }
    }

    /**
     * Depodaki tum materyalleri (config'teki tracked-materials sirasina gore) 64'luk
     * gercek yiginlar halinde tek bir sirali listeye donusturur. Bu liste sayfalar
     * arasinda otomatik bolusturulur (sayfa 1: 0-44, sayfa 2: 45-89, ...).
     */
    private List<ItemStack> buildAllStacks(Player player) {
        Map<Material, Integer> storage = plugin.getStorageManager().getAll(player.getUniqueId());
        List<ItemStack> stacks = new ArrayList<>();

        for (String name : plugin.getConfig().getStringList("tracked-materials")) {
            Material material = Material.matchMaterial(name);
            if (material == null) continue;

            int remaining = storage.getOrDefault(material, 0);
            while (remaining > 0) {
                int stackAmount = Math.min(remaining, 64);
                ItemStack item = new ItemStack(material, stackAmount);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("\u00A7f" + prettyName(material));
                item.setItemMeta(meta);
                stacks.add(item);
                remaining -= stackAmount;
            }
        }
        return stacks;
    }

    private void fillMaterials(Inventory inv, Player player, int page) {
        List<ItemStack> all = buildAllStacks(player);
        int start = (page - 1) * ITEMS_PER_PAGE;

        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int idx = start + i;
            if (idx >= all.size()) break;
            inv.setItem(i, all.get(idx));
        }

        ItemStack sellAll = new ItemStack(Material.EMERALD);
        ItemMeta sellMeta = sellAll.getItemMeta();
        sellMeta.setDisplayName(plugin.getLanguageManager().get(player, "gui-sell-all-name"));
        sellMeta.setLore(plugin.getLanguageManager().getList(player, "gui-sell-all-lore"));
        sellAll.setItemMeta(sellMeta);
        inv.setItem(SELL_ALL_SLOT, sellAll);

        ItemStack withdrawAll = new ItemStack(Material.CHEST);
        ItemMeta withdrawMeta = withdrawAll.getItemMeta();
        withdrawMeta.setDisplayName(plugin.getLanguageManager().get(player, "gui-withdraw-all-name"));
        withdrawMeta.setLore(plugin.getLanguageManager().getList(player, "gui-withdraw-all-lore"));
        withdrawAll.setItemMeta(withdrawMeta);
        inv.setItem(WITHDRAW_ALL_SLOT, withdrawAll);
    }

    private void addNavButtons(Inventory inv, Player player, int page) {
        if (page > 1) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta meta = prev.getItemMeta();
            meta.setDisplayName(plugin.getLanguageManager().get(player, "gui-nav-prev"));
            prev.setItemMeta(meta);
            inv.setItem(PREV_SLOT, prev);
        }
        if (page < com.primenw.primefarm.managers.PageManager.TOTAL_PAGES) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta meta = next.getItemMeta();
            meta.setDisplayName(plugin.getLanguageManager().get(player, "gui-nav-next"));
            next.setItemMeta(meta);
            inv.setItem(NEXT_SLOT, next);
        }
    }

    private String prettyName(Material mat) {
        String[] parts = mat.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
        return sb.toString().trim();
    }
}
