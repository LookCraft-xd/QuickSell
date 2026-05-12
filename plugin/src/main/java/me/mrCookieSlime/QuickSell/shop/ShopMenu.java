package me.mrCookieSlime.QuickSell.shop;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.mrCookieSlime.QuickSell.QuickSell;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShopMenu {

    /**
     * Opens a shop menu for a player
     *
     * @param p    Player
     * @param shop Shop
     */
    public static void open(Player p, Shop shop) {
        if (!shop.hasUnlocked(p)) {
            QuickSell.local.sendMessage(p, "messages.no-access", false);
            return;
        }

        int rows = QuickSell.cfg.getInt("options.sell-gui-rows");
        Gui gui = Gui.gui()
                .title(Component.text(QuickSell.local.getMessage("menu.title")))
                .rows(rows)
                .create();

        // Bloqueamos que los ítems de los botones se puedan mover,
        // pero el resto del inventario queda libre para que pongan sus cosas.
        if (QuickSell.cfg.getBoolean("options.enable-menu-line")) {

            // 1. Rellenar la última fila con el cristal gris decorativo
            // fillBottom llena los últimos 9 slots automáticamente
            gui.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
                    .name(Component.empty())
                    .asGuiItem(e -> e.setCancelled(true)));

            int lastRow = rows; // Triumph usa filas de 1 a 6

            // 2. Botón Aceptar (Columna 2 de la última fila)
            gui.setItem(lastRow, 2, ItemBuilder.from(Material.LIME_STAINED_GLASS_PANE)
                    .name(Component.text(QuickSell.local.getMessage("menu.accept")))
                    .asGuiItem(event -> {
                        event.setCancelled(true);
                        // Tu lógica de venta aquí
                    }));

            // 3. Botón Estimar (Columna 5 de la última fila)
            gui.setItem(lastRow, 5, ItemBuilder.from(Material.YELLOW_STAINED_GLASS_PANE)
                    .name(Component.text(QuickSell.local.getMessage("menu.estimate")))
                    .asGuiItem(event -> {
                        event.setCancelled(true);
                        // Tu lógica de estimación aquí
                    }));

            // 4. Botón Cancelar (Columna 8 de la última fila)
            gui.setItem(lastRow, 8, ItemBuilder.from(Material.RED_STAINED_GLASS_PANE)
                    .name(Component.text(QuickSell.local.getMessage("menu.cancel")))
                    .asGuiItem(event -> {
                        event.setCancelled(true);
                        gui.close(p);
                    }));
        }

        gui.open(p);
        QuickSell.shop.put(p.getUniqueId(), shop);
    }

    /**
     * Opens a shop menu based on the hierarchy
     *
     * @param p Player
     */
    public static void openMenu(Player p) {
        if (QuickSell.cfg.getBoolean("shop.enable-hierarchy")) {
            Shop highest = Shop.getHighestShop(p);
            if (highest != null) {
                open(p, highest);
            } else {
                QuickSell.local.sendMessage(p, "messages.no-access", false);
            }
            return;
        }

        // Calculamos las filas necesarias según la cantidad de tiendas
        int shopCount = Shop.list().size();
        int rows = (int) Math.ceil(shopCount / 9.0);
        if (rows == 0) rows = 1;

        Gui menu = Gui.gui()
                .title(Component.text(QuickSell.local.getMessage("menu.title")))
                .rows(rows)
                .create();

        menu.setDefaultClickAction(event -> event.setCancelled(true));

        for (Shop shop : Shop.list()) {
            if (shop == null) continue;

            boolean unlocked = shop.hasUnlocked(p);
            ItemStack icon = shop.getItem(unlocked ? ShopStatus.UNLOCKED : ShopStatus.LOCKED);

            menu.addItem(ItemBuilder.from(icon).asGuiItem(event -> {
                // Al hacer clic, abrimos la interfaz de venta de esa tienda
                open(p, shop);
            }));
        }

        menu.open(p);
    }

    final static int shop_size = 45;

    /**
     * Opens a GUI displaying shop items and prices to a player
     *
     * @param p    Player
     * @param shop Shop
     */
    public static void openPrices(Player p, final Shop shop) {
        PaginatedGui gui = Gui.paginated()
                .title(Component.text("Shop Prices"))
                .rows(6)
                .pageSize(45)
                .create();

        gui.setDefaultClickAction(event -> event.setCancelled(true));

        gui.setOpenGuiAction(event -> p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F));

        GuiItem filler = ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()).asGuiItem();
        gui.getFiller().fillBottom(filler);

        // Botón Anterior
        gui.setItem(46, ItemBuilder.from(Material.LIME_STAINED_GLASS_PANE)
                .name(Component.text("\u21E6 Previous Page"))
                .asGuiItem(event -> gui.previous()));

        // Botón Siguiente
        gui.setItem(52, ItemBuilder.from(Material.LIME_STAINED_GLASS_PANE)
                .name(Component.text("Next Page \u21E8"))
                .asGuiItem(event -> gui.next()));

        for (String id : shop.getPrices().getItems()) {
            ItemStack itemStack = shop.getPrices().getItem(id);
            if (itemStack == null) continue;

            gui.addItem(ItemBuilder.from(itemStack).asGuiItem());
        }

        gui.open(p);
    }

}
