package com.primenw.primefarm.managers;

import com.primenw.primefarm.PrimeFarm;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PrimeFarm deposu 5 sayfadan olusur:
 *  - Sayfa 1 ve 2: oyuncu oyun ici para ile satin alarak acar (config: page-prices).
 *  - Sayfa 3, 4, 5: belirli bir izne (dolayisiyla rutbeye) sahip olanlar acabilir
 *    (config: page-permissions). Sunucu tarafinda bu izinler VIP/VIP+/VIP++/Astral/Prime
 *    gruplarina LuckPerms vb. bir permission eklentisiyle atanmalidir.
 */
public class PageManager {

    public static final int TOTAL_PAGES = 5;

    private final PrimeFarm plugin;
    private final File file;

    // Sadece parayla acilan sayfalar (1 ve 2) icin satin alma kaydi.
    private final Map<UUID, Set<Integer>> purchasedPages = new ConcurrentHashMap<>();

    public PageManager(PrimeFarm plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "pages.yml");
        load();
    }

    /**
     * Bir sayfanin para ile mi yoksa izin ile mi acildigini belirtir.
     */
    public boolean isPurchasablePage(int page) {
        return page == 1 || page == 2;
    }

    public boolean isPermissionPage(int page) {
        return page >= 3 && page <= 5;
    }

    public double getPagePrice(int page) {
        return plugin.getConfig().getDouble("page-prices.page" + page, 0.0);
    }

    public String getPagePermission(int page) {
        return plugin.getConfig().getString("page-permissions.page" + page, "primefarm.page" + page);
    }

    /**
     * Oyuncunun bu sayfayi gorup kullanabilecegini kontrol eder.
     */
    public boolean hasAccess(Player player, int page) {
        if (isPermissionPage(page)) {
            return player.hasPermission(getPagePermission(page)) || player.hasPermission("primefarm.admin");
        }
        if (isPurchasablePage(page)) {
            return purchasedPages.getOrDefault(player.getUniqueId(), java.util.Collections.emptySet()).contains(page);
        }
        return true;
    }

    public boolean isPurchased(UUID player, int page) {
        return purchasedPages.getOrDefault(player, java.util.Collections.emptySet()).contains(page);
    }

    public void markPurchased(UUID player, int page) {
        purchasedPages.computeIfAbsent(player, k -> new HashSet<>()).add(page);
        save();
    }

    /**
     * Oyuncunun kac sayfaya erisimi oldugunu hesaplar (depo kapasitesi icin kullanilir).
     * Not: 3-5. sayfalar izne bagli oldugundan sadece oyuncu cevrimiciyken dogru
     * sayilabilir; cevrimdisiyken bu sayfalar kapasiteye dahil edilmez (guvenli varsayim).
     */
    /**
     * Belirli bir sayfanin oyuncu icin acik olup olmadigini kontrol eder (UUID uzerinden,
     * oyuncu cevrimdisi olsa da calisir - satin alinan sayfalar icin). Izin bazli sayfalar
     * (3-5) icin oyuncunun cevrimici olmasi gerekir, degilse guvenli varsayimla kapali sayilir.
     */
    public boolean isPageUnlockedFor(UUID uuid, int page) {
        if (isPurchasablePage(page)) {
            return isPurchased(uuid, page);
        }
        if (isPermissionPage(page)) {
            org.bukkit.entity.Player p = plugin.getServer().getPlayer(uuid);
            return p != null && (p.hasPermission(getPagePermission(page)) || p.hasPermission("primefarm.admin"));
        }
        return true;
    }

    public int unlockedPageCount(UUID uuid) {
        int count = 0;
        for (int page = 1; page <= TOTAL_PAGES; page++) {
            if (isPageUnlockedFor(uuid, page)) count++;
        }
        return count;
    }

    // ---------- Kalicilik ----------

    public void load() {
        purchasedPages.clear();
        if (!file.exists()) return;

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        if (cfg.getConfigurationSection("purchased") == null) return;

        for (String uuidStr : cfg.getConfigurationSection("purchased").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                Set<Integer> pages = new HashSet<>(cfg.getIntegerList("purchased." + uuidStr));
                purchasedPages.put(uuid, pages);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void save() {
        FileConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, Set<Integer>> entry : purchasedPages.entrySet()) {
            cfg.set("purchased." + entry.getKey().toString(), new java.util.ArrayList<>(entry.getValue()));
        }
        try {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("pages.yml kaydedilemedi: " + e.getMessage());
        }
    }
}
