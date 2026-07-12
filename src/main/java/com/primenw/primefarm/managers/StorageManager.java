package com.primenw.primefarm.managers;

import com.primenw.primefarm.PrimeFarm;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StorageManager {

    public static final int SLOTS_PER_PAGE = 45;
    public static final int STACK_SIZE = 64;

    private final PrimeFarm plugin;
    private final File file;
    private final Map<UUID, Map<Material, Integer>> storage = new ConcurrentHashMap<>();

    public StorageManager(PrimeFarm plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "storage.yml");
        load();
    }

    public int add(UUID player, Material material, int amount) {
        // Geriye uyumluluk icin: kapasitesiz ekleme (test/admin amacli).
        if (amount <= 0) return 0;
        Map<Material, Integer> map = storage.computeIfAbsent(player, k -> new EnumMap<>(Material.class));
        map.merge(material, amount, Integer::sum);
        return 0;
    }

    /** O oyuncunun deposunda su an kullanilan toplam slot sayisi (her 64 adet = 1 slot). */
    public int usedSlots(UUID player) {
        Map<Material, Integer> map = storage.get(player);
        if (map == null) return 0;
        int slots = 0;
        for (int amount : map.values()) {
            slots += (int) Math.ceil(amount / (double) STACK_SIZE);
        }
        return slots;
    }

    public int get(UUID player, Material material) {
        Map<Material, Integer> map = storage.get(player);
        if (map == null) return 0;
        return map.getOrDefault(material, 0);
    }

    public Map<Material, Integer> getAll(UUID player) {
        return storage.getOrDefault(player, new EnumMap<>(Material.class));
    }

    /**
     * Belirtilen materyalden tamamini sifirlar ve o ana kadarki miktari dondurur.
     */
    public int takeAll(UUID player, Material material) {
        Map<Material, Integer> map = storage.get(player);
        if (map == null) return 0;
        Integer removed = map.remove(material);
        return removed == null ? 0 : removed;
    }

    /**
     * Oyuncunun tum deposunu bosaltir ve bosaltilmadan onceki halini dondurur.
     */
    public Map<Material, Integer> takeEverything(UUID player) {
        Map<Material, Integer> map = storage.remove(player);
        return map == null ? new EnumMap<>(Material.class) : map;
    }

    /**
     * Depodan en fazla 'amount' kadar dusmeye calisir, gercekte dusulen miktari dondurur.
     * Envantere alma (withdraw) islemi icin kullanilir.
     */
    public int removeAmount(UUID player, Material material, int amount) {
        if (amount <= 0) return 0;
        Map<Material, Integer> map = storage.get(player);
        if (map == null) return 0;

        Integer current = map.get(material);
        if (current == null || current <= 0) return 0;

        int taken = Math.min(current, amount);
        int remaining = current - taken;

        if (remaining <= 0) {
            map.remove(material);
        } else {
            map.put(material, remaining);
        }

        return taken;
    }

    /**
     * Tum depo icin ortak kapasiteyi asmayacak sekilde ekler (materyal ayrimi yok,
     * hangi materyal olursa olsun ayni havuzu paylasir). Sigmayan kismi dondurur.
     */
    public int addWithCapacity(UUID player, Material material, int amount, int maxSlots) {
        if (amount <= 0) return 0;

        Map<Material, Integer> map = storage.computeIfAbsent(player, k -> new EnumMap<>(Material.class));
        int current = map.getOrDefault(material, 0);

        int currentSlotsForMat = (int) Math.ceil(current / (double) STACK_SIZE);
        int roomInLastStack = (current == 0) ? 0 : (currentSlotsForMat * STACK_SIZE) - current;

        int freeSlots = Math.max(0, maxSlots - usedSlots(player));
        int capacity = roomInLastStack + (freeSlots * STACK_SIZE);

        int toAdd = Math.min(amount, capacity);
        if (toAdd > 0) {
            map.merge(material, toAdd, Integer::sum);
        }
        return amount - toAdd;
    }

    // ---------- Kalicilik ----------

    public void load() {
        storage.clear();
        if (!file.exists()) return;

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = cfg.getConfigurationSection("storage");
        if (root == null) return;

        for (String uuidStr : root.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException ex) {
                continue;
            }

            ConfigurationSection playerSection = root.getConfigurationSection(uuidStr);
            if (playerSection == null) continue;

            Map<Material, Integer> map = new EnumMap<>(Material.class);
            for (String matName : playerSection.getKeys(false)) {
                Material mat = Material.matchMaterial(matName);
                if (mat == null) continue;
                map.put(mat, playerSection.getInt(matName));
            }
            storage.put(uuid, map);
        }
    }

    public void saveAll() {
        FileConfiguration cfg = new YamlConfiguration();

        for (Map.Entry<UUID, Map<Material, Integer>> entry : storage.entrySet()) {
            String base = "storage." + entry.getKey().toString();
            for (Map.Entry<Material, Integer> matEntry : entry.getValue().entrySet()) {
                cfg.set(base + "." + matEntry.getKey().name(), matEntry.getValue());
            }
        }

        try {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("storage.yml kaydedilemedi: " + e.getMessage());
        }
    }
}
