package com.primenw.primefarm.managers;

import com.primenw.primefarm.PrimeFarm;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * TR / EN / BR dil destegini yonetir.
 * Her oyuncunun kendi dil tercihi playerlang.yml icinde kalici olarak tutulur.
 * Tercih belirtilmemisse config.yml -> default-language kullanilir.
 */
public class LanguageManager {

    public static final List<String> SUPPORTED = java.util.Arrays.asList("tr", "en", "br");

    private final PrimeFarm plugin;
    private final Map<String, FileConfiguration> languages = new HashMap<>();
    private final Map<UUID, String> playerLanguage = new HashMap<>();
    private final File playerLangFile;
    private String defaultLanguage;

    public LanguageManager(PrimeFarm plugin) {
        this.plugin = plugin;
        this.playerLangFile = new File(plugin.getDataFolder(), "playerlang.yml");
        reload();
    }

    public void reload() {
        defaultLanguage = plugin.getConfig().getString("default-language", "tr");
        if (!SUPPORTED.contains(defaultLanguage)) defaultLanguage = "tr";

        languages.clear();
        for (String lang : SUPPORTED) {
            languages.put(lang, loadLanguageFile(lang));
        }

        loadPlayerPreferences();
    }

    private FileConfiguration loadLanguageFile(String lang) {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) langFolder.mkdirs();

        File external = new File(langFolder, lang + ".yml");
        if (!external.exists()) {
            // Jar icindeki varsayilan dil dosyasini disariya kopyala, boylece sunucu
            // sahibi isterse kendi sunucusunda metinleri degistirebilir.
            try (InputStream in = plugin.getResource("lang/" + lang + ".yml")) {
                if (in != null) {
                    java.nio.file.Files.copy(in, external.toPath());
                }
            } catch (IOException e) {
                plugin.getLogger().warning("lang/" + lang + ".yml disariya kopyalanamadi: " + e.getMessage());
            }
        }

        if (external.exists()) {
            return YamlConfiguration.loadConfiguration(external);
        }

        // Disk kopyasi olusturulamadiysa dogrudan jar icinden oku.
        InputStream in = plugin.getResource("lang/" + lang + ".yml");
        if (in == null) return new YamlConfiguration();
        Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
        return YamlConfiguration.loadConfiguration(reader);
    }

    // ---------- Oyuncu dil tercihi ----------

    public String getLanguage(CommandSender sender) {
        if (sender instanceof Player) {
            String pref = playerLanguage.get(((Player) sender).getUniqueId());
            if (pref != null) return pref;
        }
        return defaultLanguage;
    }

    public boolean setLanguage(Player player, String lang) {
        if (!SUPPORTED.contains(lang)) return false;
        playerLanguage.put(player.getUniqueId(), lang);
        savePlayerPreferences();
        return true;
    }

    private void loadPlayerPreferences() {
        playerLanguage.clear();
        if (!playerLangFile.exists()) return;

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(playerLangFile);
        if (cfg.getConfigurationSection("players") == null) return;

        for (String uuidStr : cfg.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                playerLanguage.put(uuid, cfg.getString("players." + uuidStr));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void savePlayerPreferences() {
        FileConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, String> entry : playerLanguage.entrySet()) {
            cfg.set("players." + entry.getKey().toString(), entry.getValue());
        }
        try {
            if (!playerLangFile.getParentFile().exists()) playerLangFile.getParentFile().mkdirs();
            cfg.save(playerLangFile);
        } catch (IOException e) {
            plugin.getLogger().severe("playerlang.yml kaydedilemedi: " + e.getMessage());
        }
    }

    // ---------- Mesaj okuma ----------

    private FileConfiguration fileFor(CommandSender sender) {
        String lang = getLanguage(sender);
        return languages.getOrDefault(lang, languages.get(defaultLanguage));
    }

    public String raw(CommandSender sender, String key) {
        FileConfiguration cfg = fileFor(sender);
        return cfg.getString(key, key);
    }

    public List<String> rawList(CommandSender sender, String key) {
        FileConfiguration cfg = fileFor(sender);
        return cfg.getStringList(key);
    }

    /**
     * Prefix ekleyip renklendirerek ve placeholder'lari degistirerek mesaji gonderir.
     * placeholders: "%isim%", "deger", "%isim2%", "deger2" ... seklinde ikili gruplar.
     */
    public void send(CommandSender target, String key, String... placeholders) {
        String prefix = fileFor(target).getString("prefix", "");
        String message = apply(raw(target, key), placeholders);
        target.sendMessage(colorize(prefix + message));
    }

    /**
     * Prefix eklemeden, tek satirlik ceviriyi renklendirip placeholder uygulayarak dondurur.
     * GUI item isimleri/lore'lari icin kullanilir.
     */
    public String get(CommandSender sender, String key, String... placeholders) {
        return colorize(apply(raw(sender, key), placeholders));
    }

    public List<String> getList(CommandSender sender, String key, String... placeholders) {
        List<String> result = new java.util.ArrayList<>();
        for (String line : rawList(sender, key)) {
            result.add(colorize(apply(line, placeholders)));
        }
        return result;
    }

    private String apply(String text, String... placeholders) {
        String result = text;
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            result = result.replace(placeholders[i], placeholders[i + 1]);
        }
        return result;
    }

    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
