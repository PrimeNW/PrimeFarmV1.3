package com.primenw.primefarm.managers;

import org.bukkit.Location;

import java.util.UUID;

/**
 * Sunucuda kullanilan plot/arsa eklentisine baglanmak icin soyutlama katmani.
 *
 * ONEMLI: Su an icin gercek bir plot eklentisine (PlotSquared, PlotMe, Towny vb.)
 * baglanmiyor - hangi plot pluginini kullandiginiz netlesince bu arayuzun somut
 * bir implementasyonu (orn. PlotSquaredHook) yazilip PrimeFarm#onEnable icinde
 * kayit edilecek. Su anki NoopPlotHook, entegrasyon gelene kadar sistemin
 * derlenip calismasini saglayan bos bir yer tutucudur.
 *
 * Beklenen gercek davranis: bir konumun (Location) hangi oyuncuya ait bir
 * plot/arsa icinde oldugunu dondurmek. PrimeFarm bunu, o konumda dusen
 * otomatik-farm itemlerini kimin deposuna ekleyecegine karar vermek icin kullanir.
 */
public interface PlotHook {

    /**
     * Verilen konum bir oyuncuya ait plot/arsa icindeyse o oyuncunun UUID'sini,
     * degilse null dondurur.
     */
    UUID getOwnerAt(Location location);

    /**
     * Bu hook'un gercek bir plot eklentisine bagli olup olmadigini belirtir.
     * false ise NoopPlotHook aktif demektir ve otomatik toplama calismaz.
     */
    boolean isReady();
}
