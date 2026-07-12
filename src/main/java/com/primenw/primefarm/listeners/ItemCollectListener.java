package com.primenw.primefarm.listeners;

import com.primenw.primefarm.PrimeFarm;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Bu listener PrimeFarm'in kalbidir.
 *
 * Kaktus, seker kamisi vb. bloklar herhangi bir sekilde kirildiginda
 * (elle, pistonla, fizik nedeniyle dusme vb.) sunucu bir ItemSpawnEvent tetikler.
 * Eger bu event bir oyuncunun arsasi icinde ve takip edilen bir materyal icin
 * gerceklesiyorsa, itemin dunyaya dusmesini iptal edip miktari dogrudan
 * o arsanin sahibinin sanal deposuna ekliyoruz. Boylece hem hopper/zincir
 * kurmaya gerek kalmiyor hem de item lag'i olusmuyor.
 */
public class ItemCollectListener implements Listener {

    private final PrimeFarm plugin;
    private final Set<Material> tracked = new HashSet<>();

    public ItemCollectListener(PrimeFarm plugin) {
        this.plugin = plugin;
        reloadTrackedMaterials();
    }

    public void reloadTrackedMaterials() {
        tracked.clear();
        List<String> names = plugin.getConfig().getStringList("tracked-materials");
        for (String name : names) {
            Material mat = Material.matchMaterial(name);
            if (mat != null) {
                tracked.add(mat);
            } else {
                plugin.getLogger().warning("config.yml -> tracked-materials icinde bilinmeyen materyal: " + name);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item itemEntity = event.getEntity();
        ItemStack stack = itemEntity.getItemStack();
        Material material = stack.getType();

        if (!tracked.contains(material)) return;

        Location loc = itemEntity.getLocation();
        java.util.UUID owner = plugin.getPlotHook().getOwnerAt(loc);
        if (owner == null) return;

        if (!plugin.getPlayerSettingsManager().isEnabled(owner, material)) return; // oyuncu bu farmi kapatmis

        int maxSlots = plugin.getPageManager().unlockedPageCount(owner) * com.primenw.primefarm.managers.StorageManager.SLOTS_PER_PAGE;
        int overflow = plugin.getStorageManager().addWithCapacity(owner, material, stack.getAmount(), maxSlots);

        if (overflow > 0) {
            // Depoda yer kalmadi: sigmayan kismi otomatik olarak satip parasini yatiriyoruz.
            double price = plugin.getConfig().getDouble("prices." + material.name(), 0.0);
            org.bukkit.OfflinePlayer offlineOwner = plugin.getServer().getOfflinePlayer(owner);
            plugin.getEconomyManager().deposit(offlineOwner, price * overflow);
        }

        // Item her durumda depoya/otomatik satisa gitti, dunyaya hic dusmesin.
        event.setCancelled(true);
    }
}
