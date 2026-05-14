package me.mrCookieSlime.QuickSell.inventories;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.core.utils.enums.Messages;
import me.mrCookieSlime.QuickSell.core.utils.message.MessageHandler;
import me.mrCookieSlime.QuickSell.manager.ShopManager;
import me.mrCookieSlime.QuickSell.shop.Shop;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class ShopMenu {

    private QuickSell plugin = QuickSell.getInstance();
    private ShopManager shopManager = plugin.getShopManager();
    private MessageHandler messageHandler = plugin.getMessageHandler();

    public ShopMenu(QuickSell plugin) {
        this.plugin = plugin;
        this.shopManager = plugin.getShopManager();
        this.messageHandler = plugin.getMessageHandler();
    }

    public void open(Player p, Shop shop) {
        if (!shop.hasUnlocked(p)) {
            messageHandler.build(p, Messages.NO_ACCESS).send();
            return;
        }

        YamlDocument config = plugin.getConfiguration();
        int rows = config.getInt("options.sell-gui-rows", 6);

        Gui gui = Gui.gui()
                .title(messageHandler.build(Messages.MENU_TITLE).placeholder("%shop%", shop.getName()).asComponent())
                .rows(rows)
                .create();

        // IMPORTANTE: No cancelamos el click general para que puedan meter sus ítems

        if (config.getBoolean("options.enable-menu-line", true)) {
            // Rellenar fondo decorativo
            gui.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
                    .name(Component.empty())
                    .asGuiItem(e -> e.setCancelled(true)));

            int lastRow = rows;

            // Botón Aceptar (Vender todo lo que hay dentro)
            gui.setItem(lastRow, 2, ItemBuilder.from(Material.LIME_STAINED_GLASS_PANE)
                    .name(messageHandler.build(Messages.MENU_ACCEPT).asComponent())
                    .asGuiItem(event -> {
                        event.setCancelled(true);
                        processSale(p, shop, gui, false);
                    }));

            // Botón Estimar (Solo decir cuánto ganarían)
            gui.setItem(lastRow, 5, ItemBuilder.from(Material.YELLOW_STAINED_GLASS_PANE)
                    .name(messageHandler.build(Messages.MENU_ESTIMATE).asComponent())
                    .asGuiItem(event -> {
                        event.setCancelled(true);
                        processSale(p, shop, gui, true);
                    }));

            // Botón Cancelar
            gui.setItem(lastRow, 8, ItemBuilder.from(Material.RED_STAINED_GLASS_PANE)
                    .name(messageHandler.build(Messages.MENU_CANCEL).asComponent())
                    .asGuiItem(event -> {
                        event.setCancelled(true);
                        gui.close(p); // Al cerrar, Triumph suele devolver los ítems si no se maneja el close
                    }));
        }

        gui.setCloseGuiAction(event -> {
            Inventory inv = event.getInventory();


            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);

                // 1. Saltamos slots vacíos
                if (item == null || item.getType() == Material.AIR) continue;

                // 2. Verificamos si en ese slot EXACTO hay un GuiItem (un botón de Triumph)
                // Si gui.getGuiItem(i) es null, significa que es un ítem que el jugador puso ahí
                if (gui.getGuiItem(i) == null) {
                    // 3. Devolvemos el ítem al jugador
                    p.getInventory().addItem(item).values().forEach(remaining ->
                            p.getWorld().dropItemNaturally(p.getLocation(), remaining)
                    );

                    // Opcional: Limpiar el slot para evitar duplicados en el cierre
                    inv.setItem(i, null);
                }
            }
        });

        gui.open(p);
    }

    public void openMenu(Player p) {
        // 1. Si la jerarquía está activa, buscamos la mejor tienda directamente
        if (plugin.getConfiguration().getBoolean("shop.enable-hierarchy", false)) {
            shopManager.getHighestShop(p).ifPresentOrElse(
                    highest -> open(p, highest),
                    () -> messageHandler.build(p, Messages.NO_ACCESS).send()
            );
            return;
        }

        // 2. Si no, mostramos el selector de todas las tiendas
        Collection<Shop> allShops = shopManager.getAllShops();
        int rows = (int) Math.ceil(allShops.size() / 9.0);
        if (rows == 0) rows = 1;

        Gui menu = Gui.gui()
                .title(messageHandler.build(Messages.MENU_TITLE).asComponent())
                .rows(Math.min(rows, 6))
                .create();

        menu.setDefaultClickAction(event -> event.setCancelled(true));

        for (Shop shop : allShops) {
            boolean unlocked = shop.hasUnlocked(p);
            // Asumiendo que shop.getDisplayItem() devuelve el ítem base
            // Puedes aplicar filtros aquí para cambiar el ítem si está bloqueado
            menu.addItem(ItemBuilder.from(shop.getMaterial())
                    .name(Component.text(shop.getName()))
                    .lore(unlocked ? Component.text("&aClick to open") : Component.text("&cLocked"))
                    .asGuiItem(event -> {
                        if (unlocked) open(p, shop);
                        else messageHandler.build(p, Messages.NO_ACCESS).send();
                    }));
        }

        menu.open(p);
    }

    public void openPrices(Player p, final Shop shop) {
        PaginatedGui gui = Gui.paginated()
                .title(Component.text("&6Prices: " + shop.getName()))
                .rows(6)
                .pageSize(45)
                .create();

        gui.setDefaultClickAction(event -> event.setCancelled(true));
        gui.setOpenGuiAction(event -> p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F));

        gui.getFiller().fillBottom(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).name(Component.empty()).asGuiItem());

        // Botones de navegación usando slots fijos de la última fila (46 y 52 son slots de 0-53)
        gui.setItem(6, 3, ItemBuilder.from(Material.ARROW).name(Component.text("&ePrevious Page")).asGuiItem(e -> gui.previous()));
        gui.setItem(6, 7, ItemBuilder.from(Material.ARROW).name(Component.text("&eNext Page")).asGuiItem(e -> gui.next()));

        // Cargamos los precios consolidados (con herencia) para que el jugador vea TODO lo que puede vender

        shop.getPrices().getLocalPrices().forEach((material, price) -> {
            ItemStack itemStack = new ItemStack(Material.matchMaterial(material));

            gui.addItem(ItemBuilder.from(itemStack)
                    .addLore("&r", "&7Price: &6$" + price)
                    .asGuiItem());
        });

        gui.open(p);
    }

    private void processSale(Player p, Shop shop, Gui gui, boolean estimateOnly) {
        double total = 0;
        Inventory inv = gui.getInventory();

        for (int i = 0; i < inv.getSize(); i++) {
            // Ignorar los botones del GUI
            if (gui.getGuiItem(i) != null) continue;

            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;

            double price = shop.getPrices().getPrice(item); // Tu PriceInfo debe saber buscar por ItemStack
            if (price > 0) {
                total += (price * item.getAmount());
                if (!estimateOnly) inv.setItem(i, null); // Quitamos el ítem si es venta real
            }
        }

        if (total > 0) {
            if (estimateOnly) {
                p.sendMessage("&eEstimación: &fGanarías &a$" + total);
            } else {
                // Aquí integrarías con Vault para dar el dinero
                p.sendMessage("&aHas vendido tus ítems por &f$" + total);
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            }
        } else {
            p.sendMessage("&cNo tienes ítems válidos para esta tienda en el menú.");
        }
    }
}
