package com.primenw.primefarm.managers;

import com.primenw.primefarm.PrimeFarm;
import org.bukkit.Location;

import java.util.UUID;

/**
 * Gercek plot eklentisi entegrasyonu yapilana kadar kullanilan yer tutucu.
 * Her zaman null dondurur, yani hicbir konum bir arsa olarak taninmaz ve
 * otomatik toplama sistemi devreye girmez. Sunucu konsoluna acilista tek
 * seferlik bir uyari basar.
 */
public class NoopPlotHook implements PlotHook {

    public NoopPlotHook(PrimeFarm plugin) {
        plugin.getLogger().warning("====================================================");
        plugin.getLogger().warning(" PrimeFarm: Henuz gercek bir plot eklentisine baglanmadi!");
        plugin.getLogger().warning(" Otomatik toplama sistemi calismayacak (hicbir konum");
        plugin.getLogger().warning(" bir arsa olarak taninmayacak). Kullandiginiz plot");
        plugin.getLogger().warning(" eklentisini (PlotSquared / PlotMe / vb.) belirtip");
        plugin.getLogger().warning(" gercek PlotHook implementasyonunu ekletin.");
        plugin.getLogger().warning("====================================================");
    }

    @Override
    public UUID getOwnerAt(Location location) {
        return null;
    }

    @Override
    public boolean isReady() {
        return false;
    }
}
