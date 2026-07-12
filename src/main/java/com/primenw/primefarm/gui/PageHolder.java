package com.primenw.primefarm.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Baslik farkli dillerde degistigi icin (TR/EN/BR), acik olan bir StorageGUI
 * envanterinin hangi sayfaya ait oldugunu InventoryHolder uzerinden tutuyoruz.
 * GUIListener bu sayede title string'ine bakmadan sayfayi guvenilir sekilde bulur.
 */
public class PageHolder implements InventoryHolder {

    private final int page;
    private Inventory inventory;

    public PageHolder(int page) {
        this.page = page;
    }

    public int getPage() {
        return page;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
