package me.mrCookieSlime.QuickSell.shop;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PriceInfo {

    private final Map<String, Double> localPrices = new HashMap<>();
    private final Map<String, Double> consolidatedPrices = new HashMap<>();

    // It just exists shrug
    public PriceInfo() {
        this.localPrices.put("STONE", 1.0);
        sync();
    }

    public PriceInfo(Section section) {
        if (section == null || section.isEmpty(false)) {
            this.localPrices.put("STONE", 1.0);
        } else {
            section.getStringRouteMappedValues(false).forEach((key, value) -> {
                try {
                    double price = Double.parseDouble(value.toString());
                    if (price > 0) localPrices.put(key.toUpperCase(), price);
                } catch (NumberFormatException ignored) {
                }
            });
        }
        sync();
    }

    public void sync() {
        consolidatedPrices.clear();
        consolidatedPrices.putAll(localPrices);
    }

    public void mergePrices(PriceInfo parentInfo) {
        parentInfo.getConsolidatedPrices().forEach(consolidatedPrices::putIfAbsent);
    }

    public double getPrice(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return 0.0;

        return consolidatedPrices.getOrDefault(item.getType().name(), 0.0);
    }

    public Map<String, Object> serialize() {
        return new LinkedHashMap<>(localPrices);
    }

    public Map<String, Double> getConsolidatedPrices() {
        return Collections.unmodifiableMap(consolidatedPrices);
    }

    /**
     * Añade o actualiza un precio en el mapa local.
     */
    public void setPrice(String key, double price) {
        this.localPrices.put(key, price);
    }

    /**
     * Elimina un ítem del mapa local.
     */
    public void removePrice(String key) {
        this.localPrices.remove(key);
    }

    /**
     * Obtiene todas las llaves (IDs de materiales/ítems) definidos
     * ÚNICAMENTE en esta tienda, ignorando herencias.
     * Útil para el editor, para no mostrar ítems repetidos de padres.
     */
    public Set<String> getLocalItems() {
        return this.localPrices.keySet();
    }

    public Map<String, Double> getLocalPrices() {
        return Collections.unmodifiableMap(localPrices);
    }

    /**
     * Verifica si un ítem existe localmente.
     */
    public boolean hasLocalItem(String key) {
        return this.localPrices.containsKey(key);
    }

}
