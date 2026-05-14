package me.mrCookieSlime.QuickSell.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class InventoryUtils {

    /**
     * Maneja los ítems sobrantes o no vendidos.
     * Intenta añadirlos al inventario y, si no caben, los dropea al suelo.
     *
     * @param player El jugador que recibe/mantiene los ítems.
     * @param item   El ItemStack a procesar.
     */
    public static void handleRest(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR || item.getAmount() <= 0) return;

        Map<Integer, ItemStack> remaining = player.getInventory().addItem(item);
        if (!remaining.isEmpty()) {
            for (ItemStack leftover : remaining.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }
    }
}
