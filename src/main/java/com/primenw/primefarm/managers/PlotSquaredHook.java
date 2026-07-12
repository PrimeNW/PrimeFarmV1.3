package com.primenw.primefarm.managers;

import com.primenw.primefarm.PrimeFarm;
import org.bukkit.Location;

import java.util.UUID;

/**
 * PlotSquared-Bukkit-18.12.12 icin gercek plot sorgusu.
 * API, CI'daki javap ciktisindan dogrulandi:
 *  - com.plotsquared.bukkit.util.BukkitUtil.getLocation(org.bukkit.Location)
 *  - com.intellectualcrafters.plot.object.Location.getPlot()
 *  - com.intellectualcrafters.plot.object.Plot.hasOwner() / .owner (public alan)
 */
public class PlotSquaredHook implements PlotHook {

    private final PrimeFarm plugin;
    private boolean warnedOnce = false;

    public PlotSquaredHook(PrimeFarm plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("PrimeFarm: PlotSquared entegrasyonu aktif edildi.");
    }

    @Override
    public UUID getOwnerAt(Location bukkitLoc) {
        try {
            com.intellectualcrafters.plot.object.Location psLoc =
                    com.plotsquared.bukkit.util.BukkitUtil.getLocation(bukkitLoc);

            com.intellectualcrafters.plot.object.Plot plot = psLoc.getPlot();

            if (plot == null || !plot.hasOwner()) return null;
            return plot.owner;
        } catch (Throwable t) {
            if (!warnedOnce) {
                warnedOnce = true;
                plugin.getLogger().warning("PlotSquared sorgusu basarisiz oldu: " + t);
            }
            return null;
        }
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
