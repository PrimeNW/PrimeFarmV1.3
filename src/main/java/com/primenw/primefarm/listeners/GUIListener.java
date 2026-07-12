package com.primenw.primefarm.listeners;

import com.primenw.primefarm.PrimeFarm;
import com.primenw.primefarm.gui.PageHolder;
import com.primenw.primefarm.gui.StorageGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class GUIListener implements Listener {

    private final PrimeFarm plugin;

    public GUIListener(PrimeFarm plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSettingsClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof com.primenw.primefarm.gui.SettingsHolder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        plugin.getPlayerSettingsManager().toggle(player.getUniqueId(), clicked.getType());
        plugin.getSettingsGUI().open(player);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof PageHolder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        UUID uuid = player.getUniqueId();
        int page = ((PageHolder) event.getInventory().getHolder()).getPage();

        int slot = event.getRawSlot();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Onceki/sonraki sayfa butonlari kilitli sayfada da calismali,
        // yoksa oyuncu kilitli bir sayfaya girince geri donemez.
        if (slot == StorageGUI.PREV_SLOT) {
            plugin.getStorageGUI().open(player, page - 1);
            return;
        }
        if (slot == StorageGUI.NEXT_SLOT) {
            plugin.getStorageGUI().open(player, page + 1);
            return;
        }

        // Kilitli sayfa: barrier'a tiklamak satin alma denemesi baslatir.
        if (!plugin.getPageManager().hasAccess(player, page)) {
            if (clicked.getType() == Material.BARRIER) {
                tryPurchasePage(player, page);
            }
            return;
        }

        if (slot == StorageGUI.SELL_ALL_SLOT) {
            sellPage(player, uuid, page);
            return;
        }
        if (slot == StorageGUI.WITHDRAW_ALL_SLOT) {
            withdrawPage(player, uuid, page);
            return;
        }
        if (slot >= StorageGUI.ITEMS_PER_PAGE) return;

        Material material = clicked.getType();
        withdrawOne(player, uuid, material, clicked.getAmount());
    }

    private java.util.List<Material> trackedMaterials() {
        java.util.List<Material> list = new java.util.ArrayList<>();
        for (String name : plugin.getConfig().getStringList("tracked-materials")) {
            Material mat = Material.matchMaterial(name);
            if (mat != null) list.add(mat);
        }
        return list;
    }

    private void tryPurchasePage(Player player, int page) {
        if (!plugin.getPageManager().isPurchasablePage(page)) {
            plugin.getLanguageManager().send(player, "page-locked-permission");
            return;
        }
        if (plugin.getPageManager().isPurchased(player.getUniqueId(), page)) {
            plugin.getLanguageManager().send(player, "page-already-unlocked");
            return;
        }

        double price = plugin.getPageManager().getPagePrice(page);
        double balance = plugin.getEconomyManager().getBalance(player);

        if (balance < price) {
            plugin.getLanguageManager().send(player, "page-not-enough-money",
                    "%price%", String.valueOf(price),
                    "%currency%", plugin.getEconomyManager().getCurrencyName());
            return;
        }

        plugin.getEconomyManager().withdraw(player, price);
        plugin.getPageManager().markPurchased(player.getUniqueId(), page);

        plugin.getLanguageManager().send(player, "page-purchase-success",
                "%page%", String.valueOf(page),
                "%price%", String.valueOf(price),
                "%currency%", plugin.getEconomyManager().getCurrencyName());

        plugin.getStorageGUI().open(player, page);
    }

    private void withdrawPage(Player player, UUID uuid, int page) {
        java.util.List<Material> materials = trackedMaterials();
        boolean anyTaken = false;
        boolean anyLeftover = false;

        for (Material material : materials) {
            int available = plugin.getStorageManager().get(uuid, material);
            if (available <= 0) continue;

            int freeSpace = freeInventorySpace(player, material);
            if (freeSpace <= 0) {
                anyLeftover = true;
                continue;
            }

            int toGive = Math.min(available, freeSpace);
            int taken = plugin.getStorageManager().removeAmount(uuid, material, toGive);
            if (taken > 0) {
                giveItems(player, material, taken);
                anyTaken = true;
            }
            if (taken < available) anyLeftover = true;
        }

        if (!anyTaken) {
            plugin.getLanguageManager().send(player, anyLeftover ? "withdraw-full" : "storage-empty");
        } else if (anyLeftover) {
            plugin.getLanguageManager().send(player, "withdraw-page-partial");
        } else {
            plugin.getLanguageManager().send(player, "withdraw-page-success");
        }

        plugin.getStorageGUI().open(player, page);
    }

    private void sellPage(Player player, UUID uuid, int page) {
        java.util.List<Material> materials = trackedMaterials();
        double totalEarned = 0.0;
        boolean any = false;

        for (Material material : materials) {
            int amount = plugin.getStorageManager().takeAll(uuid, material);
            if (amount <= 0) continue;
            any = true;
            double price = plugin.getConfig().getDouble("prices." + material.name(), 0.0);
            totalEarned += price * amount;
        }

        if (!any) {
            plugin.getLanguageManager().send(player, "storage-empty");
            return;
        }

        plugin.getEconomyManager().deposit(player, totalEarned);
        plugin.getLanguageManager().send(player, "sold-all",
                "%amount%", String.format("%.2f", totalEarned),
                "%currency%", plugin.getEconomyManager().getCurrencyName());

        plugin.getStorageGUI().open(player, page);
    }

    private void withdrawOne(Player player, UUID uuid, Material material, int desiredAmount) {
        int available = plugin.getStorageManager().get(uuid, material);
        if (available <= 0) {
            plugin.getLanguageManager().send(player, "withdraw-empty");
            return;
        }

        int wanted = Math.min(desiredAmount, available);
        int freeSpace = freeInventorySpace(player, material);
        if (freeSpace <= 0) {
            plugin.getLanguageManager().send(player, "withdraw-full");
            return;
        }

        int toGive = Math.min(wanted, freeSpace);
        int taken = plugin.getStorageManager().removeAmount(uuid, material, toGive);
        giveItems(player, material, taken);

        if (taken < wanted) {
            plugin.getLanguageManager().send(player, "withdraw-partial",
                    "%material%", material.name(),
                    "%amount%", String.valueOf(taken));
        } else {
            plugin.getLanguageManager().send(player, "withdraw-success",
                    "%material%", material.name(),
                    "%amount%", String.valueOf(taken));
        }

        refreshCurrentPage(player);
    }

    private int freeInventorySpace(Player player, Material material) {
        int free = 0;
        int maxStack = material.getMaxStackSize();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                free += maxStack;
            } else if (item.getType() == material && item.getAmount() < maxStack) {
                free += (maxStack - item.getAmount());
            }
        }
        return free;
    }

    private void giveItems(Player player, Material material, int amount) {
        int maxStack = material.getMaxStackSize();
        while (amount > 0) {
            int stackSize = Math.min(amount, maxStack);
            player.getInventory().addItem(new ItemStack(material, stackSize));
            amount -= stackSize;
        }
    }

    private void refreshCurrentPage(Player player) {
        if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof PageHolder)) return;
        int page = ((PageHolder) player.getOpenInventory().getTopInventory().getHolder()).getPage();
        plugin.getStorageGUI().open(player, page);
    }
}
