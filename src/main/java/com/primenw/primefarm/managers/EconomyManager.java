package com.primenw.primefarm.managers;

import com.primenw.primefarm.PrimeFarm;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Vault yuklu ve bir ekonomi eklentisi calisiyorsa onun uzerinden,
 * degilse dahili basit bir bakiye dosyasi uzerinden para islemlerini yonetir.
 */
public class EconomyManager {

    private final PrimeFarm plugin;
    private Economy vaultEconomy;
    private boolean vaultEnabled = false;

    // Dahili fallback ekonomi
    private final File balancesFile;
    private final Map<UUID, Double> balances = new HashMap<>();

    public EconomyManager(PrimeFarm plugin) {
        this.plugin = plugin;
        this.balancesFile = new File(plugin.getDataFolder(), "balances.yml");
        setup();
        loadFallback();
    }

    private void setup() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().info("Vault bulunamadi, dahili ekonomi sistemi kullanilacak.");
            return;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer()
                .getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            plugin.getLogger().info("Vault var ama bir ekonomi eklentisi bulunamadi, dahili ekonomi kullanilacak.");
            return;
        }

        vaultEconomy = rsp.getProvider();
        vaultEnabled = true;
        plugin.getLogger().info("Vault ekonomisine baglanildi.");
    }

    public boolean isVaultEnabled() {
        return vaultEnabled;
    }

    /**
     * Oyuncuya belirtilen miktari yatirir. Vault varsa Vault uzerinden, yoksa dahili sisteme.
     */
    public void deposit(OfflinePlayer player, double amount) {
        if (vaultEnabled) {
            vaultEconomy.depositPlayer(player, amount);
            return;
        }
        UUID uuid = player.getUniqueId();
        balances.merge(uuid, amount, Double::sum);
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        if (vaultEnabled) {
            return vaultEconomy.withdrawPlayer(player, amount).transactionSuccess();
        }
        UUID uuid = player.getUniqueId();
        double current = balances.getOrDefault(uuid, 0.0);
        if (current < amount) return false;
        balances.put(uuid, current - amount);
        return true;
    }

    public double getBalance(OfflinePlayer player) {
        if (vaultEnabled) {
            return vaultEconomy.getBalance(player);
        }
        return balances.getOrDefault(player.getUniqueId(), 0.0);
    }

    public String getCurrencyName() {
        if (vaultEnabled) {
            return vaultEconomy.currencyNamePlural();
        }
        return plugin.getConfig().getString("currency-name", "Coin");
    }

    // ---------- Dahili ekonomi kaliciligi ----------

    private void loadFallback() {
        if (!balancesFile.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(balancesFile);
        if (cfg.getConfigurationSection("balances") == null) return;

        for (String uuidStr : cfg.getConfigurationSection("balances").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                balances.put(uuid, cfg.getDouble("balances." + uuidStr));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void saveFallback() {
        if (vaultEnabled) return; // Vault kendi verisini kendi yonetiyor
        FileConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            cfg.set("balances." + entry.getKey().toString(), entry.getValue());
        }
        try {
            if (!balancesFile.getParentFile().exists()) balancesFile.getParentFile().mkdirs();
            cfg.save(balancesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("balances.yml kaydedilemedi: " + e.getMessage());
        }
    }
}
