package com.primenw.primefarm.managers;

import com.primenw.primefarm.PrimeFarm;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Oyuncunun /pf settings uzerinden kapatabildigi materyalleri tutar.
 * Kapatilan materyaller ItemCollectListener tarafindan yok sayilir
 * (item normal sekilde yere duser, depoya/otomatik satisa gitmez).
 * Ayarlanabilir materyal listesi config.yml -> toggleable-materials'tan gelir.
 */
public class PlayerSettingsManager {

    private final PrimeFarm plugin;
    private final File file;
    private final Map<UUID, Set<Material>> disabled = new ConcurrentHashMap<>();

    public PlayerSettingsManager(PrimeFarm plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "settings.yml");
        load();
    }

    public List<Material> getToggleableMaterials() {
        List<Material> result = new ArrayList<>();
        for (String name : plugin.getConfig().getStringList("toggleable-materials")) {
            Material mat = Material.matchMaterial(name);
            if (mat != null) result.add(mat);
        }
        return result;
    }

    public boolean isEnabled(UUID uuid, Material material) {
        Set<Material> set = disabled.get(uuid);
        return set == null || !set.contains(material);
    }

    public void toggle(UUID uuid, Material material) {
        Set<Material> set = disabled.computeIfAbsent(uuid, k -> new HashSet<>());
        if (!set.add(material)) set.remove(material);
        save();
    }

    // ---------- Kalicilik ----------

    public void load() {
        disabled.clear();
        if (!file.exists()) return;

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        if (cfg.getConfigurationSection("disabled") == null) return;

        for (String uuidStr : cfg.getConfigurationSection("disabled").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                Set<Material> set = new HashSet<>();
                for (String matName : cfg.getStringList("disabled." + uuidStr)) {
                    Material mat = Material.matchMaterial(matName);
                    if (mat != null) set.add(mat);
                }
                disabled.put(uuid, set);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void save() {
        FileConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, Set<Material>> entry : disabled.entrySet()) {
            List<String> names = new ArrayList<>();
            for (Material m : entry.getValue()) names.add(m.name());
            cfg.set("disabled." + entry.getKey().toString(), names);
        }
        try {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("settings.yml kaydedilemedi: " + e.getMessage());
        }
    }
}
